package com.school21.shopapi.security;

import com.school21.shopapi.service.AuthClient;
import io.grpc.StatusRuntimeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthClient authClient;

    public AuthInterceptor(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Пропускаем не-контроллерные обработчики (статика, CORS)
        if (!(handler instanceof HandlerMethod)) {
            return true; 
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // Проверяем наличие @RequiresAuth на методе или классе
        RequiresAuth requiresAuth = handlerMethod.getMethodAnnotation(RequiresAuth.class);
        if (requiresAuth == null) {
            requiresAuth = handlerMethod.getBeanType().getAnnotation(RequiresAuth.class);
        }

        // Без аннотации — эндпоинт публичный
        if (requiresAuth == null) {
            return true;
        }

        // Проверяем наличие заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            // Валидация токена через gRPC
            var validationResponse = authClient.validateToken(token);
            
            // Проверка ролей (RBAC)
            String[] allowedRoles = requiresAuth.roles();
            if (allowedRoles.length > 0) {
                String userRole = validationResponse.getRole();
                boolean hasRole = Arrays.asList(allowedRoles).contains(userRole);
                
                if (!hasRole) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403
                    response.getWriter().write("Access Denied: Insufficient permissions (Requires: " + Arrays.toString(allowedRoles) + ")");
                    return false;
                }
            }
            
            // Пробрасываем email пользователя в request для контроллера
            request.setAttribute("userEmail", validationResponse.getEmail());
            
            return true;
            
        } catch (StatusRuntimeException e) {
            // Невалидный или просроченный токен
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
            response.getWriter().write(e.getStatus().getDescription() != null ? e.getStatus().getDescription() : "Invalid token");
            return false;
        }
    }
}

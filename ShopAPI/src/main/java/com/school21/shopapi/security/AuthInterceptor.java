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
        // Пропускаем статику и CORS запросы
        if (!(handler instanceof HandlerMethod)) {
            return true; 
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // Ищем нашу аннотацию @RequiresAuth на методе или классе
        RequiresAuth requiresAuth = handlerMethod.getMethodAnnotation(RequiresAuth.class);
        if (requiresAuth == null) {
            requiresAuth = handlerMethod.getBeanType().getAnnotation(RequiresAuth.class);
        }

        // Если аннотации нет — метод публичный, пускаем дальше
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
            // Отправляем токен на проверку Питонисту по gRPC
            var validationResponse = authClient.validateToken(token);
            
            // Если токен валиден, проверяем роли (RBAC)
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
            
            // Сохраняем email в request, чтобы контроллер мог узнать, кто к нему пришел
            request.setAttribute("userEmail", validationResponse.getEmail());
            
            return true;
            
        } catch (StatusRuntimeException e) {
            // Токен просрочен или Питонист кинул ошибку
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
            response.getWriter().write(e.getStatus().getDescription() != null ? e.getStatus().getDescription() : "Invalid token");
            return false;
        }
    }
}

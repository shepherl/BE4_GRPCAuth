package com.school21.shopapi.service;

import com.school21.shopapi.grpc.auth.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
public class AuthClientImpl implements AuthClient {

    private final ManagedChannel channel;
    private final AuthServiceGrpc.AuthServiceBlockingStub authStub;

    // Внедряем адрес Питониста (по умолчанию auth-api:50051)
    public AuthClientImpl(@Value("${auth.service.host:auth-api}") String host,
                          @Value("${auth.service.port:50051}") int port) {
        // Создаем канал связи (без SSL шифрования, так как мы внутри Docker-сети)
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.authStub = AuthServiceGrpc.newBlockingStub(channel);
    }

    // Закрываем канал при выключении Spring Boot
    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    @Override
    public String register(String email, String firstName, String lastName, String phone, String password) {
        RegisterRequest request = RegisterRequest.newBuilder()
                .setEmail(email)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setPhone(phone)
                .setPassword(password)
                .build();

        AuthResponse response = authStub.register(request);

        // === ВРЕМЕННЫЙ АДАПТЕР ===
        if (!response.getError().isEmpty()) {
            // Искусственно кидаем gRPC Exception, чтобы контроллер красиво его поймал
            throw new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription(response.getError()));
        }

        return response.getToken();
    }

    @Override
    public String login(String email, String password) {
        LoginRequest request = LoginRequest.newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build();

        AuthResponse response = authStub.login(request);

        // === ВРЕМЕННЫЙ АДАПТЕР ===
        if (!response.getError().isEmpty()) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(response.getError()));
        }

        return response.getToken();
    }

    @Override
    public ValidateTokenResponse validateToken(String token) {
        ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
                .setToken(token)
                .build();

        ValidateTokenResponse response = authStub.validateToken(request);

        // === ВРЕМЕННЫЙ АДАПТЕР ===
        if (!response.getIsValid() || !response.getError().isEmpty()) {
            throw new StatusRuntimeException(Status.UNAUTHENTICATED.withDescription(response.getError()));
        }

        return response;
    }

    @Override
    public ChangePasswordResponse changePassword(String token, String oldPassword, String newPassword) {
        ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                .setToken(token)
                .setOldPassword(oldPassword)
                .setNewPassword(newPassword)
                .build();

        ChangePasswordResponse response = authStub.changePassword(request);

        // === ВРЕМЕННЫЙ АДАПТЕР ===
        if (!response.getSuccess() || !response.getError().isEmpty()) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(response.getError()));
        }

        return response;
    }

    @Override
    public ResetPasswordResponse resetPassword(String email) {
        ResetPasswordRequest request = ResetPasswordRequest.newBuilder()
                .setEmail(email)
                .build();

        // У ResetPassword нет поля error, Питонист возвращает только success и message
        return authStub.resetPassword(request);
    }
}

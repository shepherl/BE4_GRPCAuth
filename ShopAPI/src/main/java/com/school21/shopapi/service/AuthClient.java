package com.school21.shopapi.service;

import com.school21.shopapi.grpc.auth.ChangePasswordResponse;
import com.school21.shopapi.grpc.auth.ResetPasswordResponse;
import com.school21.shopapi.grpc.auth.ValidateTokenResponse;

public interface AuthClient {
    String register(String email, String firstName, String lastName, String phone, String password);
    String login(String email, String password);
    ValidateTokenResponse validateToken(String token);
    ChangePasswordResponse changePassword(String token, String oldPassword, String newPassword);
    ResetPasswordResponse resetPassword(String email);
}

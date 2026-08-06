package com.school21.shopapi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthChangePasswordDto {
    @NotBlank(message = "Старый пароль не может быть пустым")
    private String oldPassword;

    @NotBlank(message = "Новый пароль не может быть пустым")
    private String newPassword;
}

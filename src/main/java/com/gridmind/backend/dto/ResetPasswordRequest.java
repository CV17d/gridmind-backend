package com.gridmind.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "El token de restablecimiento es obligatorio.")
    @Size(max = 512, message = "Token inválido.")
    private String token;

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres.")
    private String newPassword;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}

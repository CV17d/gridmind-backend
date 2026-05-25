package com.gridmind.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "La contraseña actual es obligatoria.")
    @Size(max = 128, message = "La contraseña no puede superar los 128 caracteres.")
    private String currentPassword;

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 8, max = 128, message = "La nueva contraseña debe tener entre 8 y 128 caracteres.")
    private String newPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}

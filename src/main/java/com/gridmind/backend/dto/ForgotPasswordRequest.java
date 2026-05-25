package com.gridmind.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ForgotPasswordRequest {

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El formato del correo electrónico no es válido.")
    @Size(max = 254, message = "El correo electrónico no puede superar los 254 caracteres.")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

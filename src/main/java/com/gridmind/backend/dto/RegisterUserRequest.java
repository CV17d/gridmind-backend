package com.gridmind.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterUserRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    @Pattern(
        regexp = "^[\\p{L}\\s'-]+$",
        message = "El nombre solo puede contener letras, espacios, guiones y apóstrofes."
    )
    private String name;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El formato del correo electrónico no es válido.")
    @Size(max = 254, message = "El correo electrónico no puede superar los 254 caracteres.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres.")
    private String password;

    public RegisterUserRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
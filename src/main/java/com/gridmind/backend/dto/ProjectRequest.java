package com.gridmind.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectRequest {

    @NotBlank(message = "El nombre del proyecto es obligatorio.")
    @Size(min = 1, max = 100, message = "El nombre del proyecto debe tener entre 1 y 100 caracteres.")
    private String name;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
    private String description;

    public String getName() { return name; }
    public String getDescription() { return description; }
}
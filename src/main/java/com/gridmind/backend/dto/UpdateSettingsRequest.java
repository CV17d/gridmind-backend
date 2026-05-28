package com.gridmind.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateSettingsRequest {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    private String name;

    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa eléctrica debe ser un valor positivo.")
    private BigDecimal electricityRate;

    @DecimalMin(value = "0.0", inclusive = false, message = "El umbral de alerta debe ser un valor positivo.")
    private BigDecimal alertThreshold;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getElectricityRate() { return electricityRate; }
    public void setElectricityRate(BigDecimal electricityRate) { this.electricityRate = electricityRate; }

    public BigDecimal getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(BigDecimal alertThreshold) { this.alertThreshold = alertThreshold; }
}

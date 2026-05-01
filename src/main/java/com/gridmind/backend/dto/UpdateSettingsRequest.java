package com.gridmind.backend.dto;

import java.math.BigDecimal;

public class UpdateSettingsRequest {
    private String name;
    private BigDecimal electricityRate;
    private BigDecimal alertThreshold;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getElectricityRate() { return electricityRate; }
    public void setElectricityRate(BigDecimal electricityRate) { this.electricityRate = electricityRate; }

    public BigDecimal getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(BigDecimal alertThreshold) { this.alertThreshold = alertThreshold; }
}

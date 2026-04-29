package com.gridmind.backend.dto;

public class UpdateSettingsRequest {
    private String name;
    private Double electricityRate;
    private Double alertThreshold;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getElectricityRate() { return electricityRate; }
    public void setElectricityRate(Double electricityRate) { this.electricityRate = electricityRate; }

    public Double getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(Double alertThreshold) { this.alertThreshold = alertThreshold; }
}

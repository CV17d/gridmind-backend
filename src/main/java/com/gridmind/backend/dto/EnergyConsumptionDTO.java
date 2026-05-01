package com.gridmind.backend.dto;

import java.time.LocalDateTime;

public record EnergyConsumptionDTO(
        Long id,
        java.math.BigDecimal consumption,
        Double voltage,
        Double current,
        Double power,
        LocalDateTime timestamp,
        Long deviceId,
        String deviceName) {
}

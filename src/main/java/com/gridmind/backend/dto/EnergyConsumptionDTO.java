package com.gridmind.backend.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public record EnergyConsumptionDTO(
        Long id,
        BigDecimal consumption,
        Double voltage,
        Double current,
        Double power,
        LocalDateTime timestamp,
        Long deviceId,
        String deviceName) {
}

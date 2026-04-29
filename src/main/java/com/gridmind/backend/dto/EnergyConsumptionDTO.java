package com.gridmind.backend.dto;

import java.time.LocalDateTime;

public record EnergyConsumptionDTO(
        Long id,
        Double consumption,
        Double voltage,
        Double current,
        Double power,
        LocalDateTime timestamp,
        Long deviceId,
        String deviceName) {
}

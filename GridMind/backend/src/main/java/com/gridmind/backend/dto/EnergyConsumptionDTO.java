package com.gridmind.backend.dto;

import java.time.LocalDateTime;

public record EnergyConsumptionDTO(
        Long id,
        Double consumption,
        LocalDateTime timestamp,
        Long deviceId,
        String deviceName) {
}

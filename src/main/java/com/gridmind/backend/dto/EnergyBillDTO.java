package com.gridmind.backend.dto;

import java.time.LocalDateTime;

public record EnergyBillDTO(
        Long id,
        String fileUrl,
        Double totalKwh,
        Double totalAmount,
        String aiRecommendations,
        LocalDateTime uploadedAt
) {}

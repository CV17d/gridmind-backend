package com.gridmind.backend.dto;
import java.time.LocalDate;
public interface DailyConsumptionDTO {
    
    // Spring Boot inyectará automáticamente aquí la fecha del día
    LocalDate getDate();
    
    // Y aquí meterá el resultado de sumar todas las medidas ESP32 de ese día
    Double getTotalKwh();
}

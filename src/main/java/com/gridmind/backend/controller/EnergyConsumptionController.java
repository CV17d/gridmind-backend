package com.gridmind.backend.controller;

import com.gridmind.backend.model.EnergyConsumption;
import com.gridmind.backend.service.EnergyConsumptionService;

import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.gridmind.backend.dto.EnergyConsumptionDTO;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/energy")
public class EnergyConsumptionController {

    private final EnergyConsumptionService service;

    public EnergyConsumptionController(EnergyConsumptionService service) {
        this.service = service;
    }

    // ⚡ Registrar consumo (Devolviendo DTO)
    @PostMapping("/{deviceId}")
    public EnergyConsumptionDTO create(
            @PathVariable("deviceId") Long deviceId,
            @RequestParam("consumption") Double consumption,
            Authentication authentication) {
        String email = authentication.getName();
        EnergyConsumption savedEc = service.create(deviceId, consumption, email);
        // Convertir la Entidad devuelta por el servicio a DTO seguro
        return new EnergyConsumptionDTO(
                savedEc.getId(),
                savedEc.getConsumption(),
                savedEc.getVoltage(),
                savedEc.getCurrent(),
                savedEc.getPower(),
                savedEc.getTimestamp(),
                savedEc.getDevice().getId(),
                savedEc.getDevice().getName());
    }

    // 📊 Obtener historial (Con Paginación DTO)
    @GetMapping("/{deviceId}")
    public Page<EnergyConsumptionDTO> get(
            @PathVariable("deviceId") Long deviceId,
            Authentication authentication,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        String email = authentication.getName();
        Page<EnergyConsumption> rawPage = service.getByDevice(deviceId, email, pageable);
        // Map iterará por los 20 registros convirtiendo cada uno al molde DTO
        return rawPage.map(ec -> new EnergyConsumptionDTO(
                ec.getId(),
                ec.getConsumption(),
                ec.getVoltage(),
                ec.getCurrent(),
                ec.getPower(),
                ec.getTimestamp(),
                ec.getDevice().getId(),
                ec.getDevice().getName()));
    }
}
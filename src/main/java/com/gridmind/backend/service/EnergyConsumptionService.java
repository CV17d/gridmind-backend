package com.gridmind.backend.service;

import com.gridmind.backend.model.*;
import com.gridmind.backend.repository.*;
import com.gridmind.backend.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EnergyConsumptionService {

    private static final Logger log = LoggerFactory.getLogger(EnergyConsumptionService.class);

    private final EnergyConsumptionRepository repository;
    private final DeviceRepository deviceRepository;

    public EnergyConsumptionService(
            EnergyConsumptionRepository repository,
            DeviceRepository deviceRepository) {
        this.repository = repository;
        this.deviceRepository = deviceRepository;
    }

    // 🔥 Crear registro de consumo
    public EnergyConsumption create(Long deviceId, BigDecimal consumption, String email) {
        log.debug("Registrando consumo para dispositivo ID: {} por usuario: {}", deviceId, email);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));

        // 🔐 Seguridad por dueño
        if (!device.getUser().getEmail().equalsIgnoreCase(email)) {
            log.warn("ACCESO DENEGADO: El usuario {} intentó registrar consumo en el dispositivo {} que no posee.", email, deviceId);
            throw new AccessDeniedException("No tienes permiso sobre este dispositivo");
        }

        EnergyConsumption ec = new EnergyConsumption();
        ec.setConsumption(consumption);
        ec.setDevice(device);

        log.info("Consumo de {} kWh guardado exitosamente para dispositivo {}", consumption, deviceId);
        return repository.save(ec);
    }

    // 📊 Obtener historial (Ahora con Paginación)
    public Page<EnergyConsumption> getByDevice(Long deviceId, String email, Pageable pageable) {
        log.debug("Consultando historial paginado del dispositivo {} para el usuario {}", deviceId, email);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));

        // 🔐 Seguridad
        if (!device.getUser().getEmail().equalsIgnoreCase(email)) {
            log.warn("CONSULTA DENEGADA: El usuario {} intentó ver el historial del dispositivo {} sin autorización.", email, deviceId);
            throw new AccessDeniedException("No tienes permiso para ver este historial");
        }

        // Le pasamos la paginación al repositorio
        return repository.findByDevice(device, pageable);
    }
}
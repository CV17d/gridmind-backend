package com.gridmind.backend.service;

import com.gridmind.backend.model.*;
import com.gridmind.backend.repository.*;
import com.gridmind.backend.exception.*;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class EnergyConsumptionService {

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

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        // 🔐 Seguridad por dueño
        if (!device.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Access denied");
        }

        EnergyConsumption ec = new EnergyConsumption();
        ec.setConsumption(consumption);
        ec.setDevice(device);

        return repository.save(ec);
    }

    // 📊 Obtener historial (Ahora con Paginación)
    public Page<EnergyConsumption> getByDevice(Long deviceId, String email, Pageable pageable) {

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        // 🔐 Seguridad
        if (!device.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Access denied");
        }

        // Le pasamos la paginación al repositorio
        return repository.findByDevice(device, pageable);
    }
}
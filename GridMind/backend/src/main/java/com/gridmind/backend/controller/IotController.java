package com.gridmind.backend.controller;

import com.gridmind.backend.model.Device;
import com.gridmind.backend.model.EnergyConsumption;
import com.gridmind.backend.repository.DeviceRepository;
import com.gridmind.backend.repository.EnergyConsumptionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iot")
public class IotController {

    private final DeviceRepository deviceRepository;
    private final EnergyConsumptionRepository energyRepository;

    public IotController(DeviceRepository deviceRepository, EnergyConsumptionRepository energyRepository) {
        this.deviceRepository = deviceRepository;
        this.energyRepository = energyRepository;
    }

    @PostMapping("/energy/{esp32Id}")
    public ResponseEntity<String> registerIotConsumption(
            @PathVariable("esp32Id") String esp32Id,
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam("consumption") Double consumption) {

        // 1. Buscamos la placa física en la BD por su número de serie
        Device device = deviceRepository.findByEsp32Id(esp32Id).orElse(null);

        // 2. Seguridad Paranoica: Si no existe, o si la ApiKey no cuadra, lo echamos.
        if (device == null || !device.getApiKey().equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: API Key inválida o Hardware desconocido");
        }

        // 3. Registramos el Consumo limpiamente 
        EnergyConsumption ec = new EnergyConsumption();
        ec.setConsumption(consumption);
        ec.setDevice(device);

        energyRepository.save(ec);

        return ResponseEntity.ok("Lectura de energía guardada con éxito por GridMind");
    }
}

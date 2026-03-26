package com.gridmind.backend.controller;
import com.gridmind.backend.model.Device;
import com.gridmind.backend.model.EnergyConsumption;
import com.gridmind.backend.repository.DeviceRepository;
import com.gridmind.backend.repository.EnergyConsumptionRepository;
import com.gridmind.backend.service.AlertService;
import com.gridmind.backend.service.WebSocketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/iot")
public class IotController {
    private final DeviceRepository deviceRepository;
    private final EnergyConsumptionRepository energyRepository;
    private final AlertService alertService;
    private final WebSocketService webSocketService; // 📡 NUEVO
    public IotController(DeviceRepository deviceRepository, 
                         EnergyConsumptionRepository energyRepository,
                         AlertService alertService,
                         WebSocketService webSocketService) { // 📡 NUEVO
        this.deviceRepository = deviceRepository;
        this.energyRepository = energyRepository;
        this.alertService = alertService;
        this.webSocketService = webSocketService; // 📡 NUEVO
    }
    @PostMapping("/energy/{esp32Id}")
    public ResponseEntity<String> registerIotConsumption(
            @PathVariable("esp32Id") String esp32Id,
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam("consumption") Double consumption) {
        Device device = deviceRepository.findByEsp32Id(esp32Id).orElse(null);
        if (device == null || !device.getApiKey().equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Acceso denegado: API Key inválida o Hardware desconocido");
        }
        EnergyConsumption ec = new EnergyConsumption();
        ec.setConsumption(consumption);
        ec.setDevice(device);
        energyRepository.save(ec);
        // 🚨 EL VIGILANTE: Revisa si este consumo dispara una alerta
        alertService.checkAndTriggerAlert(device.getUser(), consumption);
        // 📡 Transmitir en vivo a todos los dashboards conectados
        webSocketService.broadcastEnergyReading(esp32Id, consumption);
        return ResponseEntity.ok("Lectura de energía guardada con éxito por GridMind");
    }
}

package com.gridmind.backend.controller;
import com.gridmind.backend.model.Device;
import com.gridmind.backend.model.EnergyConsumption;
import com.gridmind.backend.repository.DeviceRepository;
import com.gridmind.backend.repository.EnergyConsumptionRepository;
import com.gridmind.backend.service.AlertService;
import com.gridmind.backend.service.WebSocketService;
import com.gridmind.backend.service.PredictiveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/iot")
public class IotController {
    private final DeviceRepository deviceRepository;
    private final EnergyConsumptionRepository energyRepository;
    private final AlertService alertService;
    private final WebSocketService webSocketService;
    private final PredictiveService predictiveService; // 🧠 NUEVO

    public IotController(DeviceRepository deviceRepository, 
                         EnergyConsumptionRepository energyRepository,
                         AlertService alertService,
                         WebSocketService webSocketService,
                         PredictiveService predictiveService) {
        this.deviceRepository = deviceRepository;
        this.energyRepository = energyRepository;
        this.alertService = alertService;
        this.webSocketService = webSocketService;
        this.predictiveService = predictiveService;
    }
    // 📡 Nuevo endpoint para recibir telemetría vía JSON (Más moderno y seguro)
    @PostMapping("/telemetry")
    public ResponseEntity<String> receiveTelemetry(@RequestBody TelemetryRequest request) {
        // Buscamos el dispositivo por su API Key (la llave secreta)
        Device device = deviceRepository.findByApiKey(request.apiKey).orElse(null);

        if (device == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Error: API Key no reconocida por GridMind");
        }

        EnergyConsumption ec = new EnergyConsumption();
        // Ajuste: Si power es W, en 10 segundos el consumo es (W / 1000) * (10 / 3600) kWh
        ec.setConsumption((request.power / 1000.0) * (10.0 / 3600.0)); 
        ec.setVoltage(request.voltage);
        ec.setCurrent(request.current);
        ec.setPower(request.power);
        ec.setDevice(device);
        
        energyRepository.save(ec);

        // Alertas y WebSockets
        alertService.checkAndTriggerAlert(device.getUser(), device.getName(), ec.getConsumption(), request.voltage, request.power);
        webSocketService.broadcastEnergyReading(device.getEsp32Id(), ec.getConsumption(), request.voltage, request.current, request.power);
        webSocketService.broadcastForecast(predictiveService.getForecast(device.getUser().getEmail()));

        return ResponseEntity.ok("Telemetría GridMind recibida con éxito");
    }

    // Clase auxiliar para el JSON
    public static class TelemetryRequest {
        public String apiKey;
        public Double voltage;
        public Double current;
        public Double power;
    }

    @PostMapping("/energy/{esp32Id}")
    public ResponseEntity<String> registerIotConsumption(
            @PathVariable("esp32Id") String esp32Id,
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam("consumption") Double consumption,
            @RequestParam(value = "voltage", required = false) Double voltage,
            @RequestParam(value = "current", required = false) Double current,
            @RequestParam(value = "power", required = false) Double power) {
        Device device = deviceRepository.findByEsp32Id(esp32Id).orElse(null);
        if (device == null || !device.getApiKey().equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Acceso denegado: API Key inválida o Hardware desconocido");
        }
        EnergyConsumption ec = new EnergyConsumption();
        ec.setConsumption(consumption);
        ec.setVoltage(voltage);
        ec.setCurrent(current);
        ec.setPower(power);
        ec.setDevice(device);
        energyRepository.save(ec);
        // 🚨 EL VIGILANTE: Revisa si este consumo dispara una alerta
        alertService.checkAndTriggerAlert(device.getUser(), device.getName(), consumption, voltage, power);
        // 📡 Transmitir en vivo a todos los dashboards conectados
        webSocketService.broadcastEnergyReading(esp32Id, consumption, voltage, current, power);
        
        // 🧠 Recalcular y transmitir la predicción IA en vivo
        webSocketService.broadcastForecast(predictiveService.getForecast(device.getUser().getEmail()));

        return ResponseEntity.ok("Lectura de energía guardada con éxito por GridMind");
    }
}

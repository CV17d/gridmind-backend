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
import java.math.BigDecimal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/iot")
@Validated
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
    public ResponseEntity<String> receiveTelemetry(@Valid @RequestBody TelemetryRequest request) {
        // Buscamos el dispositivo por su API Key (la llave secreta)
        Device device = deviceRepository.findByApiKey(request.apiKey).orElse(null);

        if (device == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Error: API Key no reconocida por GridMind");
        }

        EnergyConsumption ec = new EnergyConsumption();
        // Ajuste: Calculamos con BigDecimal para no perder decimales en la DB
        double calc = (request.power / 1000.0) * (10.0 / 3600.0);
        ec.setConsumption(java.math.BigDecimal.valueOf(calc)); 
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
        @NotBlank(message = "El API Key es obligatorio")
        public String apiKey;

        @NotNull(message = "El voltaje es obligatorio")
        @DecimalMin(value = "0.0", message = "El voltaje no puede ser menor a 0")
        @DecimalMax(value = "1000.0", message = "El voltaje no puede ser mayor a 1000")
        public Double voltage;

        @NotNull(message = "La corriente es obligatoria")
        @DecimalMin(value = "0.0", message = "La corriente no puede ser menor a 0")
        @DecimalMax(value = "1000.0", message = "La corriente no puede ser mayor a 1000")
        public Double current;

        @NotNull(message = "La potencia es obligatoria")
        @DecimalMin(value = "0.0", message = "La potencia no puede ser menor a 0")
        @DecimalMax(value = "10000.0", message = "La potencia no puede ser mayor a 10000")
        public Double power;
    }

    @PostMapping("/energy/{esp32Id}")
    public ResponseEntity<String> registerIotConsumption(
            @PathVariable("esp32Id") String esp32Id,
            @RequestHeader("X-API-KEY") String apiKey,
            @RequestParam("consumption") @DecimalMin("0.0") @DecimalMax("10000.0") BigDecimal consumption,
            @RequestParam(value = "voltage", required = false) @DecimalMin("0.0") @DecimalMax("1000.0") Double voltage,
            @RequestParam(value = "current", required = false) @DecimalMin("0.0") @DecimalMax("1000.0") Double current,
            @RequestParam(value = "power", required = false) @DecimalMin("0.0") @DecimalMax("10000.0") Double power) {
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

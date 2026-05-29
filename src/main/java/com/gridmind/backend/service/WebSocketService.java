package com.gridmind.backend.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
@Service
public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    // 📡 Transmite una lectura de energía EN VIVO a todos los que estén escuchando
    public void broadcastEnergyReading(String esp32Id, Object consumption, Double voltage, Double current, Double power) {
        log.debug("Difundiendo lectura de energía para: {}", esp32Id);
        messagingTemplate.convertAndSend("/topic/energy", 
            Map.of(
                "esp32Id", esp32Id,
                "consumption", consumption,
                "voltage", voltage != null ? voltage : 0.0,
                "current", current != null ? current : 0.0,
                "power", power != null ? power : 0.0,
                "timestamp", java.time.LocalDateTime.now().toString()
            )
        );
    }
    // 🚨 Transmite una alerta EN VIVO
    public void broadcastAlert(String message) {
        log.info("Difundiendo alerta en tiempo real: {}", message);
        messagingTemplate.convertAndSend("/topic/alerts", 
            Map.of(
                "type", "HIGH_CONSUMPTION",
                "message", message,
                "timestamp", java.time.LocalDateTime.now().toString()
            )
        );
    }
    // 🧠 Transmite la predicción de la IA
    public void broadcastForecast(Map<String, Object> forecast) {
        log.info("Difundiendo nueva predicción de IA");
        messagingTemplate.convertAndSend("/topic/forecast", forecast);
    }
}

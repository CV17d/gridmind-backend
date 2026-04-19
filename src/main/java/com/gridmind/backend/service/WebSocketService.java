package com.gridmind.backend.service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    // 📡 Transmite una lectura de energía EN VIVO a todos los que estén escuchando
    public void broadcastEnergyReading(String esp32Id, Double consumption) {
        messagingTemplate.convertAndSend("/topic/energy", 
            Map.of(
                "esp32Id", esp32Id,
                "consumption", consumption,
                "timestamp", java.time.LocalDateTime.now().toString()
            )
        );
    }
    // 🚨 Transmite una alerta EN VIVO
    public void broadcastAlert(String message) {
        messagingTemplate.convertAndSend("/topic/alerts", 
            Map.of(
                "type", "HIGH_CONSUMPTION",
                "message", message,
                "timestamp", java.time.LocalDateTime.now().toString()
            )
        );
    }
}

package com.gridmind.backend.service;
import com.gridmind.backend.model.Alert;
import com.gridmind.backend.model.User;
import com.gridmind.backend.repository.AlertRepository;
import com.gridmind.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class AlertService {
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;

    // Umbrales de seguridad
    private static final double DAILY_KWH_THRESHOLD = 10.0;
    private static final double VOLTAGE_MIN = 100.0;
    private static final double VOLTAGE_MAX = 135.0;

    public AlertService(AlertRepository alertRepository, UserRepository userRepository, WebSocketService webSocketService) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.webSocketService = webSocketService;
    }

    public void checkAndTriggerAlert(User user, String deviceName, java.math.BigDecimal consumption, Double voltage, Double power) {
        // 1. Alerta de Consumo Acumulado (Usando el umbral personalizado del usuario)
        if (consumption != null && user.getAlertThreshold() != null && consumption.compareTo(user.getAlertThreshold()) > 0) {
            createAlert(user, "HIGH_CONSUMPTION", "⚠️ Consumo alto en " + deviceName + ": " + consumption + " kWh (Límite: " + user.getAlertThreshold() + ").");
        }

        // 2. Alerta de Anomalía Eléctrica (Voltaje)
        if (voltage != null && (voltage < VOLTAGE_MIN || voltage > VOLTAGE_MAX)) {
            String msg = "⚡ ¡ANOMALÍA DE VOLTAJE! El dispositivo " + deviceName + " registró " + voltage + "V. Riesgo para el hardware.";
            createAlert(user, "VOLTAGE_ANOMALY", msg);
        }

        // 3. Alerta de Pico de Potencia (Ejemplo: > 2000W para un socket común)
        if (power != null && power > 2000.0) {
            createAlert(user, "POWER_SPIKE", "🔥 PICO DE POTENCIA detectado en " + deviceName + ": " + power + "W.");
        }
    }

    private void createAlert(User user, String type, String message) {
        System.out.println("🔔 DISPARANDO ALERTA: " + type + " -> " + message);
        Alert alert = new Alert();
        alert.setUser(user);
        alert.setType(type);
        alert.setMessage(message);
        alertRepository.save(alert);
        
        // Notificar en tiempo real por WebSocket
        System.out.println("📡 Enviando alerta por WebSocket a /topic/alerts...");
        webSocketService.broadcastAlert(message);
    }
    // 📋 Listar todas las alertas del usuario
    public List<Alert> getMyAlerts(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return alertRepository.findByUserOrderByCreatedAtDesc(user);
    }
    // 🔴 Contar alertas no leídas (para el badge)
    public Long countUnread(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return alertRepository.countByUserAndReadFalse(user);
    }
    // ✅ Marcar una alerta como leída
    public void markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        alert.setRead(true);
        alertRepository.save(alert);
    }
}

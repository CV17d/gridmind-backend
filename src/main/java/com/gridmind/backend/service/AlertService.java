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
    // El umbral de consumo diario en kWh (Si pasa de esto, ¡alerta!)
    private static final double DAILY_KWH_THRESHOLD = 10.0;
    public AlertService(AlertRepository alertRepository, UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
    }
    // 🚨 Método que el IotController llamará cada vez que llegue una lectura
    public void checkAndTriggerAlert(User user, Double currentConsumption) {
        if (currentConsumption > DAILY_KWH_THRESHOLD) {
            Alert alert = new Alert();
            alert.setUser(user);
            alert.setType("HIGH_CONSUMPTION");
            alert.setMessage("⚠️ ¡Alerta GridMind! Se detectó un consumo de " 
                + currentConsumption + " kWh, superando tu límite de " 
                + DAILY_KWH_THRESHOLD + " kWh. Revisa tus dispositivos conectados.");
            alertRepository.save(alert);
        }
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

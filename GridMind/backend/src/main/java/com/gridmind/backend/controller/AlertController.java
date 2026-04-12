package com.gridmind.backend.controller;
import com.gridmind.backend.model.Alert;
import com.gridmind.backend.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {
    private final AlertService alertService;
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }
    // 📋 Ver todas mis alertas
    @GetMapping
    public ResponseEntity<List<Alert>> getMyAlerts(Authentication authentication) {
        return ResponseEntity.ok(alertService.getMyAlerts(authentication.getName()));
    }
    // 🔴 ¿Cuántas alertas sin leer tengo?
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long count = alertService.countUnread(authentication.getName());
        return ResponseEntity.ok(Map.of("unreadAlerts", count));
    }
    // ✅ Marcar como leída
    @PatchMapping("/{alertId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable("alertId") Long alertId) {
        alertService.markAsRead(alertId);
        return ResponseEntity.ok("Alerta marcada como leída");
    }
}

package com.gridmind.backend.controller;
import com.gridmind.backend.dto.DailyConsumptionDTO;
import com.gridmind.backend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.gridmind.backend.service.PredictiveService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final PredictiveService predictiveService;

    public AnalyticsController(AnalyticsService analyticsService, PredictiveService predictiveService) {
        this.analyticsService = analyticsService;
        this.predictiveService = predictiveService;
    }

    // 📈 La joya de la corona visual
    @GetMapping("/daily")
    public ResponseEntity<List<DailyConsumptionDTO>> getDailyChart(Authentication authentication) {
        String email = authentication.getName();
        List<DailyConsumptionDTO> chartData = analyticsService.getMyDailyChartData(email);
        return ResponseEntity.ok(chartData);
    }

    // 🧠 Predicción IA
    @GetMapping("/forecast")
    public ResponseEntity<Map<String, Object>> getForecast(Authentication authentication) {
        System.out.println("🛰️ CONTROLADOR: Petición de predicción recibida desde el frontend.");
        String email = authentication.getName();
        return ResponseEntity.ok(predictiveService.getForecast(email));
    }

    // ⚖️ Comparativa de Factura
    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getComparison(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(analyticsService.getBillComparison(email));
    }
}

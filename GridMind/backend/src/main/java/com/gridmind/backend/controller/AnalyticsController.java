package com.gridmind.backend.controller;
import com.gridmind.backend.dto.DailyConsumptionDTO;
import com.gridmind.backend.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    // 📈 La joya de la corona visual
    @GetMapping("/daily")
    public ResponseEntity<List<DailyConsumptionDTO>> getDailyChart(Authentication authentication) {
        
        // Atrapamos la sesión mágica del guardia de seguridad
        String email = authentication.getName();
        
        List<DailyConsumptionDTO> chartData = analyticsService.getMyDailyChartData(email);
        return ResponseEntity.ok(chartData);
    }
}

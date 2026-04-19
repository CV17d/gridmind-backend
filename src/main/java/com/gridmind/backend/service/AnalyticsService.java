package com.gridmind.backend.service;
import com.gridmind.backend.dto.DailyConsumptionDTO;
import com.gridmind.backend.model.User;
import com.gridmind.backend.repository.EnergyConsumptionRepository;
import com.gridmind.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class AnalyticsService {
    
    // Inyectamos tus dos repositorios como a ti te gusta
    private final EnergyConsumptionRepository consumptionRepository;
    private final UserRepository userRepository;
    public AnalyticsService(EnergyConsumptionRepository consumptionRepository, UserRepository userRepository) {
        this.consumptionRepository = consumptionRepository;
        this.userRepository = userRepository;
    }
    // El método central que le dará vida a la gráfica de "Consumo Semanal"
    public List<DailyConsumptionDTO> getMyDailyChartData(String email) {
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Fraude estadístico evitado: Usuario extinto."));
            
        return consumptionRepository.findDailyConsumptionByUserId(user.getId());
    }
}

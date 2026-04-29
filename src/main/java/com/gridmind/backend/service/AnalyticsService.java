package com.gridmind.backend.service;

import com.gridmind.backend.dto.DailyConsumptionDTO;
import com.gridmind.backend.model.User;
import com.gridmind.backend.model.EnergyBill;
import com.gridmind.backend.repository.EnergyConsumptionRepository;
import com.gridmind.backend.repository.UserRepository;
import com.gridmind.backend.repository.EnergyBillRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnalyticsService {
    
    private final EnergyConsumptionRepository consumptionRepository;
    private final UserRepository userRepository;
    private final EnergyBillRepository billRepository;

    public AnalyticsService(EnergyConsumptionRepository consumptionRepository, 
                            UserRepository userRepository,
                            EnergyBillRepository billRepository) {
        this.consumptionRepository = consumptionRepository;
        this.userRepository = userRepository;
        this.billRepository = billRepository;
    }

    public Map<String, Object> getBillComparison(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        List<EnergyBill> bills = billRepository.findByUserOrderByUploadedAtDesc(user);
        
        Map<String, Object> result = new HashMap<>();
        if (bills.isEmpty()) {
            result.put("status", "NO_BILLS");
            return result;
        }

        EnergyBill latestBill = bills.get(0);
        Double totalMeasured = consumptionRepository.sumConsumptionByUserId(user.getId());

        result.put("status", "SUCCESS");
        result.put("billKwh", latestBill.getTotalKwh());
        result.put("measuredKwh", totalMeasured != null ? totalMeasured : 0.0);
        result.put("advice", latestBill.getAiRecommendations());
        result.put("uploadedAt", latestBill.getUploadedAt());
        
        return result;
    }

    public List<DailyConsumptionDTO> getMyDailyChartData(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        return consumptionRepository.findDailyConsumptionByUserId(user.getId());
    }
}

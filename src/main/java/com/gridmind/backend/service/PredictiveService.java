package com.gridmind.backend.service;

import com.gridmind.backend.model.EnergyConsumption;
import com.gridmind.backend.repository.EnergyConsumptionRepository;
import com.gridmind.backend.model.User;
import com.gridmind.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PredictiveService {

    private final EnergyConsumptionRepository energyRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    
    @org.springframework.beans.factory.annotation.Value("${ia.service.url:http://localhost:5000/predict}")
    private String iaServiceUrl;

    public PredictiveService(EnergyConsumptionRepository energyRepository, UserRepository userRepository) {
        this.energyRepository = energyRepository;
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> getForecast(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Obtener historial (últimas 1000 lecturas para el modelo)
        List<EnergyConsumption> history = energyRepository.findHistoryForPrediction(user.getId());

        if (history.isEmpty()) {
            System.out.println("🧠 IA INFO: El historial para " + email + " está vacío.");
            return Map.of("error", "No hay suficiente historial para predecir");
        }
        
        System.out.println("🧠 IA INFO: Enviando " + history.size() + " registros a la IA para " + email);

        // 2. Formatear datos para la IA con AGREGACIÓN POR 5 MINUTOS (PROMEDIO), FILTRO y ESCALADO
        // Agrupamos por 5 minutos para ver tendencias de largo plazo y cubrir más horas de historia
        Map<String, List<java.math.BigDecimal>> grouped = new LinkedHashMap<>();
        for (EnergyConsumption ec : history) {
            if (ec.getConsumption() == null || ec.getConsumption().compareTo(new java.math.BigDecimal("0.01")) >= 0) continue;
            
            // Truncar a bloques de 5 minutos
            int minutes = ec.getTimestamp().getMinute();
            int truncatedMinutes = (minutes / 5) * 5;
            String key = ec.getTimestamp().withMinute(truncatedMinutes).withSecond(0).withNano(0).toString();
            
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(ec.getConsumption());
        }

        List<Map<String, Object>> formattedHistory = grouped.entrySet().stream().map(entry -> {
            java.math.BigDecimal avg = entry.getValue().stream()
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                .divide(new java.math.BigDecimal(entry.getValue().size()), java.math.MathContext.DECIMAL128);
            
            Map<String, Object> map = new HashMap<>();
            map.put("timestamp", entry.getKey()); 
            map.put("consumption", avg.multiply(new java.math.BigDecimal("1000")));
            return map;
        }).collect(Collectors.toList());

        System.out.println("🧠 IA INFO: Enviando " + formattedHistory.size() + " puntos promedio (5 min) a la IA.");

        // 3. Llamar al microservicio de Python
        Map<String, Object> request = new HashMap<>();
        request.put("history", formattedHistory);

        try {
            System.out.println("🧠 IA: Solicitando predicción con agregación por minuto...");
            
            Map<String, Object> response = restTemplate.postForObject(iaServiceUrl, request, Map.class);
            System.out.println("🧠 IA RAW RESPONSE: " + response);
            
            // NORMALIZACIÓN
            Map<String, Object> normalizedResponse = new HashMap<>(response);
            if (response.containsKey("predicted_next_30_days")) {
                Object rawValue = response.get("predicted_next_30_days");
                double value = (rawValue instanceof Number) ? ((Number) rawValue).doubleValue() : 0.0;
                // La IA predice en la escala enviada (Wh). Convertimos a kWh.
                normalizedResponse.put("prediction", value / 1000.0);
            } else {
                normalizedResponse.put("prediction", 0.0);
            }
            
            return normalizedResponse;
        } catch (Exception e) {
            System.err.println("❌ IA ERROR: No se pudo conectar con el microservicio en " + iaServiceUrl + ". Error: " + e.getMessage());
            return Map.of("error", "El servicio de IA no responde.");
        }
    }
}

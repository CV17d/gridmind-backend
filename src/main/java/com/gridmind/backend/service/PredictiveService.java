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

        // 2. Formatear datos para la IA con FILTRO DE SANIDAD EXTREMO y ESCALADO (x1000)
        // Escalamos a Wh para que la IA no trabaje con floats tan pequeños que causen inestabilidad (0.0)
        List<Map<String, Object>> formattedHistory = history.stream()
            .filter(ec -> ec.getConsumption() != null && ec.getConsumption().compareTo(new java.math.BigDecimal("0.01")) < 0)
            .map(ec -> {
                Map<String, Object> map = new HashMap<>();
                map.put("timestamp", ec.getTimestamp().toString()); 
                // Enviamos en Wh (x1000) para mayor estabilidad numérica en el modelo de IA
                map.put("consumption", ec.getConsumption().multiply(new java.math.BigDecimal("1000")));
                return map;
            }).collect(Collectors.toList());

        // 3. Llamar al microservicio de Python
        Map<String, Object> request = new HashMap<>();
        request.put("history", formattedHistory);

        try {
            System.out.println("🧠 IA: Solicitando predicción con escalado x1000 para estabilidad...");
            
            Map<String, Object> response = restTemplate.postForObject(iaServiceUrl, request, Map.class);
            System.out.println("🧠 IA RAW RESPONSE: " + response);
            
            // NORMALIZACIÓN: El microservicio devuelve la predicción en la misma escala enviada.
            // Si enviamos Wh, devuelve Wh. Dividimos por 1000 para volver a kWh.
            Map<String, Object> normalizedResponse = new HashMap<>(response);
            if (response.containsKey("predicted_next_30_days")) {
                Object rawValue = response.get("predicted_next_30_days");
                double value = (rawValue instanceof Number) ? ((Number) rawValue).doubleValue() : 0.0;
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

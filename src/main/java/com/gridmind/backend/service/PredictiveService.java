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

        // 2. Formatear datos para la IA con FILTRO DE SANIDAD EXTREMO
        // Filtramos valores > 0.01 kWh (10 Wh) en una sola lectura de segundos para eliminar ruido legacy
        List<Map<String, Object>> formattedHistory = history.stream()
            .filter(ec -> ec.getConsumption() != null && ec.getConsumption().compareTo(new java.math.BigDecimal("0.01")) < 0)
            .map(ec -> {
                Map<String, Object> map = new HashMap<>();
                map.put("timestamp", ec.getTimestamp().toString()); 
                map.put("consumption", ec.getConsumption());
                return map;
            }).collect(Collectors.toList());

        // 3. Llamar al microservicio de Python
        Map<String, Object> request = new HashMap<>();
        request.put("history", formattedHistory);

        try {
            System.out.println("🧠 IA: Solicitando predicción al microservicio de Python en: " + iaServiceUrl);
            // Logueamos una muestra de los datos enviados para verificar precisión
            if (!formattedHistory.isEmpty()) {
                System.out.println("🧠 IA DATA SAMPLE: " + formattedHistory.get(formattedHistory.size()-1));
            }
            
            Map<String, Object> response = restTemplate.postForObject(iaServiceUrl, request, Map.class);
            System.out.println("🧠 IA RESPONSE: " + response);
            
            return response;
        } catch (Exception e) {
            System.err.println("❌ IA ERROR: No se pudo conectar con el microservicio en " + iaServiceUrl + ". Error: " + e.getMessage());
            return Map.of("error", "El servicio de IA no responde.");
        }
    }
}

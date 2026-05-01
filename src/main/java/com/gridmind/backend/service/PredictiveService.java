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

        // 1. Obtener historial (últimas 100 lecturas para el modelo)
        List<EnergyConsumption> history = energyRepository.findTop100ByUserOrderByTimestampAsc(user.getId());

        if (history.isEmpty()) {
            System.out.println("🧠 IA INFO: El historial para " + email + " está vacío.");
            return Map.of("error", "No hay suficiente historial para predecir");
        }
        
        System.out.println("🧠 IA INFO: Enviando " + history.size() + " registros a la IA para " + email);

        // 2. Formatear datos para la IA
        List<Map<String, Object>> formattedHistory = history.stream().map(ec -> {
            Map<String, Object> map = new HashMap<>();
            // Formato ISO-8601 estándar para que Python lo entienda perfecto
            map.put("timestamp", ec.getTimestamp().toString()); 
            map.put("consumption", ec.getConsumption());
            return map;
        }).collect(Collectors.toList());

        // 3. Llamar al microservicio de Python
        Map<String, Object> request = new HashMap<>();
        request.put("history", formattedHistory);

        try {
            System.out.println("🧠 IA: Solicitando predicción al microservicio de Python en: " + iaServiceUrl);
            Map<String, Object> response = restTemplate.postForObject(iaServiceUrl, request, Map.class);
            System.out.println("🧠 IA: Predicción recibida con éxito.");
            return response;
        } catch (Exception e) {
            System.err.println("❌ IA ERROR: No se pudo conectar con el microservicio en " + iaServiceUrl);
            return Map.of("error", "El servicio de IA no responde.");
        }
    }
}

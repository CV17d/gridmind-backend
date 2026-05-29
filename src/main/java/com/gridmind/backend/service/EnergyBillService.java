package com.gridmind.backend.service;
import com.gridmind.backend.model.EnergyBill;
import com.gridmind.backend.model.User;
import com.gridmind.backend.repository.EnergyBillRepository;
import com.gridmind.backend.repository.UserRepository;
import com.gridmind.backend.exception.ResourceNotFoundException;
import com.gridmind.backend.exception.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.*;
import java.math.BigDecimal;
import java.util.*;

@Service
public class EnergyBillService {

    private static final Logger log = LoggerFactory.getLogger(EnergyBillService.class);

    private final EnergyBillRepository billRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.upload.dir:uploads/bills/}")
    private String uploadDir;

    // Spring inyecta la llave secreta que escribiste en tu application.yaml
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent}")
    private String geminiApiUrl;

    public EnergyBillService(EnergyBillRepository billRepository, UserRepository userRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    // 📥 1. Método Principal que atrapa la Foto
    public EnergyBill analyzeAndSaveBill(MultipartFile file, String userEmail) throws Exception {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        log.info("Iniciando análisis de factura para el usuario: {}", userEmail);

        // A) Guardar foto físicamente
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) { Files.createDirectories(uploadPath); }
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Archivo guardado en: {}. Solicitando análisis a Gemini...", filePath);
        JsonNode iaResponse = callGeminiVisionAPI(filePath);

        // C) Guardar Factura Final
        EnergyBill bill = new EnergyBill();
        bill.setUser(user);
        bill.setFileUrl(filePath.toString());
        bill.setTotalKwh(new BigDecimal(iaResponse.get("totalKwh").asText()));
        bill.setTotalAmount(new BigDecimal(iaResponse.get("totalAmount").asText()));
        bill.setAiRecommendations(iaResponse.get("advice").asText());

        // D) Auto-guardar tarifa eléctrica extraída por Gemini
        JsonNode rateNode = iaResponse.get("electricityRate");
        if (rateNode != null && !rateNode.isNull()) {
            user.setElectricityRate(new BigDecimal(rateNode.asText()));
            userRepository.save(user);
        }

        log.info("Factura analizada y guardada exitosamente para {}", userEmail);
        return billRepository.save(bill);
    }

    // 🧠 2. El puente directo con Google Gemini 1.5
    private JsonNode callGeminiVisionAPI(Path imagePath) throws Exception {
        String apiUrl = geminiApiUrl + "?key=" + geminiApiKey;
        byte[] imageBytes = Files.readAllBytes(imagePath);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String prompt = "Actúa como un asesor experto en facturas de energía eléctrica. Extrae de esta imagen: el total de kWh consumidos, el monto total de la factura, y la tarifa eléctrica aplicada. Devuélveme ÚNICA Y EXCLUSIVAMENTE un JSON válido con 4 llaves: \"totalKwh\" (decimal), \"totalAmount\" (decimal), \"electricityRate\" (decimal), y \"advice\" (string). No uses markdown.";

        Map<String, Object> inlineData = Map.of("mime_type", "image/jpeg", "data", base64Image);
        Map<String, Object> parts = Map.of("contents", List.of(Map.of("parts", List.of(
            Map.of("text", prompt),
            Map.of("inline_data", inlineData)
        ))));

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(apiUrl, parts, JsonNode.class);
        String rawText = response.getBody().path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        String cleanJson = rawText.replace("```json", "").replace("```", "").trim();
        return objectMapper.readTree(cleanJson);
    }
    // 📋 3. Obtener el historial
    public List<EnergyBill> getUserBills(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return billRepository.findByUserOrderByUploadedAtDesc(user);
    }

    // 🖼️ 4. Obtener la imagen fotográfica de la factura
    public byte[] getBillImageAsBytes(Long billId, String userEmail) throws Exception {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
        EnergyBill bill = billRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
            
        if (!bill.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("No tienes permiso para ver la foto de esta factura");
        }
        
        Path imagePath = Paths.get(bill.getFileUrl());
        if (!Files.exists(imagePath)) {
            throw new ResourceNotFoundException("El archivo físico de la foto ya no existe en el servidor");
        }
        
        return Files.readAllBytes(imagePath);
    }
}

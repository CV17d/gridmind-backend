package com.gridmind.backend.service;
import com.gridmind.backend.model.EnergyBill;
import com.gridmind.backend.model.User;
import com.gridmind.backend.repository.EnergyBillRepository;
import com.gridmind.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.*;
import java.util.*;
@Service
public class EnergyBillService {
    private final EnergyBillRepository billRepository;
    private final UserRepository userRepository;
    
    private final String UPLOAD_DIR = "uploads/bills/";
    // Spring inyecta la llave secreta que escribiste en tu application.yaml
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EnergyBillService(EnergyBillRepository billRepository, UserRepository userRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
    }
    // 📥 1. Método Principal que atrapa la Foto
    public EnergyBill analyzeAndSaveBill(MultipartFile file, String userEmail) throws Exception {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        // A) Guardar foto físicamente
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) { Files.createDirectories(uploadPath); }
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // B) Hablar con la Inteligencia Artificial REAL
        JsonNode iaResponse = callGeminiVisionAPI(filePath);

        // C) Guardar Factura Final
        EnergyBill bill = new EnergyBill();
        bill.setUser(user);
        bill.setFileUrl(filePath.toString());
        bill.setTotalKwh(iaResponse.get("totalKwh").asDouble());
        bill.setTotalAmount(iaResponse.get("totalAmount").asDouble());
        bill.setAiRecommendations(iaResponse.get("advice").asText());

        // D) Auto-guardar tarifa eléctrica extraída por Gemini
        JsonNode rateNode = iaResponse.get("electricityRate");
        if (rateNode != null && !rateNode.isNull() && rateNode.asDouble() > 0) {
            user.setElectricityRate(rateNode.asDouble());
            userRepository.save(user);
        }

        return billRepository.save(bill);
    }

    // 🧠 2. El puente directo con Google Gemini 1.5
    private JsonNode callGeminiVisionAPI(Path imagePath) throws Exception {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + geminiApiKey;
        byte[] imageBytes = Files.readAllBytes(imagePath);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String prompt = "Actúa como un asesor experto en facturas de energía eléctrica. Extrae de esta imagen: el total de kWh consumidos, el monto total de la factura, y la tarifa eléctrica aplicada. Devuélveme ÚNICA Y EXCLUSIVAMENTE un JSON válido con 4 llaves: \"totalKwh\" (decimal), \"totalAmount\" (decimal), \"electricityRate\" (decimal), y \"advice\" (string). No uses markdown.";

        Map<String, Object> inlineData = Map.of("mime_type", "image/jpeg", "data", base64Image);
        Map<String, Object> parts = Map.of("contents", List.of(Map.of("parts", List.of(
            Map.of("text", prompt),
            Map.of("inline_data", inlineData)
        ))));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(apiUrl, parts, JsonNode.class);
        String rawText = response.getBody().path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        String cleanJson = rawText.replace("```json", "").replace("```", "").trim();
        return new ObjectMapper().readTree(cleanJson);
    }
    // 📋 3. Obtener el historial
    public List<EnergyBill> getUserBills(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return billRepository.findByUserOrderByUploadedAtDesc(user);
    }

    // 🖼️ 4. Obtener la imagen fotográfica de la factura
    public byte[] getBillImageAsBytes(Long billId, String userEmail) throws Exception {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
        EnergyBill bill = billRepository.findById(billId)
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
            
        if (!bill.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Acceso denegado: No tienes permiso para ver la foto de esta factura");
        }
        
        Path imagePath = Paths.get(bill.getFileUrl());
        if (!Files.exists(imagePath)) {
            throw new RuntimeException("El archivo físico de la foto ya no existe en el servidor");
        }
        
        return Files.readAllBytes(imagePath);
    }
}

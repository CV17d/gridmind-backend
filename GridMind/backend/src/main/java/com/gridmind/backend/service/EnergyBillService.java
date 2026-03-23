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
        return billRepository.save(bill);
    }
    // 🧠 2. El puente directo con Google Gemini 1.5
    private JsonNode callGeminiVisionAPI(Path imagePath) throws Exception {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + geminiApiKey;
        // Convertir la imagen pesada a un formato de texto largo (Base64) que Google pueda entender
        byte[] imageBytes = Files.readAllBytes(imagePath);
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        // El 'Prompt' estricto: Le exigimos un JSON puro sin adornos
        String prompt = "Actúa como un asesor experto en facturas de energía eléctrica. " +
                "Extrae de esta imagen el total de kWh consumidos y el total de la factura a pagar. " +
                "Genera además un consejo breve y amigable sobre cómo optimizar energía, sugiriendo exactamente en dónde " +
                "conectar nuestro 'Enchufe Inteligente' o 'Toma de Bombilla' basándote en la factura. " +
                "Prohibido agregar formato o backticks. Devuélveme ÚNICA Y EXCLUSIVAMENTE un JSON válido con 3 llaves estables: " +
                "\"totalKwh\" (número decimal), \"totalAmount\" (número decimal), y \"advice\" (texto string).";
        // Organizar los mapas anidados tal cual lo exige el manual de Google AI
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mime_type", "image/jpeg");
        inlineData.put("data", base64Image);
        Map<String, Object> imagePart = new HashMap<>();
        imagePart.put("inline_data", inlineData);
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        Map<String, Object> partsObj = new HashMap<>();
        partsObj.put("parts", Arrays.asList(textPart, imagePart));
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", Collections.singletonList(partsObj));
        // Enviar la petición por la red neuronal
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
        // Diseccionar la respuesta de Gemini (Llegar al centro del pastel)
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String geminiRawText = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        
        // Limpiamos la basura extra (como los caracteres ```json de formato invisible que la IA a veces incluye)
        String cleanJson = geminiRawText.replace("```json", "").replace("```", "").trim();
        System.out.println("🤖 Respuesta de la IA limpia: \n" + cleanJson);
        return mapper.readTree(cleanJson);
    }
    // 📋 3. Obtener el historial
    public List<EnergyBill> getUserBills(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return billRepository.findByUserOrderByUploadedAtDesc(user);
    }
}

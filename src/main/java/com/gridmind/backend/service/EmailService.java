package com.gridmind.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.resend.api-key}")
    private String resendApiKey;

    @Value("${app.resend.from-email}")
    private String fromEmail;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    public void sendPasswordResetEmail(String toEmail, String token) {
        if (resendApiKey == null || resendApiKey.trim().isEmpty()) {
            System.err.println("SKIPPING EMAIL: Resend API Key is not configured. Reset token for " + toEmail + " is: " + token);
            return;
        }

        String resetLink = frontendUrl + "/reset-password/" + token;
        
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #0d1219; color: #ffffff; padding: 40px; border-radius: 12px; border: 1px solid #1a2235;'>" +
                "<div style='text-align: center; margin-bottom: 24px;'>" +
                "<h1 style='color: #3cecb0; margin: 0;'>GridMind</h1>" +
                "<p style='color: #8b9bb4; font-size: 14px; margin-top: 4px;'>Protocolo de Recuperación de Acceso</p>" +
                "</div>" +
                "<p style='color: #ffffff; font-size: 16px; line-height: 1.6;'>Hemos recibido una solicitud para cambiar tu contraseña maestra de GridMind.</p>" +
                "<p style='color: #ffffff; font-size: 16px; line-height: 1.6;'>Haz clic en el botón inferior para establecer una nueva contraseña de manera segura. Este enlace expira en 15 minutos.</p>" +
                "<div style='text-align: center; margin: 40px 0;'>" +
                "<a href='" + resetLink + "' style='background-color: #3cecb0; color: #000000; padding: 14px 28px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px; display: inline-block;'>Restablecer Contraseña</a>" +
                "</div>" +
                "<p style='color: #8b9bb4; font-size: 12px; text-align: center; border-top: 1px solid #1a2235; padding-top: 20px;'>Si no solicitaste este cambio, puedes ignorar este correo de forma segura.</p>" +
                "</div>";

        // Preparar la petición para Resend
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", fromEmail);
        body.put("to", List.of(toEmail));
        body.put("subject", "GridMind: Restablecimiento de contraseña");
        body.put("html", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(RESEND_API_URL, request, Map.class);
        } catch (Exception e) {
            System.err.println("Error enviando email vía Resend: " + e.getMessage());
            throw new RuntimeException("Error al enviar el correo electrónico vía API.", e);
        }
    }
}

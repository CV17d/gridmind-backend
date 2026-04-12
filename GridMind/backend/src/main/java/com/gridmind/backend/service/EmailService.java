package com.gridmind.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
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

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("GridMind: Restablecimiento de contraseña");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo electrónico.", e);
        }
    }
}

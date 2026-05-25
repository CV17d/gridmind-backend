package com.gridmind.backend.service;

import com.gridmind.backend.dto.RegisterUserRequest;
import com.gridmind.backend.model.User;
import com.gridmind.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User registerUser(RegisterUserRequest request) {

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElse(null);
    }

    public String processForgotPassword(String email) {
        User user = findByEmail(email);
        if (user == null) {
            System.out.println("No se enviará correo de recuperación: Usuario no registrado (" + email + ")");
            return null; // Graceful exist to prevent user enumeration without throwing 500
        }

        java.security.SecureRandom secureRandom = new java.security.SecureRandom();
        byte[] tokenBytes = new byte[128]; // 1024 bits de entropía
        secureRandom.nextBytes(tokenBytes);
        String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        user.setResetToken(token);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Envío real del correo
        emailService.sendPasswordResetEmail(email, token);

        return token;
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o expirado."));

        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
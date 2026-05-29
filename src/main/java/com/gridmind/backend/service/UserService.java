package com.gridmind.backend.service;

import com.gridmind.backend.dto.RegisterUserRequest;
import com.gridmind.backend.model.User;
import com.gridmind.backend.repository.UserRepository;
import com.gridmind.backend.exception.ResourceNotFoundException;
import com.gridmind.backend.exception.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

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
            log.warn("Intento de recuperación de contraseña para correo no registrado: {}", email);
            return null; // Graceful exist to prevent user enumeration without throwing 500
        }

        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Envío real del correo
        emailService.sendPasswordResetEmail(email, token);

        return token;
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido o expirado."));

        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new AccessDeniedException("El token ha expirado.");
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
package com.gridmind.backend.controller;

import com.gridmind.backend.dto.RegisterUserRequest;
import com.gridmind.backend.dto.LoginRequest;
import com.gridmind.backend.dto.LoginResponse;
import com.gridmind.backend.dto.ForgotPasswordRequest;
import com.gridmind.backend.dto.ResetPasswordRequest;
import com.gridmind.backend.dto.UpdateSettingsRequest;
import com.gridmind.backend.dto.ChangePasswordRequest;
import com.gridmind.backend.model.User;
import com.gridmind.backend.service.UserService;
import com.gridmind.backend.security.JwtService;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterUserRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        User user = userService.findByEmail(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(token, user.getName());
    }

    @GetMapping("/me")
    public User getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        return userService.findByEmail(email);
    }

    @PostMapping("/forgot-password")
    public java.util.Map<String, String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.processForgotPassword(request.getEmail());
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Si el correo está registrado, se enviarán las instrucciones para restablecer tu cuenta de GridMind.");
        return response;
    }

    @PostMapping("/reset-password")
    public java.util.Map<String, String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Password successfully reset.");
        return response;
    }

    @PutMapping("/settings")
    public java.util.Map<String, String> updateSettings(
            @RequestBody UpdateSettingsRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getElectricityRate() != null) {
            user.setElectricityRate(request.getElectricityRate());
        }
        if (request.getAlertThreshold() != null) {
            user.setAlertThreshold(request.getAlertThreshold());
        }
        userService.save(user);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Ajustes actualizados correctamente.");
        return response;
    }

    @PutMapping("/change-password")
    public java.util.Map<String, String> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.save(user);
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Contraseña actualizada correctamente.");
        return response;
    }
}
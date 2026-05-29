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

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public User register(@Valid @RequestBody RegisterUserRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {

        User user = userService.findByEmail(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/users/refresh-token");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 dias
        response.addCookie(cookie);

        return new LoginResponse(accessToken, user.getName());
    }

    @PostMapping("/refresh-token")
    public LoginResponse refreshToken(HttpServletRequest request) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || !jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }

        String email = jwtService.extractUsername(refreshToken);
        User user = userService.findByEmail(email);
        
        if (user == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        String newAccessToken = jwtService.generateToken(user.getEmail());
        return new LoginResponse(newAccessToken, user.getName());
    }

    @GetMapping("/me")
    public User getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        return userService.findByEmail(email);
    }

    @PostMapping("/forgot-password")
    public java.util.Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.processForgotPassword(request.getEmail());
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Si el correo está registrado, se enviarán las instrucciones para restablecer tu cuenta de GridMind.");
        return response;
    }

    @PostMapping("/reset-password")
    public java.util.Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Password successfully reset.");
        return response;
    }

    @PutMapping("/settings")
    public java.util.Map<String, String> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request,
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
            @Valid @RequestBody ChangePasswordRequest request,
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
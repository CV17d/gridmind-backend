package com.gridmind.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class IotApiKeyFilter extends OncePerRequestFilter {

    @Value("${iot.api.key:gridmind_iot_secret_2026}")
    private String expectedApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Solo aplicamos este filtro a las rutas de IoT
        if (path.startsWith("/api/v1/iot")) {
            
            // Permitir peticiones OPTIONS (CORS preflight) sin API Key
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }

            String apiKey = request.getHeader("X-IoT-API-Key");

            // Validar la API Key
            if (expectedApiKey.equals(apiKey)) {
                // Le decimos a Spring Security que esta petición está autorizada como "dispositivo IoT"
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "iot-device", null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // Si no trae llave o es incorrecta, bloqueamos la petición
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"API Key de IoT invalida o ausente. Usa el header X-IoT-API-Key.\"}");
                return;
            }
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}

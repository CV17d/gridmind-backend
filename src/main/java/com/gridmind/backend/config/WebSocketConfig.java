package com.gridmind.backend.config;

import com.gridmind.backend.security.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;

    public WebSocketConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // "/topic" es el canal donde el servidor EMPUJA datos hacia el Frontend
        config.enableSimpleBroker("/topic");
        // "/app" es por donde el Frontend ENVÍA mensajes al servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Configuramos la URL de conexión de WebSockets con CORS seguro
        registry.addEndpoint("/api/v1/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:5173",
                        "http://localhost:5174",
                        "http://localhost:5175",
                        "http://localhost:3000",
                        "https://gridmind-on.vercel.app",
                        "https://gridmind.lat",
                        "https://app.gridmind.lat",
                        "https://www.gridmind.lat"
                );
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Interceptamos la conexión STOMP para exigir un Token JWT
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                // Si la petición es de tipo CONNECT (Intento de conectar al WebSocket)
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            if (jwtService.validateToken(token)) {
                                String username = jwtService.extractUsername(token);
                                // Token válido: Autenticamos al usuario en la sesión de WebSockets
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        username, null, Collections.emptyList());
                                accessor.setUser(auth);
                                return message;
                            }
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Token JWT invalido para WebSocket");
                        }
                    }
                    // Si no tiene token o es inválido, rechazamos la conexión
                    throw new IllegalArgumentException("No autorizado. Falta el token JWT en la cabecera Authorization.");
                }
                return message;
            }
        });
    }
}

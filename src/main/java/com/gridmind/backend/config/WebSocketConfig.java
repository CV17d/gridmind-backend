package com.gridmind.backend.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // "/topic" es el canal donde el servidor EMPUJA datos hacia el Frontend
        config.enableSimpleBroker("/topic");
        // "/app" es por donde el Frontend ENVÍA mensajes al servidor (si lo necesita)
        config.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Esta es la URL donde el Frontend se "conecta" al WebSocket
        registry.addEndpoint("/api/v1/ws")
                .setAllowedOriginPatterns("*"); // Permite conexiones desde cualquier dominio
    }
}

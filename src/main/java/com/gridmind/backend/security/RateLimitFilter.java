package com.gridmind.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter — Sliding Window per IP.
 *
 * Protects sensitive public endpoints against brute-force and
 * credential-stuffing attacks by tracking request timestamps per
 * IP address and rejecting requests that exceed the configured
 * threshold within the time window.
 *
 * Limits:
 *   - /login          → 10 requests / 60 s
 *   - /register       → 10 requests / 60 s
 *   - /forgot-password → 5 requests / 60 s
 *   - /reset-password  → 5 requests / 60 s
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // ── Endpoints sujetos a rate limiting ───────────────────────────────────
    private static final Set<String> STRICT_PATHS = Set.of(
            "/api/v1/users/forgot-password",
            "/api/v1/users/reset-password"
    );

    private static final Set<String> NORMAL_PATHS = Set.of(
            "/api/v1/users/login",
            "/api/v1/users/register"
    );

    // ── Parámetros de la ventana deslizante ─────────────────────────────────
    private static final long  WINDOW_MILLIS  = 60_000L; // 60 segundos
    private static final int   NORMAL_LIMIT   = 10;       // intentos por ventana
    private static final int   STRICT_LIMIT   = 5;        // intentos por ventana

    // ── Estado en memoria: IP → cola de timestamps ──────────────────────────
    // ConcurrentHashMap para thread-safety; Deque para la cola de timestamps.
    private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path   = request.getRequestURI();
        String method = request.getMethod();

        // Solo aplica a POST en rutas protegidas
        if (!"POST".equalsIgnoreCase(method)
                || (!STRICT_PATHS.contains(path) && !NORMAL_PATHS.contains(path))) {
            filterChain.doFilter(request, response);
            return;
        }

        int limit = STRICT_PATHS.contains(path) ? STRICT_LIMIT : NORMAL_LIMIT;
        String ip = resolveClientIp(request);
        String key = ip + ":" + path;

        if (isRateLimited(key, limit)) {
            sendRateLimitResponse(response, ip, path);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Registra la petición en la ventana deslizante y decide si está limitada.
     * Sincronizado por clave para evitar condiciones de carrera en la misma IP.
     */
    private boolean isRateLimited(String key, int limit) {
        long now = Instant.now().toEpochMilli();

        // computeIfAbsent garantiza que la cola existe antes del bloque sync
        Deque<Long> timestamps = requestLog.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // Eliminar timestamps fuera de la ventana
            while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > WINDOW_MILLIS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= limit) {
                return true; // ← bloqueado
            }

            timestamps.addLast(now);
            return false;
        }
    }

    /**
     * Extrae la IP real del cliente respetando proxies (Railway, Vercel, Nginx).
     * Si hay varios valores en X-Forwarded-For, toma el primero (origen real).
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    /** Responde con 429 Too Many Requests y cuerpo JSON. */
    private void sendRateLimitResponse(HttpServletResponse response,
                                       String ip, String path)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"error\":\"Too Many Requests\"," +
                "\"message\":\"Demasiados intentos. Por favor espera 1 minuto antes de volver a intentarlo.\"}"
        );
        logger.warn(String.format("[RateLimit] IP bloqueada: %s → %s", ip, path));
    }
}

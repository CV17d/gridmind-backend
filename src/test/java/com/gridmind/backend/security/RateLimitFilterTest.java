package com.gridmind.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RateLimitFilterTest {

    @Test
    public void testRateLimitBlocksAfterLimitReached() throws ServletException, IOException {
        RateLimitFilter filter = new RateLimitFilter();

        // Simulate 10 requests to /api/v1/users/login (Limit is 10)
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users/login");
            request.setRemoteAddr("192.168.1.1");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = (req, res) -> {};
            
            filter.doFilterInternal(request, response, chain);
            assertEquals(200, response.getStatus(), "Las primeras 10 peticiones deben pasar (HTTP 200)");
        }

        // 11th request should be blocked
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users/login");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {};
        
        filter.doFilterInternal(request, response, chain);
        assertEquals(429, response.getStatus(), "La petición 11 debe ser bloqueada (HTTP 429)");
    }

    @Test
    public void testRateLimitDoesNotBlockIotEndpoints() throws ServletException, IOException {
        RateLimitFilter filter = new RateLimitFilter();

        // Simulate 50 rapid requests to IoT endpoint
        for (int i = 0; i < 50; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/iot/telemetry");
            request.setRemoteAddr("192.168.1.2");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = (req, res) -> {};
            
            filter.doFilterInternal(request, response, chain);
            assertEquals(200, response.getStatus(), "Los endpoints IoT no deberían estar limitados por RateLimitFilter");
        }
    }

    @Test
    public void testConcurrentStressOnRateLimitFilter() throws InterruptedException {
        RateLimitFilter filter = new RateLimitFilter();
        int threadCount = 50; // Simulate 50 concurrent requests
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger tooManyRequests = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users/login");
                    request.setRemoteAddr("10.0.0.1"); // Same IP
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    FilterChain chain = (req, res) -> {};
                    
                    filter.doFilterInternal(request, response, chain);
                    
                    if (response.getStatus() == 200) {
                        successfulRequests.incrementAndGet();
                    } else if (response.getStatus() == 429) {
                        tooManyRequests.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        long endTime = System.currentTimeMillis();
        
        // Exact 10 should succeed, the rest (40) should be 429 Too Many Requests
        assertEquals(10, successfulRequests.get(), "Solo 10 peticiones concurrentes deben ser exitosas");
        assertEquals(40, tooManyRequests.get(), "40 peticiones concurrentes deben ser bloqueadas");
        
        // Ensure that the filter adds negligible overhead (should complete very fast, < 1000ms)
        long duration = endTime - startTime;
        assertTrue(duration < 1000, "La prueba de estrés de concurrencia tomó demasiado tiempo: " + duration + "ms");
    }
}

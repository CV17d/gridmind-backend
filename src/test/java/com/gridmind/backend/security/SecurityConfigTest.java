package com.gridmind.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.DummyController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, RateLimitFilter.class, IotApiKeyFilter.class})
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @RestController
    public static class DummyController {
        @GetMapping("/api/v1/projects")
        public String protectedEndpoint() {
            return "protected";
        }

        @PostMapping("/api/v1/users/login")
        public String publicLogin() {
            return "login";
        }

        @PostMapping("/api/v1/iot/telemetry")
        public String iotEndpoint() {
            return "iot";
        }
    }

    @Test
    public void testProtectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/projects"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPublicLoginEndpointIsAccessible() throws Exception {
        mockMvc.perform(post("/api/v1/users/login"))
               .andExpect(status().isOk());
    }

    @Test
    public void testIotEndpointWithoutApiKeyReturnsUnauthorized() throws Exception {
        // Since it's IoT route, it goes through IotApiKeyFilter. It should return 401 if missing
        mockMvc.perform(post("/api/v1/iot/telemetry"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testIotEndpointWithInvalidApiKeyReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/iot/telemetry")
               .header("X-IoT-API-Key", "wrong_key"))
               .andExpect(status().isUnauthorized());
    }
}

package com.gridmind.backend.controller;

import com.gridmind.backend.repository.DeviceRepository;
import com.gridmind.backend.service.RelayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * GridMind - RelayController
 *
 * Expone los endpoints HTTP para:
 *
 * 1. [GET]  /api/v1/iot/relay-status?apiKey=XXX
 *    → Consultado por el ESP32 cada 5 segundos para conocer el estado deseado del relé.
 *    → NO requiere autenticación JWT (el ESP32 se identifica por su apiKey).
 *    → Responde: { "relayState": true/false }
 *
 * 2. [POST] /api/v1/devices/{id}/relay
 *    → Llamado desde el frontend web (usuario autenticado con JWT).
 *    → Body JSON: { "state": true/false }
 *    → Establece el nuevo estado deseado del relé del dispositivo.
 *
 * 3. [GET]  /api/v1/devices/{id}/relay
 *    → Consultado por el frontend para conocer el estado actual del relé de un dispositivo.
 *    → Requiere autenticación JWT.
 */
@RestController
public class RelayController {

    private final RelayService relayService;
    private final DeviceRepository deviceRepository;

    public RelayController(RelayService relayService, DeviceRepository deviceRepository) {
        this.relayService = relayService;
        this.deviceRepository = deviceRepository;
    }

    // ================================================================
    // ENDPOINT PARA EL ESP32 (Sin autenticación JWT - usa apiKey propia)
    // ================================================================

    /**
     * El ESP32 llama a este endpoint cada 5 segundos para ver si debe
     * encender o apagar su relé físico.
     *
     * Ejemplo de llamada desde el firmware:
     * GET https://backend.com/api/v1/iot/relay-status?apiKey=UUID-DE-TU-DISPOSITIVO
     *
     * Respuesta esperada:
     * { "relayState": true }  → El ESP32 activa el relé (enciende la luz/enchufe)
     * { "relayState": false } → El ESP32 desactiva el relé (apaga la luz/enchufe)
     */
    @GetMapping("/api/v1/iot/relay-status")
    public ResponseEntity<Map<String, Boolean>> getRelayStatusForDevice(
            @RequestParam("apiKey") String apiKey) {

        // Verificar que la apiKey existe en la base de datos
        boolean exists = deviceRepository.findByApiKey(apiKey).isPresent();
        if (!exists) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("relayState", false));
        }

        boolean state = relayService.getRelayState(apiKey);
        return ResponseEntity.ok(Map.of("relayState", state));
    }

    // ================================================================
    // ENDPOINTS PARA EL FRONTEND WEB (Requieren autenticación JWT)
    // ================================================================

    /**
     * El frontend web envía un comando para encender o apagar el relé de un dispositivo.
     *
     * Requiere: Header Authorization: Bearer <JWT_TOKEN>
     * Body: { "state": true }  → Encender
     *       { "state": false } → Apagar
     */
    @PostMapping("/api/v1/devices/{id}/relay")
    public ResponseEntity<Map<String, Object>> setRelayState(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            Authentication authentication) {

        if (!body.containsKey("state")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "El campo 'state' (true/false) es obligatorio en el body."));
        }

        boolean newState = body.get("state");

        try {
            relayService.setRelayState(id, authentication.getName(), newState);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "deviceId", id,
                    "relayState", newState,
                    "message", newState ? "Comando de ENCENDIDO enviado correctamente." : "Comando de APAGADO enviado correctamente."
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * El frontend consulta el estado actual del relé para mostrarlo visualmente
     * en la interfaz (ícono ON/OFF del dispositivo).
     */
    @GetMapping("/api/v1/devices/{id}/relay")
    public ResponseEntity<Map<String, Object>> getRelayStateForFrontend(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            Map<String, Object> state = relayService.getRelayStateByDeviceId(id, authentication.getName());
            return ResponseEntity.ok(state);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

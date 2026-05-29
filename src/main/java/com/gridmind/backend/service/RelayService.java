package com.gridmind.backend.service;

import com.gridmind.backend.model.Device;
import com.gridmind.backend.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GridMind - RelayService
 *
 * Gestiona el estado deseado del relé para cada dispositivo (por apiKey).
 * El ESP32 consulta este estado cada 5 segundos mediante GET /api/v1/iot/relay-status
 * y actúa en consecuencia encendiendo o apagando su relé físico.
 *
 * El estado se almacena en memoria (ConcurrentHashMap) para máxima velocidad.
 * En producción avanzada se podría persistir en base de datos.
 */
@Service
public class RelayService {

    private final DeviceRepository deviceRepository;

    // Mapa en memoria: apiKey -> estado deseado del relé (true = encendido, false = apagado)
    private final ConcurrentHashMap<String, Boolean> relayStates = new ConcurrentHashMap<>();

    public RelayService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    /**
     * Obtiene el estado deseado del relé para un dispositivo dado por su API Key.
     * Si no existe registro previo, el relé arranca apagado por seguridad.
     *
     * @param apiKey La API Key del dispositivo ESP32
     * @return true si debe estar encendido, false si debe estar apagado
     */
    public boolean getRelayState(String apiKey) {
        return relayStates.getOrDefault(apiKey, false);
    }

    /**
     * Establece el estado deseado del relé para un dispositivo dado.
     * Lanzado desde el frontend autenticado a través del DeviceController.
     *
     * @param deviceId  ID del dispositivo en la base de datos (verificación de propiedad)
     * @param userEmail Email del usuario autenticado (para verificar que le pertenece el dispositivo)
     * @param state     true = encender relé / false = apagar relé
     */
    public void setRelayState(Long deviceId, String userEmail, boolean state) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Dispositivo no encontrado con ID: " + deviceId));

        // Verificación de seguridad: el dispositivo debe pertenecer al usuario autenticado
        if (!device.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new SecurityException("Acceso denegado: este dispositivo no te pertenece.");
        }

        relayStates.put(device.getApiKey(), state);

        System.out.printf("[RELAY] 📡 Comando enviado al dispositivo '%s' (apiKey: %s...): %s%n",
                device.getName(),
                device.getApiKey().substring(0, 8),
                state ? "⚡ ENCENDER" : "🔴 APAGAR");
    }

    /**
     * Devuelve el estado del relé de un dispositivo por su ID (para el frontend).
     */
    public Map<String, Object> getRelayStateByDeviceId(Long deviceId, String userEmail) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Dispositivo no encontrado con ID: " + deviceId));

        if (!device.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new SecurityException("Acceso denegado.");
        }

        boolean state = relayStates.getOrDefault(device.getApiKey(), false);
        return Map.of(
                "deviceId", deviceId,
                "deviceName", device.getName(),
                "relayState", state
        );
    }
}

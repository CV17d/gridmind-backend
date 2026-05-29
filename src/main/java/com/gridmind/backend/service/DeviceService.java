package com.gridmind.backend.service;

import com.gridmind.backend.model.Device;
import com.gridmind.backend.model.User;
import com.gridmind.backend.repository.DeviceRepository;
import com.gridmind.backend.exception.AccessDeniedException;
import com.gridmind.backend.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final UserService userService;

    public DeviceService(DeviceRepository deviceRepository, UserService userService) {
        this.deviceRepository = deviceRepository;
        this.userService = userService;
    }

    // ✅ Crear device
    public Device createDevice(Device device, String email) {

        log.info("Registrando nuevo dispositivo '{}' para el usuario: {}", device.getName(), email);
        User user = userService.findByEmail(email);

        device.setUser(user);

        // Le asignamos un código UUID único y aleatorio de 36 caracteres a la placa
        device.setApiKey(UUID.randomUUID().toString());

        return deviceRepository.save(device);
    }

    // ✅ Obtener devices del usuario
    public List<Device> getUserDevices(String email) {

        User user = userService.findByEmail(email);

        return deviceRepository.findByUser(user);
    }

    // 🔐 Obtener por ID con seguridad
    public Device getDeviceById(Long id, String email) {
        log.debug("Consultando dispositivo {} para el usuario: {}", id, email);
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        if (!device.getUser().getEmail().equalsIgnoreCase(email)) {
            log.warn("ACCESO DENEGADO: El usuario {} intentó acceder al dispositivo {} que no le pertenece.", email, id);
            throw new AccessDeniedException("No tienes permiso para ver este dispositivo");
        }

        return device;
    }

    // 🔐 Eliminar
    public void deleteDevice(Long id, String email) {

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        if (!device.getUser().getEmail().equalsIgnoreCase(email)) {
            log.warn("ELIMINACIÓN DENEGADA: El usuario {} intentó borrar el dispositivo {} sin autorización.", email, id);
            throw new AccessDeniedException("No tienes permiso para eliminar este dispositivo");
        }

        log.info("Eliminando dispositivo {} ('{}') por solicitud de {}", id, device.getName(), email);
        deviceRepository.delete(device);
    }
}
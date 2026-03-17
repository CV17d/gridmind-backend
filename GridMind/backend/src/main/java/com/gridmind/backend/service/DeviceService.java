package com.gridmind.backend.service;

import com.gridmind.backend.model.Device;
import com.gridmind.backend.model.User;
import com.gridmind.backend.repository.DeviceRepository;
import com.gridmind.backend.service.UserService;
import com.gridmind.backend.exception.AccessDeniedException;
import com.gridmind.backend.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserService userService;

    public DeviceService(DeviceRepository deviceRepository, UserService userService) {
        this.deviceRepository = deviceRepository;
        this.userService = userService;
    }

    // ✅ Crear device
    public Device createDevice(Device device, String email) {

        User user = userService.findByEmail(email);

        device.setUser(user);

        return deviceRepository.save(device);
    }

    // ✅ Obtener devices del usuario
    public List<Device> getUserDevices(String email) {

        User user = userService.findByEmail(email);

        return deviceRepository.findByUser(user);
    }

    // 🔐 Obtener por ID con seguridad
    public Device getDeviceById(Long id, String email) {

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        if (!device.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Access denied");
        }

        return device;
    }

    // 🔐 Eliminar
    public void deleteDevice(Long id, String email) {

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        if (!device.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Access denied");
        }

        deviceRepository.delete(device);
    }
}
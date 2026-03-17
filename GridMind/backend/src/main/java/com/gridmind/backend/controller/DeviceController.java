package com.gridmind.backend.controller;

import com.gridmind.backend.model.Device;
import com.gridmind.backend.service.DeviceService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // Crear device
    @PostMapping
    public Device createDevice(
            @RequestBody Device device,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return deviceService.createDevice(device, email);
    }

    // Obtener todos los devices del usuario
    @GetMapping
    public List<Device> getDevices(Authentication authentication) {

        String email = authentication.getName();

        return deviceService.getUserDevices(email);
    }

    // Obtener por ID
    @GetMapping("/{id}")
    public Device getDeviceById(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        return deviceService.getDeviceById(id, email);
    }

    // Eliminar
    @DeleteMapping("/{id}")
    public void deleteDevice(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        deviceService.deleteDevice(id, email);
    }
}
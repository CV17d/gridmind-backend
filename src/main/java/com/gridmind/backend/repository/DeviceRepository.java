package com.gridmind.backend.repository;

import com.gridmind.backend.model.Device;
import com.gridmind.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByUser(User user);

    // Spring Boot escribirá el código SQL equivalente solito
    java.util.Optional<Device> findByEsp32Id(String esp32Id);
    java.util.Optional<Device> findByApiKey(String apiKey);
}
package com.gridmind.backend.repository;

import com.gridmind.backend.model.EnergyConsumption;
import com.gridmind.backend.model.Device;
import com.gridmind.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnergyConsumptionRepository extends JpaRepository<EnergyConsumption, Long> {

    Page<EnergyConsumption> findByDevice(Device device, Pageable pageable);

    @Query(value = "SELECT ec.* FROM energy_consumptions ec JOIN devices d ON ec.device_id = d.id WHERE d.user_id = :userId AND ec.timestamp > NOW() - INTERVAL '5 minutes' ORDER BY ec.timestamp DESC LIMIT 100", nativeQuery = true)
    List<EnergyConsumption> findTop100ByUserOrderByTimestampAsc(@Param("userId") Long userId);

    @Query(value = "SELECT ec.* FROM energy_consumptions ec WHERE ec.device_id = :deviceId ORDER BY ec.timestamp ASC LIMIT 100", nativeQuery = true)
    List<EnergyConsumption> findTop100ByDeviceOrderByTimestampAsc(@Param("deviceId") Long deviceId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT DATE(e.timestamp) AS date, SUM(e.consumption) AS totalKwh " +
        "FROM EnergyConsumption e " +
        "WHERE e.device.user.id = :userId " +
        "GROUP BY DATE(e.timestamp) " +
        "ORDER BY date ASC"
    )
    java.util.List<com.gridmind.backend.dto.DailyConsumptionDTO> findDailyConsumptionByUserId(
        @org.springframework.data.repository.query.Param("userId") Long userId
    );

    @Query("SELECT SUM(e.consumption) FROM EnergyConsumption e WHERE e.device.user.id = :userId")
    Double sumConsumptionByUserId(@Param("userId") Long userId);
}
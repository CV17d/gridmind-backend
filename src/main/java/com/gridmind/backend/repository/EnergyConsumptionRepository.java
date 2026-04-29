package com.gridmind.backend.repository;

import com.gridmind.backend.model.EnergyConsumption;
import com.gridmind.backend.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnergyConsumptionRepository extends JpaRepository<EnergyConsumption, Long> {

    Page<EnergyConsumption> findByDevice(Device device, Pageable pageable);

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
}
package com.gridmind.backend.repository;

import com.gridmind.backend.model.EnergyConsumption;
import com.gridmind.backend.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnergyConsumptionRepository extends JpaRepository<EnergyConsumption, Long> {

    Page<EnergyConsumption> findByDevice(Device device, Pageable pageable);
}
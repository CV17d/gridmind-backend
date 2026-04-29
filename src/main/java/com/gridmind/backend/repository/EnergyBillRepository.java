package com.gridmind.backend.repository;

import com.gridmind.backend.model.EnergyBill;
import com.gridmind.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnergyBillRepository extends JpaRepository<EnergyBill, Long> {
    List<EnergyBill> findByUserOrderByUploadedAtDesc(User user);
}

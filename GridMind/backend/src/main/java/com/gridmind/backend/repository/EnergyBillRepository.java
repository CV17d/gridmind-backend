package com.gridmind.backend.repository;
import com.gridmind.backend.model.EnergyBill;
import com.gridmind.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EnergyBillRepository extends JpaRepository<EnergyBill, Long> {
    // Extrae mágicamente todas las facturas de un dueño y pone la última que subió de primero
    List<EnergyBill> findByUserOrderByUploadedAtDesc(User user);
}

package com.gridmind.backend.repository;
import com.gridmind.backend.model.Alert;
import com.gridmind.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AlertRepository extends JpaRepository<Alert, Long> {
    // Trae las alertas del usuario, las más recientes primero
    List<Alert> findByUserOrderByCreatedAtDesc(User user);
    // Cuenta cuántas alertas NO LEÍDAS tiene (para el "badge" rojo de la App)
    Long countByUserAndReadFalse(User user);
}

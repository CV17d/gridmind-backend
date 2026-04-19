package com.gridmind.backend.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Tipo de alerta: "HIGH_CONSUMPTION", "DEVICE_OFFLINE", "BILL_SPIKE"
    private String type;
    // El mensaje que verá el usuario en su App
    private String message;
    // ¿Ya la leyó el usuario? (Para mostrar el puntito rojo de notificación)
    private Boolean read = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    public Alert() {}
    // --- Getters y Setters ---
    public Long getId() { return id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

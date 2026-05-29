package com.gridmind.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    
    private String role = "USER"; // Default role

    private BigDecimal electricityRate = new BigDecimal("0.12"); // Tarifa por kWh en dólares

    private BigDecimal alertThreshold = new BigDecimal("50.0"); // Límite de consumo en kWh para alerta

    private LocalDateTime createdAt = LocalDateTime.now();

    private String resetToken;

    private LocalDateTime resetTokenExpiry;

    @Version
    private Long version;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Device> devices;

    public User() {}

    // Getters y Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    public BigDecimal getElectricityRate() { return electricityRate; }
    public void setElectricityRate(BigDecimal electricityRate) { this.electricityRate = electricityRate; }

    public BigDecimal getAlertThreshold() { return alertThreshold; }
    public void setAlertThreshold(BigDecimal alertThreshold) { this.alertThreshold = alertThreshold; }

    public Long getVersion() { return version; }
}

package com.gridmind.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_consumptions")
public class EnergyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision = 19, scale = 10)
    private java.math.BigDecimal consumption; // kWh (Precisión extrema para IoT)
    
    private Double voltage; // V
    private Double current; // A
    private Double power;   // W

    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    public EnergyConsumption() {
    }

    public Long getId() {
        return id;
    }

    public java.math.BigDecimal getConsumption() {
        return consumption;
    }
    public void setConsumption(java.math.BigDecimal consumption) {
        this.consumption = consumption;
    }

    public Double getVoltage() { return voltage; }
    public void setVoltage(Double voltage) { this.voltage = voltage; }

    public Double getCurrent() { return current; }
    public void setCurrent(Double current) { this.current = current; }

    public Double getPower() { return power; }
    public void setPower(Double power) { this.power = power; }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
        
        System.out.println("📡 IOT DEBUG: Guardando consumo: " + this.getConsumption() + " kWh para el dispositivo " + (device != null ? device.getName() : "null"));
    }
}
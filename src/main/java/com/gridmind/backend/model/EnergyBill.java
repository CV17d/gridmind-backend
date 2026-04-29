package com.gridmind.backend.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "energy_bills")
public class EnergyBill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Ruta o nombre del archivo de la foto que subirá el usuario
    private String fileUrl; 
    // Los datos que la IA logrará extraer de la foto (Lo que la compañía de luz le cobró)
    private Double totalKwh;      // Ej. 150.5 kWh
    private Double totalAmount;   // Ej. $45.00 dólares/pesos
    // Para saber de cuándo es la factura
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    // Aquí guardaremos las "Recomendaciones AI" (Ej. "Conecta el enchufe a la TV")
    @Column(columnDefinition = "TEXT")
    private String aiRecommendations;
    private LocalDateTime uploadedAt = LocalDateTime.now();
    // Cada factura le pertenece estrictamente a un Humano
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    public EnergyBill() {
    }
    // --- AÑADE a partir de aquí tus Getters y Setters habituales ---
    public Long getId() { return id; }
    
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    
    public Double getTotalKwh() { return totalKwh; }
    public void setTotalKwh(Double totalKwh) { this.totalKwh = totalKwh; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public LocalDate getBillingPeriodStart() { return billingPeriodStart; }
    public void setBillingPeriodStart(LocalDate billingPeriodStart) { this.billingPeriodStart = billingPeriodStart; }
    
    public LocalDate getBillingPeriodEnd() { return billingPeriodEnd; }
    public void setBillingPeriodEnd(LocalDate billingPeriodEnd) { this.billingPeriodEnd = billingPeriodEnd; }
    
    public String getAiRecommendations() { return aiRecommendations; }
    public void setAiRecommendations(String aiRecommendations) { this.aiRecommendations = aiRecommendations; }
    
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

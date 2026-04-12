package com.gridmind.backend.controller;

import com.gridmind.backend.dto.EnergyBillDTO;
import com.gridmind.backend.model.EnergyBill;
import com.gridmind.backend.service.EnergyBillService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bills")
public class EnergyBillController {
    
    private final EnergyBillService billService;

    public EnergyBillController(EnergyBillService billService) {
        this.billService = billService;
    }

    // 📸 Endpoint para subir la foto de la factura
    @PostMapping("/upload")
    public ResponseEntity<?> uploadBill(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            EnergyBill savedBill = billService.analyzeAndSaveBill(file, email);
            
            return ResponseEntity.ok(new EnergyBillDTO(
                    savedBill.getId(),
                    savedBill.getFileUrl(),
                    savedBill.getTotalKwh(),
                    savedBill.getTotalAmount(),
                    savedBill.getAiRecommendations(),
                    savedBill.getUploadedAt()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la imagen en el servidor: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 📋 Endpoint para listar el historial del "Asesor AI"
    @GetMapping
    public ResponseEntity<List<EnergyBillDTO>> getMyBills(Authentication authentication) {
        
        String email = authentication.getName();
        List<EnergyBill> bills = billService.getUserBills(email);
        
        List<EnergyBillDTO> billDTOs = bills.stream()
            .map(bill -> new EnergyBillDTO(
                    bill.getId(),
                    bill.getFileUrl(),
                    bill.getTotalKwh(),
                    bill.getTotalAmount(),
                    bill.getAiRecommendations(),
                    bill.getUploadedAt()))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(billDTOs);
    }
}

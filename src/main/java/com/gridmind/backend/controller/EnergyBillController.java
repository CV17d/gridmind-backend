package com.gridmind.backend.controller;

import com.gridmind.backend.model.EnergyBill;
import com.gridmind.backend.service.EnergyBillService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bills")
public class EnergyBillController {

    private final EnergyBillService billService;

    public EnergyBillController(EnergyBillService billService) {
        this.billService = billService;
    }

    // 📤 Subir y Analizar Factura con IA
    @PostMapping("/upload")
    public ResponseEntity<EnergyBill> uploadBill(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws Exception {
        String email = authentication.getName();
        EnergyBill savedBill = billService.analyzeAndSaveBill(file, email);
        return ResponseEntity.ok(savedBill);
    }

    // 📋 Listar mis facturas
    @GetMapping
    public ResponseEntity<List<EnergyBill>> getMyBills(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(billService.getUserBills(email));
    }

    // 🖼️ Ver la foto de la factura
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getBillImage(
            @PathVariable Long id,
            Authentication authentication) throws Exception {
        String email = authentication.getName();
        byte[] image = billService.getBillImageAsBytes(id, email);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }
}

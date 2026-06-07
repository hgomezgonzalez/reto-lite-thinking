package com.litethinking.reto.presentation.controller;

import com.litethinking.reto.application.dto.EmailRequest;
import com.litethinking.reto.application.dto.InventarioRequest;
import com.litethinking.reto.application.dto.InventarioResponse;
import com.litethinking.reto.application.service.InventarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de Inventario (requisitos d y e): consulta, ajuste de stock,
 * descarga del PDF y envio del PDF por correo.
 */
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    public List<InventarioResponse> findAll(@RequestParam(required = false) String empresaNit) {
        return (empresaNit == null || empresaNit.isBlank())
                ? inventarioService.findAll()
                : inventarioService.findByEmpresa(empresaNit);
    }

    @PostMapping
    public ResponseEntity<InventarioResponse> upsert(@Valid @RequestBody InventarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.upsert(request));
    }

    /** Descarga el PDF del inventario (requisito d). */
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam(required = false) String empresaNit) {
        byte[] pdf = inventarioService.generarPdf(empresaNit);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventario.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /** Genera el PDF y lo envia a un correo (requisito d). */
    @PostMapping("/pdf/email")
    public ResponseEntity<Void> emailPdf(@RequestParam(required = false) String empresaNit,
                                         @Valid @RequestBody EmailRequest request) {
        inventarioService.enviarPdfPorCorreo(empresaNit, request.to());
        return ResponseEntity.accepted().build();
    }
}

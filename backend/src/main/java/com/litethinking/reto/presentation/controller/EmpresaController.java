package com.litethinking.reto.presentation.controller;

import com.litethinking.reto.application.dto.EmpresaRequest;
import com.litethinking.reto.application.dto.EmpresaResponse;
import com.litethinking.reto.application.service.EmpresaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de Empresa (requisito a). Las lecturas estan disponibles para
 * ADMIN y EXTERNAL; las escrituras solo para ADMIN (control en SecurityConfig).
 */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public List<EmpresaResponse> findAll() {
        return empresaService.findAll();
    }

    @GetMapping("/{nit}")
    public EmpresaResponse findByNit(@PathVariable String nit) {
        return empresaService.findByNit(nit);
    }

    @PostMapping
    public ResponseEntity<EmpresaResponse> create(@Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.create(request));
    }

    @PutMapping("/{nit}")
    public EmpresaResponse update(@PathVariable String nit, @Valid @RequestBody EmpresaRequest request) {
        return empresaService.update(nit, request);
    }

    @DeleteMapping("/{nit}")
    public ResponseEntity<Void> delete(@PathVariable String nit) {
        empresaService.delete(nit);
        return ResponseEntity.noContent().build();
    }
}

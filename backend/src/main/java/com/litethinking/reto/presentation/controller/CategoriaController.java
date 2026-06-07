package com.litethinking.reto.presentation.controller;

import com.litethinking.reto.application.dto.CategoriaResponse;
import com.litethinking.reto.application.service.CategoriaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint de catalogo de Categorias. */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> findAll() {
        return categoriaService.findAll();
    }
}

package com.litethinking.reto.application.mapper;

import com.litethinking.reto.application.dto.CategoriaResponse;
import com.litethinking.reto.application.dto.ProductoResponse;
import com.litethinking.reto.domain.model.Producto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Conversion de la entidad Producto a su DTO de salida. */
public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static ProductoResponse toResponse(Producto producto, Map<String, BigDecimal> precios) {
        List<CategoriaResponse> categorias = producto.getCategorias().stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNombre()))
                .sorted((a, b) -> a.nombre().compareToIgnoreCase(b.nombre()))
                .collect(Collectors.toList());

        return new ProductoResponse(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getCaracteristicas(),
                producto.getPrecioBase(),
                producto.getMonedaBase(),
                producto.getEmpresa().getNit(),
                producto.getEmpresa().getNombre(),
                categorias,
                precios);
    }
}

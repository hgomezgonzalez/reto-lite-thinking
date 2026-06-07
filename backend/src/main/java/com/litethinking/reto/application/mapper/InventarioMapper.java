package com.litethinking.reto.application.mapper;

import com.litethinking.reto.application.dto.InventarioResponse;
import com.litethinking.reto.domain.model.Inventario;
import com.litethinking.reto.domain.model.Producto;
import java.math.BigDecimal;
import java.util.Map;

/** Conversion de la entidad Inventario a su DTO de salida. */
public final class InventarioMapper {

    private InventarioMapper() {
    }

    public static InventarioResponse toResponse(Inventario inventario, Map<String, BigDecimal> precios) {
        Producto producto = inventario.getProducto();
        return new InventarioResponse(
                inventario.getId(),
                inventario.getEmpresa().getNit(),
                inventario.getEmpresa().getNombre(),
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                inventario.getCantidad(),
                producto.getPrecioBase(),
                producto.getMonedaBase().name(),
                precios);
    }
}

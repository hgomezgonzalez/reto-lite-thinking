package com.litethinking.reto.application.mapper;

import com.litethinking.reto.application.dto.EmpresaRequest;
import com.litethinking.reto.application.dto.EmpresaResponse;
import com.litethinking.reto.domain.model.Empresa;

/** Conversion entre la entidad Empresa y sus DTOs. */
public final class EmpresaMapper {

    private EmpresaMapper() {
    }

    public static EmpresaResponse toResponse(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getNit(),
                empresa.getNombre(),
                empresa.getDireccion(),
                empresa.getTelefono());
    }

    /** Copia los campos editables de la peticion sobre la entidad. */
    public static void applyToEntity(EmpresaRequest request, Empresa empresa) {
        empresa.setNombre(request.nombre());
        empresa.setDireccion(request.direccion());
        empresa.setTelefono(request.telefono());
    }
}

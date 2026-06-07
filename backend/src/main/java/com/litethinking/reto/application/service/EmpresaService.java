package com.litethinking.reto.application.service;

import com.litethinking.reto.application.dto.EmpresaRequest;
import com.litethinking.reto.application.dto.EmpresaResponse;
import com.litethinking.reto.application.exception.BusinessException;
import com.litethinking.reto.application.exception.ResourceNotFoundException;
import com.litethinking.reto.application.mapper.EmpresaMapper;
import com.litethinking.reto.domain.model.Empresa;
import com.litethinking.reto.infrastructure.persistence.EmpresaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de Empresa (requisito a y e). La creacion, edicion y
 * eliminacion son operaciones reservadas al administrador (se controla en
 * la capa de seguridad). Este servicio solo contiene reglas de negocio.
 */
@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> findAll() {
        return empresaRepository.findAll().stream()
                .map(EmpresaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponse findByNit(String nit) {
        return EmpresaMapper.toResponse(getEntity(nit));
    }

    public EmpresaResponse create(EmpresaRequest request) {
        if (empresaRepository.existsById(request.nit())) {
            throw new BusinessException("Ya existe una empresa con el NIT " + request.nit());
        }
        Empresa empresa = new Empresa();
        empresa.setNit(request.nit());
        EmpresaMapper.applyToEntity(request, empresa);
        return EmpresaMapper.toResponse(empresaRepository.save(empresa));
    }

    public EmpresaResponse update(String nit, EmpresaRequest request) {
        Empresa empresa = getEntity(nit);
        EmpresaMapper.applyToEntity(request, empresa);
        return EmpresaMapper.toResponse(empresaRepository.save(empresa));
    }

    public void delete(String nit) {
        Empresa empresa = getEntity(nit);
        empresaRepository.delete(empresa);
    }

    private Empresa getEntity(String nit) {
        return empresaRepository.findById(nit)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la empresa con NIT " + nit));
    }
}

package com.litethinking.reto.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.litethinking.reto.application.dto.EmpresaRequest;
import com.litethinking.reto.application.dto.EmpresaResponse;
import com.litethinking.reto.application.exception.BusinessException;
import com.litethinking.reto.application.exception.ResourceNotFoundException;
import com.litethinking.reto.domain.model.Empresa;
import com.litethinking.reto.infrastructure.persistence.EmpresaRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    private EmpresaRequest request() {
        return new EmpresaRequest("900123456-1", "Lite Thinking", "Calle 100", "6017551234");
    }

    @Test
    void creaEmpresaCuandoNitNoExiste() {
        when(empresaRepository.existsById("900123456-1")).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpresaResponse response = empresaService.create(request());

        assertThat(response.nit()).isEqualTo("900123456-1");
        assertThat(response.nombre()).isEqualTo("Lite Thinking");
        verify(empresaRepository).save(any(Empresa.class));
    }

    @Test
    void lanzaErrorCuandoNitYaExiste() {
        when(empresaRepository.existsById("900123456-1")).thenReturn(true);

        assertThatThrownBy(() -> empresaService.create(request()))
                .isInstanceOf(BusinessException.class);
        verify(empresaRepository, never()).save(any());
    }

    @Test
    void lanzaErrorCuandoSeActualizaEmpresaInexistente() {
        when(empresaRepository.findById("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaService.update("999", request()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

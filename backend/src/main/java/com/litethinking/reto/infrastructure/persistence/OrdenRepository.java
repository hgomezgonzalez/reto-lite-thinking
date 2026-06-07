package com.litethinking.reto.infrastructure.persistence;

import com.litethinking.reto.domain.model.Orden;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    List<Orden> findByClienteId(Long clienteId);
}

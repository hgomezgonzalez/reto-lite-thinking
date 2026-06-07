package com.litethinking.reto.infrastructure.persistence;

import com.litethinking.reto.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}

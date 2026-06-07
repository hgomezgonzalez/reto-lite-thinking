package com.litethinking.reto.infrastructure.persistence;

import com.litethinking.reto.domain.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, String> {
}

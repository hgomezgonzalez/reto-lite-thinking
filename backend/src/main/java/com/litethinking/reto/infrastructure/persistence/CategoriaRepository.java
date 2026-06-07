package com.litethinking.reto.infrastructure.persistence;

import com.litethinking.reto.domain.model.Categoria;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Set<Categoria> findByIdIn(Set<Long> ids);
}

package com.litethinking.reto.infrastructure.persistence;

import com.litethinking.reto.domain.model.Producto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByEmpresaNit(String nit);

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}

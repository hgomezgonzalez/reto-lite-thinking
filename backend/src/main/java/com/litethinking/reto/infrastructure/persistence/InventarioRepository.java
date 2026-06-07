package com.litethinking.reto.infrastructure.persistence;

import com.litethinking.reto.domain.model.Inventario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByEmpresaNit(String nit);

    Optional<Inventario> findByEmpresaNitAndProductoId(String nit, Long productoId);
}

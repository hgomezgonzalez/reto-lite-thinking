package com.litethinking.reto.application.port;

import com.litethinking.reto.domain.model.Moneda;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Puerto de conversion de monedas. La capa de aplicacion depende de esta
 * abstraccion y no de la implementacion concreta (DIP de SOLID).
 */
public interface CurrencyConverter {

    /**
     * Convierte un monto expresado en la moneda base a todas las monedas
     * objetivo configuradas.
     *
     * @param amount monto en la moneda base
     * @param base   moneda en la que esta expresado el monto
     * @return mapa moneda -> monto convertido (incluye la propia moneda base)
     */
    Map<String, BigDecimal> convertToAll(BigDecimal amount, Moneda base);
}

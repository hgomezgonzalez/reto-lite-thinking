package com.litethinking.reto.infrastructure.currency;

import static org.assertj.core.api.Assertions.assertThat;

import com.litethinking.reto.domain.model.Moneda;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExchangeRateApiAdapterTest {

    // URL invalida para forzar el uso de las tasas estaticas de respaldo.
    private final ExchangeRateApiAdapter adapter =
            new ExchangeRateApiAdapter("COP,USD,EUR", "http://localhost:1/no-existe");

    @Test
    void convierteUsandoTasasDeRespaldoCuandoLaApiFalla() {
        Map<String, BigDecimal> precios = adapter.convertToAll(new BigDecimal("1000000"), Moneda.COP);

        // La conversion a la misma moneda base debe devolver el mismo monto.
        assertThat(precios.get("COP")).isEqualByComparingTo("1000000.00");
        // 1.000.000 COP * 0.00025 USD/COP = 250 USD aprox.
        assertThat(precios.get("USD")).isEqualByComparingTo("250.00");
        // El valor en EUR debe ser positivo y menor que en USD.
        assertThat(precios.get("EUR")).isPositive();
        assertThat(precios.get("EUR")).isLessThan(precios.get("USD"));
    }
}

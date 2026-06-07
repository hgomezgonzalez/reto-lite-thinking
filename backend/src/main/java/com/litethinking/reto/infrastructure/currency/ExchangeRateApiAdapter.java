package com.litethinking.reto.infrastructure.currency;

import com.litethinking.reto.application.port.CurrencyConverter;
import com.litethinking.reto.domain.model.Moneda;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptador de conversion de monedas (implementa el puerto CurrencyConverter).
 *
 * Estrategia: consulta una API REST publica de tasas de cambio y, si falla o
 * no esta disponible, usa un conjunto de tasas estaticas de respaldo. Las
 * tasas se cachean en memoria para no llamar a la API en cada peticion.
 */
@Component
public class ExchangeRateApiAdapter implements CurrencyConverter {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateApiAdapter.class);
    private static final long CACHE_TTL_MS = 60 * 60 * 1000L; // 1 hora

    // Tasas estaticas de respaldo expresadas en USD (1 unidad de X = N USD).
    private static final Map<String, BigDecimal> USD_FALLBACK = Map.of(
            "USD", new BigDecimal("1.0"),
            "EUR", new BigDecimal("1.08"),
            "COP", new BigDecimal("0.00025"));

    private final List<String> targets;
    private final String apiUrl;
    private final RestClient restClient = RestClient.create();
    private final Map<String, CachedRates> cache = new ConcurrentHashMap<>();

    public ExchangeRateApiAdapter(@Value("${app.currency.targets}") String targetsCsv,
                                  @Value("${app.currency.api-url}") String apiUrl) {
        this.targets = List.of(targetsCsv.split(","));
        this.apiUrl = apiUrl;
    }

    @Override
    public Map<String, BigDecimal> convertToAll(BigDecimal amount, Moneda base) {
        Map<String, BigDecimal> rates = getRates(base.name());
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (String target : targets) {
            BigDecimal rate = rates.get(target.trim());
            if (rate != null) {
                result.put(target.trim(), amount.multiply(rate).setScale(2, RoundingMode.HALF_UP));
            }
        }
        return result;
    }

    private Map<String, BigDecimal> getRates(String base) {
        CachedRates cached = cache.get(base);
        if (cached != null && !cached.isExpired()) {
            return cached.rates();
        }
        Map<String, BigDecimal> rates = fetchFromApi(base);
        if (rates == null) {
            rates = staticRates(base);
        }
        cache.put(base, new CachedRates(rates, System.currentTimeMillis() + CACHE_TTL_MS));
        return rates;
    }

    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> fetchFromApi(String base) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(apiUrl + "/" + base)
                    .retrieve()
                    .body(Map.class);
            if (body == null || !"success".equals(body.get("result"))) {
                return null;
            }
            Map<String, Object> apiRates = (Map<String, Object>) body.get("rates");
            if (apiRates == null) {
                return null;
            }
            Map<String, BigDecimal> rates = new LinkedHashMap<>();
            for (String target : targets) {
                Object value = apiRates.get(target.trim());
                if (value != null) {
                    rates.put(target.trim(), new BigDecimal(value.toString()));
                }
            }
            return rates;
        } catch (Exception ex) {
            log.warn("No se pudo consultar la API de tasas de cambio ({}). Se usan tasas de respaldo.",
                    ex.getMessage());
            return null;
        }
    }

    /** Calcula las tasas base->target usando el USD como moneda puente. */
    private Map<String, BigDecimal> staticRates(String base) {
        BigDecimal baseToUsd = USD_FALLBACK.getOrDefault(base, BigDecimal.ONE);
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        for (String target : targets) {
            BigDecimal targetToUsd = USD_FALLBACK.get(target.trim());
            if (targetToUsd != null && targetToUsd.signum() != 0) {
                rates.put(target.trim(),
                        baseToUsd.divide(targetToUsd, 8, RoundingMode.HALF_UP));
            }
        }
        return rates;
    }

    private record CachedRates(Map<String, BigDecimal> rates, long expiresAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}

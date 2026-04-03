package ro.axonsoft.eval.minibank.service;

import org.springframework.stereotype.Service;
import ro.axonsoft.eval.minibank.config.ExchangeRateProperties;
import ro.axonsoft.eval.minibank.exception.BadRequestException;
import ro.axonsoft.eval.minibank.model.Currency;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ExchangeRateService {

    private final ExchangeRateProperties exchangeRateProperties;

    public ExchangeRateService(ExchangeRateProperties exchangeRateProperties) {
        this.exchangeRateProperties = exchangeRateProperties;
    }

    public BigDecimal getExchangeRate(Currency currency) {
        BigDecimal rate = exchangeRateProperties.getRates().get(currency.name());
        if (rate == null) {
            throw new BadRequestException("Unsupported currency");
        }
        return rate;
    }

    public Map<String, BigDecimal> getAllExchangeRates() {
        return new LinkedHashMap<>(exchangeRateProperties.getRates());
    }
}
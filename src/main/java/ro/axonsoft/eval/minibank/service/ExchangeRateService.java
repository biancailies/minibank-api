package ro.axonsoft.eval.minibank.service;

import org.springframework.stereotype.Service;
import ro.axonsoft.eval.minibank.exception.BadRequestException;
import ro.axonsoft.eval.minibank.model.Currency;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ExchangeRateService {
    public BigDecimal getExchangeRate(Currency currency) {
        switch (currency) {
            case EUR:
                return new BigDecimal("4.97");
            case USD:
                return new BigDecimal("4.56");
            case GBP:
                return new BigDecimal("5.73");
            case RON:
                return new BigDecimal("1.00");
            default:
                throw new BadRequestException("Unsupported currency");
        }
    }

    public Map<String, BigDecimal> getAllExchangeRates() {
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        rates.put("EUR", new BigDecimal("4.97"));
        rates.put("USD", new BigDecimal("4.56"));
        rates.put("GBP", new BigDecimal("5.73"));
        rates.put("RON", new BigDecimal("1.00"));

        return rates;
    }
}

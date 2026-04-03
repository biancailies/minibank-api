package ro.axonsoft.eval.minibank.service;

import org.springframework.stereotype.Service;
import ro.axonsoft.eval.minibank.exception.BadRequestException;
import ro.axonsoft.eval.minibank.model.Currency;

import java.math.BigDecimal;

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
                new BadRequestException("Unsupported currency");
        }
        return null;
    }
}

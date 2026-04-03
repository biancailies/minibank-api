package ro.axonsoft.eval.minibank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.axonsoft.eval.minibank.service.ExchangeRateService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/api/exchange-rates")
    public Map<String, BigDecimal> getExchangeRates() {
        return exchangeRateService.getAllExchangeRates();
    }
}
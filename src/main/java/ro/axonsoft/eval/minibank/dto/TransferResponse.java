package ro.axonsoft.eval.minibank.dto;

import jakarta.persistence.Enumerated;
import ro.axonsoft.eval.minibank.model.Currency;

import java.math.BigDecimal;
import java.time.Instant;

public class TransferResponse {
    private Long id;
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;
    private Currency currency;
    private Currency targetCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private String idempotencyKey;
    private Instant createdAt;

    public TransferResponse(){}

    public void setId(Long id) {
        this.id = id;
    }

    public void setSourceIban(String sourceIban) {
        this.sourceIban = sourceIban;
    }

    public void setTargetIban(String targetIban) {
        this.targetIban = targetIban;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTargetIban() {
        return targetIban;
    }

    public String getSourceIban() {
        return sourceIban;
    }

    public Long getId() {
        return id;
    }
}

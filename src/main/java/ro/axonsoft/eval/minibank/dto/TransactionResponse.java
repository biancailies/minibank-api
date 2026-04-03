package ro.axonsoft.eval.minibank.dto;

import ro.axonsoft.eval.minibank.model.Currency;
import ro.axonsoft.eval.minibank.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse {
    private Long id;
    private Instant timestamp;
    private TransactionType type;
    private BigDecimal amount;
    private Currency currency;
    private BigDecimal balanceAfter;
    private String counterpartyIban;
    private Long transferId;

    public TransactionResponse() {}

    public void setId(Long id) {
        this.id = id;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public void setCounterpartyIban(String counterpartyIban) {
        this.counterpartyIban = counterpartyIban;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public Long getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getCounterpartyIban() {
        return counterpartyIban;
    }

    public Long getTransferId() {
        return transferId;
    }
}

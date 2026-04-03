package ro.axonsoft.eval.minibank.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account account;

    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private BigDecimal amount;

    private BigDecimal balanceAfter;
    private String counterpartyIban;
    private Long transferId;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    public Transaction() {}

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
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

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getCounterpartyIban() {
        return counterpartyIban;
    }

    public Long getTransferId() {
        return transferId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setAccount(Account account) {
        this.account = account;
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

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public void setCounterpartyIban(String counterpartyIban) {
        this.counterpartyIban = counterpartyIban;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}

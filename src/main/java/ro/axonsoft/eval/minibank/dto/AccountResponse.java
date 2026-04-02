package ro.axonsoft.eval.minibank.dto;

import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Currency;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountResponse {
    private Long id;
    private String ownerName;
    private String iban;
    private Currency currency;
    private AccountType accountType;
    private BigDecimal balance;
    private Instant createdAt;

    public AccountResponse() {}

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getIban() {
        return iban;
    }

    public Currency getCurrency() {
        return currency;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

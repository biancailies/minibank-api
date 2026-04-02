package ro.axonsoft.eval.minibank.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ownerName;
    private String iban;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    private BigDecimal balance;
    private Instant createdAt;

    public Account() {}

    public void setAccountType(AccountType accountType){
        this.accountType = accountType;
    }

    public void setOwnerName(String ownerName){
        this.ownerName = ownerName;
    }

    public void setIban(String iban){
        this.iban = iban;
    }

    public void setCurrency(Currency currency){
        this.currency = currency;
    }

    public void setBalance(BigDecimal balance){
        this.balance = balance;
    }

    public void setCreatedAt(Instant createdAt){
        this.createdAt = createdAt;
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

    public Long getId() {
        return id;
    }
}

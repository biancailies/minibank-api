package ro.axonsoft.eval.minibank.dto;

import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Currency;

public class CreateAccountRequest {
    private String ownerName;
    private String iban;
    private Currency currency;
    private AccountType accountType;

    public CreateAccountRequest() {}

    public String getIban(){
        return iban;
    }

    public String getOwnerName(){
        return ownerName;
    }

    public Currency getCurrency(){
        return currency;
    }

    public AccountType getAccountType(){
        return accountType;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}

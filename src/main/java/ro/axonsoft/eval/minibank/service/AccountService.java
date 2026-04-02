package ro.axonsoft.eval.minibank.service;

import org.springframework.stereotype.Service;
import ro.axonsoft.eval.minibank.Exception.ResourceAlreadyExistsException;
import ro.axonsoft.eval.minibank.dto.AccountResponse;
import ro.axonsoft.eval.minibank.dto.CreateAccountRequest;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Currency;
import ro.axonsoft.eval.minibank.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        String iban = request.getIban();

        if(accountRepository.existsByIban(iban)){
            throw new ResourceAlreadyExistsException("IBAN already exists");
        }

        Account account = new Account();

        account.setOwnerName(request.getOwnerName());
        account.setCurrency(request.getCurrency());
        account.setAccountType(request.getAccountType());
        account.setIban(iban);
        account.setBalance(BigDecimal.ZERO);
        account.setCreatedAt(Instant.now());

        Account saveAcc = accountRepository.save(account);

        AccountResponse accountResponse = new AccountResponse();

        accountResponse.setId(saveAcc.getId());
        accountResponse.setAccountType(saveAcc.getAccountType());
        accountResponse.setIban(saveAcc.getIban());
        accountResponse.setBalance(saveAcc.getBalance());
        accountResponse.setCreatedAt(saveAcc.getCreatedAt());
        accountResponse.setOwnerName(saveAcc.getOwnerName());
        accountResponse.setCurrency(saveAcc.getCurrency());
        return accountResponse;
    }

    public AccountResponse getAllAccounts() {
        List<Account> accountsList = new ArrayList<Account>();


    }
}

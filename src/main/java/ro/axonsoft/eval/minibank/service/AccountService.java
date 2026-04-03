package ro.axonsoft.eval.minibank.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ro.axonsoft.eval.minibank.exception.BadRequestException;
import ro.axonsoft.eval.minibank.exception.ResourceAlreadyExistsException;
import ro.axonsoft.eval.minibank.exception.ResourceNotFoundException;
import ro.axonsoft.eval.minibank.dto.AccountResponse;
import ro.axonsoft.eval.minibank.dto.AccountsPageResponse;
import ro.axonsoft.eval.minibank.dto.CreateAccountRequest;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.util.IbanValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        String iban = request.getIban();

        String normalizedIban = IbanValidator.normalizeIban(iban).toUpperCase();

        if (!IbanValidator.isValidIban(normalizedIban)) {
            throw new BadRequestException("Invalid IBAN");
        }

        if(accountRepository.existsByIban(normalizedIban)) {
            throw new ResourceAlreadyExistsException("IBAN already exists");
        }

        Account account = new Account();

        account.setOwnerName(request.getOwnerName());
        account.setCurrency(request.getCurrency());
        account.setAccountType(request.getAccountType());
        account.setIban(normalizedIban);
        account.setBalance(BigDecimal.ZERO.setScale(2));
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

    public AccountsPageResponse getAllAccounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Account> accountsPage = accountRepository.findAll(pageable);
        List<Account> accountsList = accountsPage.getContent();
        List<AccountResponse> accountResponses = new ArrayList<>();

        for(Account account : accountsList){
            AccountResponse accountResponse = new AccountResponse();

            accountResponse.setAccountType(account.getAccountType());
            accountResponse.setIban(account.getIban());
            accountResponse.setId(account.getId());
            accountResponse.setBalance(account.getBalance());
            accountResponse.setCreatedAt(account.getCreatedAt());
            accountResponse.setOwnerName(account.getOwnerName());
            accountResponse.setCurrency(account.getCurrency());

            accountResponses.add(accountResponse);
        }
        AccountsPageResponse response = new AccountsPageResponse();

        response.setContent(accountResponses);
        response.setTotalElements(accountsPage.getTotalElements());
        response.setTotalPages(accountsPage.getTotalPages());
        response.setNumber(accountsPage.getNumber());
        response.setSize(accountsPage.getSize());

        return response;
    }

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElse(null);

        if(account == null){
            throw new ResourceNotFoundException("Account not found");
        }
        AccountResponse accountResponse = new AccountResponse();

        accountResponse.setAccountType(account.getAccountType());
        accountResponse.setIban(account.getIban());
        accountResponse.setBalance(account.getBalance());
        accountResponse.setCreatedAt(account.getCreatedAt());
        accountResponse.setOwnerName(account.getOwnerName());
        accountResponse.setCurrency(account.getCurrency());
        accountResponse.setId(account.getId());

        return accountResponse;
    }
}

package ro.axonsoft.eval.minibank.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ro.axonsoft.eval.minibank.dto.*;
import ro.axonsoft.eval.minibank.exception.BadRequestException;
import ro.axonsoft.eval.minibank.exception.ResourceAlreadyExistsException;
import ro.axonsoft.eval.minibank.exception.ResourceNotFoundException;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.Transaction;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransactionRepository;
import ro.axonsoft.eval.minibank.util.IbanValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
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
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return toAccountResponse(account);
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        String normalizedIban = IbanValidator.normalizeIban(request.getIban());

        if (!IbanValidator.isValidIban(normalizedIban)) {
            throw new BadRequestException("Invalid IBAN");
        }

        if (accountRepository.existsByIban(normalizedIban)) {
            throw new ResourceAlreadyExistsException("IBAN already exists");
        }

        Account account = new Account();
        account.setOwnerName(request.getOwnerName());
        account.setIban(normalizedIban);
        account.setCurrency(request.getCurrency());
        account.setAccountType(request.getAccountType());
        account.setBalance(new BigDecimal("0.00"));
        account.setCreatedAt(Instant.now());

        return toAccountResponse(accountRepository.save(account));
    }

    private AccountResponse toAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setOwnerName(account.getOwnerName());
        response.setIban(account.getIban());
        response.setCurrency(account.getCurrency());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }

    public TransactionsPageResponse getAccountTransactions(Long accountId, int page, int size) {
        Account account = accountRepository.findById(accountId).orElse(null);

        if (account == null) {
            throw new ResourceNotFoundException("Account not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionsPage =
                transactionRepository.findByAccountIdOrderByTimestampAscIdAsc(accountId, pageable);

        List<Transaction> transactionsList = transactionsPage.getContent();
        List<TransactionResponse> transactionResponses = new ArrayList<>();

        for (Transaction transaction : transactionsList) {
            TransactionResponse transactionResponse = new TransactionResponse();

            transactionResponse.setId(transaction.getId());
            transactionResponse.setTimestamp(transaction.getTimestamp());
            transactionResponse.setType(transaction.getType());
            transactionResponse.setAmount(transaction.getAmount());
            transactionResponse.setCurrency(transaction.getCurrency());
            transactionResponse.setBalanceAfter(transaction.getBalanceAfter());
            transactionResponse.setCounterpartyIban(transaction.getCounterpartyIban());
            transactionResponse.setTransferId(transaction.getTransferId());

            transactionResponses.add(transactionResponse);
        }

        TransactionsPageResponse response = new TransactionsPageResponse();
        response.setContent(transactionResponses);
        response.setTotalElements(transactionsPage.getTotalElements());
        response.setTotalPages(transactionsPage.getTotalPages());
        response.setNumber(transactionsPage.getNumber());
        response.setSize(transactionsPage.getSize());

        return response;
    }

}

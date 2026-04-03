package ro.axonsoft.eval.minibank.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Currency;
import ro.axonsoft.eval.minibank.repository.AccountRepository;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class DataInitializer {

    public static final String SYSTEM_IBAN = "RO49AAAA1B31007593840000";

    private final AccountRepository accountRepository;

    public DataInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostConstruct
    public void initAccount() {
        if (accountRepository.existsByIban(SYSTEM_IBAN)) {
            return;
        }

        Account account = new Account();
        account.setOwnerName("SYSTEM_BANK");
        account.setIban(SYSTEM_IBAN);
        account.setCurrency(Currency.RON);
        account.setAccountType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("0.00"));
        account.setCreatedAt(Instant.now());

        accountRepository.save(account);
    }
}
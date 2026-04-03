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
    private final AccountRepository accountRepository;

    public DataInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PostConstruct
    public void initAccount() {
        if(accountRepository.existsByIban("RO49AAAA1B31007593840000")) {}
        else {
            Account account = new Account();

            account.setAccountType(AccountType.CHECKING);
            account.setBalance(BigDecimal.ZERO);
            account.setCurrency(Currency.RON);
            account.setOwnerName("SYSTEM_BANK");
            account.setIban("RO49AAAA1B31007593840000");
            account.setCreatedAt(Instant.now());

            accountRepository.save(account);
        }
    }
}

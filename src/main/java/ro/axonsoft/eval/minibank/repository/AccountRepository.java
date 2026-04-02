package ro.axonsoft.eval.minibank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.axonsoft.eval.minibank.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    public Account findByIban(String iban);
    public boolean existsByIban(String iban);
}

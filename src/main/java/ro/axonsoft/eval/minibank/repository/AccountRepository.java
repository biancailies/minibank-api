package ro.axonsoft.eval.minibank.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.axonsoft.eval.minibank.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

     boolean existsByIban(String iban);

     Account findByIban(String iban);

     Optional<Account> findOptionalByIban(String iban);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("select a from Account a where a.iban = :iban")
     Optional<Account> findByIbanForUpdate(@Param("iban") String iban);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("select a from Account a where a.iban in :ibans order by a.iban asc")
     List<Account> findAllByIbanInForUpdate(@Param("ibans") List<String> ibans);
}
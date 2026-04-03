package ro.axonsoft.eval.minibank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.axonsoft.eval.minibank.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}

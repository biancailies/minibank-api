package ro.axonsoft.eval.minibank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.axonsoft.eval.minibank.model.Transfer;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
}

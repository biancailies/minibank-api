package ro.axonsoft.eval.minibank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.axonsoft.eval.minibank.model.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    public Transfer searchByIdempotencyKey(String id);
}

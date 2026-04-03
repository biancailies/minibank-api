package ro.axonsoft.eval.minibank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.axonsoft.eval.minibank.model.Currency;
import ro.axonsoft.eval.minibank.model.Transfer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    @Query("""
        select t from Transfer t
        where (:iban is null or t.sourceIban = :iban or t.targetIban = :iban)
          and (:fromDate is null or t.createdAt >= :fromDate)
          and (:toDate is null or t.createdAt <= :toDate)
        order by t.createdAt asc, t.id asc
    """)
    Page<Transfer> search(@Param("iban") String iban, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, Pageable pageable);

    @Query("""
        select t from Transfer t
        where t.sourceIban = :sourceIban
          and t.createdAt >= :startOfDay
          and t.createdAt < :endOfDay
        order by t.createdAt asc, t.id asc
    """)
    List<Transfer> findOutgoingForDay(@Param("sourceIban") String sourceIban, @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);

    @Query("""
        select coalesce(sum(t.amount), 0)
        from Transfer t
        where t.sourceIban = :sourceIban
          and t.createdAt >= :startOfDay
          and t.createdAt < :endOfDay
          and t.currency = :currency
    """)
    BigDecimal sumOutgoingForDaySameCurrency(@Param("sourceIban") String sourceIban, @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay, @Param("currency") Currency currency);
}
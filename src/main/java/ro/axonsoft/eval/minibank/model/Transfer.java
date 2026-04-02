package ro.axonsoft.eval.minibank.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceIban;
    private String targetIban;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private Currency targetCurrency;

    private BigDecimal exchangeRate;
    private BigDecimal convertedAmount;
    private String idempotencyKey;
    private Instant createdAt;

    public Transfer() {}
}

package ro.axonsoft.eval.minibank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateTransferRequest {
    @NotBlank(message = "sourceIban is required")
    private String sourceIban;

    @NotBlank(message = "targetIban is required")
    private String targetIban;

    @NotNull(message = "amount is required")
    private BigDecimal amount;
    private String idempotencyKey;

    public CreateTransferRequest() {}

    public void setSourceIban(String sourceIban) {
        this.sourceIban = sourceIban;
    }

    public void setTargetIban(String targetIban) {
        this.targetIban = targetIban;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getSourceIban() {
        return sourceIban;
    }

    public String getTargetIban() {
        return targetIban;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}

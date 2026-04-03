package ro.axonsoft.eval.minibank.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.dto.CreateTransferRequest;
import ro.axonsoft.eval.minibank.dto.TransferResponse;
import ro.axonsoft.eval.minibank.dto.TransfersPageResponse;
import ro.axonsoft.eval.minibank.service.TransferService;

import java.time.Instant;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping("/{id}")
    public TransferResponse getTransferById(@PathVariable long id) {
        return transferService.getTransferById(id);
    }

    @GetMapping
    public TransfersPageResponse getTransfers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String iban, @RequestParam(required = false) Instant fromDate, @RequestParam(required = false) Instant toDate) {
        return transferService.getTransfers(iban, fromDate, toDate, page, size);
    }

    @PostMapping
    public TransferResponse createTransfer(@Valid @RequestBody CreateTransferRequest request){
        return transferService.createTransfer(request);
    }
}
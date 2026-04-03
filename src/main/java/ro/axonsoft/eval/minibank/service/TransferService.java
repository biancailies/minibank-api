package ro.axonsoft.eval.minibank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axonsoft.eval.minibank.dto.CreateTransferRequest;
import ro.axonsoft.eval.minibank.dto.TransferResponse;
import ro.axonsoft.eval.minibank.exception.BadRequestException;
import ro.axonsoft.eval.minibank.exception.ResourceNotFoundException;
import ro.axonsoft.eval.minibank.model.*;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransactionRepository;
import ro.axonsoft.eval.minibank.repository.TransferRepository;
import ro.axonsoft.eval.minibank.util.IbanValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

@Service
public class TransferService {
    private static final String SYSTEM_IBAN = "RO49AAAA1B31007593840000";

    private final ExchangeRateService exchangeRateService;

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(ExchangeRateService exchangeRateService, TransferRepository transferRepository, AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.exchangeRateService = exchangeRateService;
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request) {
        String sourceIban = request.getSourceIban();
        String targetIban = request.getTargetIban();
        BigDecimal amount = request.getAmount();
        String idempotencyKey = request.getIdempotencyKey();

        String normSourceIban = IbanValidator.normalizeIban(sourceIban).toUpperCase();
        String normTargetIban = IbanValidator.normalizeIban(targetIban).toUpperCase();

        if (!IbanValidator.isValidIban(normSourceIban)) {
            throw new BadRequestException("Invalid source IBAN");
        }

        if (!IbanValidator.isValidIban(normTargetIban)) {
            throw new BadRequestException("Invalid target IBAN");
        }

        String sourceCountryCode = IbanValidator.getCountryCode(normSourceIban);
        String targetCountryCode = IbanValidator.getCountryCode(normTargetIban);

        if (!IbanValidator.isSepaCountry(sourceCountryCode) || !IbanValidator.isSepaCountry(targetCountryCode)) {
            throw new BadRequestException("Only SEPA transfers are allowed");
        }

        if(amount.equals(BigDecimal.ZERO) || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }

        if(normSourceIban.equals(normTargetIban)) {
            throw new BadRequestException("Source Iban and Target Iban cannot be the same");
        }

        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transfer> existingTransfer = transferRepository.findByIdempotencyKey(idempotencyKey);

            if (existingTransfer.isPresent()) {
                Transfer transfer = existingTransfer.get();
                TransferResponse transferResponse = new TransferResponse();
                transferResponse.setSourceIban(transfer.getSourceIban());
                transferResponse.setTargetIban(transfer.getTargetIban());
                transferResponse.setAmount(transfer.getAmount());
                transferResponse.setCurrency(transfer.getCurrency());
                transferResponse.setTargetCurrency(transfer.getTargetCurrency());
                transferResponse.setExchangeRate(transfer.getExchangeRate());
                transferResponse.setConvertedAmount(transfer.getConvertedAmount());
                transferResponse.setIdempotencyKey(transfer.getIdempotencyKey());
                transferResponse.setCreatedAt(transfer.getCreatedAt());
                transferResponse.setId(transfer.getId());
                return transferResponse;
            }
        }

        Account sourceAccount = accountRepository.findByIban(normSourceIban);
        Account targetAccount = accountRepository.findByIban(normTargetIban);

        if (sourceAccount == null) {
            throw new ResourceNotFoundException("Source account not found");
        }

        if (targetAccount == null) {
            throw new ResourceNotFoundException("Target account not found");
        }

        if (!normSourceIban.equals(SYSTEM_IBAN)) {
            if (sourceAccount.getBalance().compareTo(amount) < 0) {
                throw new BadRequestException("Insufficient funds");
            }

            BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(amount);
            sourceAccount.setBalance(newSourceBalance);
            accountRepository.save(sourceAccount);
        }

        Currency currency = sourceAccount.getCurrency();
        Currency targetCurrency = targetAccount.getCurrency();
        BigDecimal exchangeRate = null;
        BigDecimal convertedAmount = null;

        if(!sourceAccount.getCurrency().equals(targetAccount.getCurrency())) {
            BigDecimal sourceToRon = exchangeRateService.getExchangeRate(sourceAccount.getCurrency());
            BigDecimal targetToRon = exchangeRateService.getExchangeRate(targetAccount.getCurrency());

            exchangeRate = sourceToRon.divide(targetToRon, 6, RoundingMode.HALF_EVEN);
            convertedAmount = amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_EVEN);
        }

        Transfer transfer = new Transfer();

        transfer.setSourceIban(normSourceIban);
        transfer.setTargetIban(normTargetIban);
        transfer.setAmount(amount);
        transfer.setCurrency(currency);
        transfer.setTargetCurrency(targetCurrency);
        transfer.setExchangeRate(exchangeRate);
        transfer.setConvertedAmount(convertedAmount);
        transfer.setIdempotencyKey(idempotencyKey);
        transfer.setCreatedAt(Instant.now());



        BigDecimal newTargetBalance;

        if (convertedAmount == null) {
            newTargetBalance = targetAccount.getBalance().add(amount);
        } else {
            newTargetBalance = targetAccount.getBalance().add(convertedAmount);
        }

        if(!targetAccount.getIban().equals(SYSTEM_IBAN)) {
            targetAccount.setBalance(newTargetBalance);
            accountRepository.save(targetAccount);
        }

        Transfer saveTransfer = transferRepository.save(transfer);

        if (sourceAccount.getIban().equals(SYSTEM_IBAN)) {
            Transaction newTransaction = new Transaction();
            newTransaction.setAccount(targetAccount);
            newTransaction.setTimestamp(saveTransfer.getCreatedAt());
            newTransaction.setType(TransactionType.DEPOSIT);

            if (convertedAmount == null) {
                newTransaction.setAmount(amount);
            } else {
                newTransaction.setAmount(convertedAmount);
            }

            newTransaction.setCurrency(targetAccount.getCurrency());
            newTransaction.setBalanceAfter(targetAccount.getBalance());
            newTransaction.setCounterpartyIban(null);
            newTransaction.setTransferId(saveTransfer.getId());

            transactionRepository.save(newTransaction);
        }
        else if (targetAccount.getIban().equals(SYSTEM_IBAN)) {
            Transaction newTransaction = new Transaction();
            newTransaction.setAccount(sourceAccount);
            newTransaction.setTimestamp(saveTransfer.getCreatedAt());
            newTransaction.setType(TransactionType.WITHDRAWAL);
            newTransaction.setAmount(amount);
            newTransaction.setCurrency(sourceAccount.getCurrency());
            newTransaction.setBalanceAfter(sourceAccount.getBalance());
            newTransaction.setCounterpartyIban(null);
            newTransaction.setTransferId(saveTransfer.getId());

            transactionRepository.save(newTransaction);
        }
        else {
            Transaction sourceTransaction = new Transaction();
            sourceTransaction.setAccount(sourceAccount);
            sourceTransaction.setTimestamp(saveTransfer.getCreatedAt());
            sourceTransaction.setType(TransactionType.TRANSFER_OUT);
            sourceTransaction.setAmount(amount);
            sourceTransaction.setCurrency(sourceAccount.getCurrency());
            sourceTransaction.setBalanceAfter(sourceAccount.getBalance());
            sourceTransaction.setCounterpartyIban(targetAccount.getIban());
            sourceTransaction.setTransferId(saveTransfer.getId());
            transactionRepository.save(sourceTransaction);

            Transaction targetTransaction = new Transaction();
            targetTransaction.setAccount(targetAccount);
            targetTransaction.setTimestamp(saveTransfer.getCreatedAt());
            targetTransaction.setType(TransactionType.TRANSFER_IN);

            if (convertedAmount == null) {
                targetTransaction.setAmount(amount);
            } else {
                targetTransaction.setAmount(convertedAmount);
            }

            targetTransaction.setCurrency(targetAccount.getCurrency());
            targetTransaction.setBalanceAfter(targetAccount.getBalance());
            targetTransaction.setCounterpartyIban(sourceAccount.getIban());
            targetTransaction.setTransferId(saveTransfer.getId());
            transactionRepository.save(targetTransaction);
        }

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setId(saveTransfer.getId());
        transferResponse.setSourceIban(saveTransfer.getSourceIban());
        transferResponse.setTargetIban(saveTransfer.getTargetIban());
        transferResponse.setAmount(saveTransfer.getAmount());
        transferResponse.setCurrency(saveTransfer.getCurrency());
        transferResponse.setTargetCurrency(saveTransfer.getTargetCurrency());
        transferResponse.setExchangeRate(saveTransfer.getExchangeRate());
        transferResponse.setConvertedAmount(saveTransfer.getConvertedAmount());
        transferResponse.setIdempotencyKey(saveTransfer.getIdempotencyKey());
        transferResponse.setCreatedAt(saveTransfer.getCreatedAt());

        return transferResponse;
    }

}

package ro.axonsoft.eval.minibank.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.axonsoft.eval.minibank.config.DataInitializer;
import ro.axonsoft.eval.minibank.dto.CreateTransferRequest;
import ro.axonsoft.eval.minibank.dto.TransferResponse;
import ro.axonsoft.eval.minibank.dto.TransfersPageResponse;
import ro.axonsoft.eval.minibank.exception.BadRequestException;
import ro.axonsoft.eval.minibank.exception.BusinessConflictException;
import ro.axonsoft.eval.minibank.exception.ResourceNotFoundException;
import ro.axonsoft.eval.minibank.model.Account;
import ro.axonsoft.eval.minibank.model.AccountType;
import ro.axonsoft.eval.minibank.model.Currency;
import ro.axonsoft.eval.minibank.model.Transaction;
import ro.axonsoft.eval.minibank.model.TransactionType;
import ro.axonsoft.eval.minibank.model.Transfer;
import ro.axonsoft.eval.minibank.repository.AccountRepository;
import ro.axonsoft.eval.minibank.repository.TransactionRepository;
import ro.axonsoft.eval.minibank.repository.TransferRepository;
import ro.axonsoft.eval.minibank.util.IbanValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TransferService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal SAVINGS_DAILY_LIMIT_EUR = new BigDecimal("5000.00");

    private final ExchangeRateService exchangeRateService;
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(
            ExchangeRateService exchangeRateService,
            TransferRepository transferRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository
    ) {
        this.exchangeRateService = exchangeRateService;
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest request) {
        String sourceIban = normalizeRequiredIban(request.getSourceIban(), "Invalid source IBAN");
        String targetIban = normalizeRequiredIban(request.getTargetIban(), "Invalid target IBAN");
        BigDecimal amount = normalizeAmount(request.getAmount());
        String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());

        if (sourceIban.equals(targetIban)) {
            throw new BadRequestException("Source IBAN and target IBAN cannot be the same");
        }

        if (!IbanValidator.isSepaCountry(IbanValidator.getCountryCode(sourceIban)) ||
                !IbanValidator.isSepaCountry(IbanValidator.getCountryCode(targetIban))) {
            throw new BusinessConflictException("Only SEPA transfers are allowed");
        }

        if (idempotencyKey != null) {
            Optional<Transfer> existing = transferRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return toTransferResponse(existing.get());
            }
        }

        List<String> ibansToLock = new ArrayList<>(List.of(sourceIban, targetIban));
        ibansToLock.sort(Comparator.naturalOrder());

        List<Account> lockedAccounts = accountRepository.findAllByIbanInForUpdate(ibansToLock);
        if (lockedAccounts.size() != 2) {
            boolean sourceExists = lockedAccounts.stream().anyMatch(a -> a.getIban().equals(sourceIban));
            boolean targetExists = lockedAccounts.stream().anyMatch(a -> a.getIban().equals(targetIban));

            if (!sourceExists) {
                throw new ResourceNotFoundException("Source account not found");
            }
            throw new ResourceNotFoundException("Target account not found");
        }

        Account sourceAccount = lockedAccounts.stream()
                .filter(a -> a.getIban().equals(sourceIban))
                .findFirst()
                .orElseThrow();

        Account targetAccount = lockedAccounts.stream()
                .filter(a -> a.getIban().equals(targetIban))
                .findFirst()
                .orElseThrow();

        if (!sourceIban.equals(DataInitializer.SYSTEM_IBAN) && sourceAccount.getAccountType() == AccountType.SAVINGS) {
            validateSavingsDailyLimit(sourceAccount, amount);
        }

        if (!sourceIban.equals(DataInitializer.SYSTEM_IBAN)) {
            if (sourceAccount.getBalance().compareTo(amount) < 0) {
                throw new BusinessConflictException("Insufficient funds");
            }
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount).setScale(2, RoundingMode.HALF_EVEN));
        }

        Currency sourceCurrency = sourceAccount.getCurrency();
        Currency targetCurrency = targetAccount.getCurrency();

        BigDecimal exchangeRate = null;
        BigDecimal convertedAmount = null;
        BigDecimal creditedAmount = amount;

        if (!sourceCurrency.equals(targetCurrency)) {
            BigDecimal sourceToRon = exchangeRateService.getExchangeRate(sourceCurrency);
            BigDecimal targetToRon = exchangeRateService.getExchangeRate(targetCurrency);

            exchangeRate = sourceToRon.divide(targetToRon, 6, RoundingMode.HALF_EVEN);
            convertedAmount = amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_EVEN);
            creditedAmount = convertedAmount;
        }

        if (!targetIban.equals(DataInitializer.SYSTEM_IBAN)) {
            targetAccount.setBalance(targetAccount.getBalance().add(creditedAmount).setScale(2, RoundingMode.HALF_EVEN));
        }

        Instant now = Instant.now();

        Transfer transfer = new Transfer();
        transfer.setSourceIban(sourceIban);
        transfer.setTargetIban(targetIban);
        transfer.setAmount(amount);
        transfer.setCurrency(sourceCurrency);
        transfer.setTargetCurrency(targetCurrency);
        transfer.setExchangeRate(exchangeRate);
        transfer.setConvertedAmount(convertedAmount);
        transfer.setIdempotencyKey(idempotencyKey);
        transfer.setCreatedAt(now);

        try {
            transfer = transferRepository.saveAndFlush(transfer);
        } catch (DataIntegrityViolationException e) {
            if (idempotencyKey != null) {
                Transfer existing = transferRepository.findByIdempotencyKey(idempotencyKey)
                        .orElseThrow(() -> e);
                return toTransferResponse(existing);
            }
            throw e;
        }

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        createTransactions(sourceAccount, targetAccount, transfer, amount, creditedAmount);

        return toTransferResponse(transfer);
    }

    public TransferResponse getTransferById(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        return toTransferResponse(transfer);
    }

    public TransfersPageResponse getTransfers(String iban, Instant fromDate, Instant toDate, int page, int size) {
        String normalizedIban = null;
        if (iban != null && !iban.isBlank()) {
            normalizedIban = IbanValidator.normalizeIban(iban);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Transfer> transferPage = transferRepository.search(normalizedIban, fromDate, toDate, pageable);

        List<TransferResponse> content = transferPage.getContent().stream()
                .map(this::toTransferResponse)
                .toList();

        TransfersPageResponse response = new TransfersPageResponse();
        response.setContent(content);
        response.setTotalElements(transferPage.getTotalElements());
        response.setTotalPages(transferPage.getTotalPages());
        response.setNumber(transferPage.getNumber());
        response.setSize(transferPage.getSize());

        return response;
    }

    private void validateSavingsDailyLimit(Account sourceAccount, BigDecimal currentAmount) {
        ZoneId zone = ZoneOffset.UTC;
        LocalDate today = LocalDate.now(zone);

        Instant startOfDay = today.atStartOfDay(zone).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(zone).toInstant();

        List<Transfer> transfersToday = transferRepository.findOutgoingForDay(
                sourceAccount.getIban(), startOfDay, endOfDay
        );

        BigDecimal totalEur = ZERO;

        for (Transfer transfer : transfersToday) {
            totalEur = totalEur.add(convertToEur(transfer.getAmount(), transfer.getCurrency()));
        }

        totalEur = totalEur.add(convertToEur(currentAmount, sourceAccount.getCurrency()))
                .setScale(2, RoundingMode.HALF_EVEN);

        if (totalEur.compareTo(SAVINGS_DAILY_LIMIT_EUR) > 0) {
            throw new BusinessConflictException("Daily savings transfer limit exceeded");
        }
    }

    private void createTransactions(Account sourceAccount, Account targetAccount, Transfer transfer,
                                    BigDecimal debitedAmount, BigDecimal creditedAmount) {

        if (sourceAccount.getIban().equals(DataInitializer.SYSTEM_IBAN)) {
            Transaction targetTransaction = new Transaction();
            targetTransaction.setAccount(targetAccount);
            targetTransaction.setTimestamp(transfer.getCreatedAt());
            targetTransaction.setType(TransactionType.DEPOSIT);
            targetTransaction.setAmount(creditedAmount);
            targetTransaction.setCurrency(targetAccount.getCurrency());
            targetTransaction.setBalanceAfter(targetAccount.getBalance());
            targetTransaction.setCounterpartyIban(null);
            targetTransaction.setTransferId(transfer.getId());
            transactionRepository.save(targetTransaction);
            return;
        }

        if (targetAccount.getIban().equals(DataInitializer.SYSTEM_IBAN)) {
            Transaction sourceTransaction = new Transaction();
            sourceTransaction.setAccount(sourceAccount);
            sourceTransaction.setTimestamp(transfer.getCreatedAt());
            sourceTransaction.setType(TransactionType.WITHDRAWAL);
            sourceTransaction.setAmount(debitedAmount);
            sourceTransaction.setCurrency(sourceAccount.getCurrency());
            sourceTransaction.setBalanceAfter(sourceAccount.getBalance());
            sourceTransaction.setCounterpartyIban(null);
            sourceTransaction.setTransferId(transfer.getId());
            transactionRepository.save(sourceTransaction);
            return;
        }

        Transaction sourceTransaction = new Transaction();
        sourceTransaction.setAccount(sourceAccount);
        sourceTransaction.setTimestamp(transfer.getCreatedAt());
        sourceTransaction.setType(TransactionType.TRANSFER_OUT);
        sourceTransaction.setAmount(debitedAmount);
        sourceTransaction.setCurrency(sourceAccount.getCurrency());
        sourceTransaction.setBalanceAfter(sourceAccount.getBalance());
        sourceTransaction.setCounterpartyIban(targetAccount.getIban());
        sourceTransaction.setTransferId(transfer.getId());
        transactionRepository.save(sourceTransaction);

        Transaction targetTransaction = new Transaction();
        targetTransaction.setAccount(targetAccount);
        targetTransaction.setTimestamp(transfer.getCreatedAt());
        targetTransaction.setType(TransactionType.TRANSFER_IN);
        targetTransaction.setAmount(creditedAmount);
        targetTransaction.setCurrency(targetAccount.getCurrency());
        targetTransaction.setBalanceAfter(targetAccount.getBalance());
        targetTransaction.setCounterpartyIban(sourceAccount.getIban());
        targetTransaction.setTransferId(transfer.getId());
        transactionRepository.save(targetTransaction);
    }

    private BigDecimal convertToEur(BigDecimal amount, Currency currency) {
        if (currency == Currency.EUR) {
            return amount.setScale(2, RoundingMode.HALF_EVEN);
        }

        BigDecimal sourceToRon = exchangeRateService.getExchangeRate(currency);
        BigDecimal eurToRon = exchangeRateService.getExchangeRate(Currency.EUR);

        BigDecimal rateToEur = sourceToRon.divide(eurToRon, 6, RoundingMode.HALF_EVEN);
        return amount.multiply(rateToEur).setScale(2, RoundingMode.HALF_EVEN);
    }

    private String normalizeRequiredIban(String iban, String message) {
        String normalized = IbanValidator.normalizeIban(iban);
        if (!IbanValidator.isValidIban(normalized)) {
            throw new BadRequestException(message);
        }
        return normalized;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("Amount is required");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    private String normalizeIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return key.trim();
    }

    private TransferResponse toTransferResponse(Transfer transfer) {
        TransferResponse response = new TransferResponse();
        response.setId(transfer.getId());
        response.setSourceIban(transfer.getSourceIban());
        response.setTargetIban(transfer.getTargetIban());
        response.setAmount(transfer.getAmount());
        response.setCurrency(transfer.getCurrency());
        response.setTargetCurrency(transfer.getTargetCurrency());
        response.setExchangeRate(transfer.getExchangeRate());
        response.setConvertedAmount(transfer.getConvertedAmount());
        response.setIdempotencyKey(transfer.getIdempotencyKey());
        response.setCreatedAt(transfer.getCreatedAt());
        return response;
    }
}
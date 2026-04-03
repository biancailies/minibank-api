package ro.axonsoft.eval.minibank.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.dto.AccountResponse;
import ro.axonsoft.eval.minibank.dto.AccountsPageResponse;
import ro.axonsoft.eval.minibank.dto.CreateAccountRequest;
import ro.axonsoft.eval.minibank.service.AccountService;


@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping
    public AccountsPageResponse getAllAccounts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return accountService.getAllAccounts(page, size);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccountById(@PathVariable long id) {
        return accountService.getAccountById(id);
    }

}

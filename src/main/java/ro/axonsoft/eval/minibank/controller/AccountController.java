package ro.axonsoft.eval.minibank.controller;

import org.springframework.web.bind.annotation.*;
import ro.axonsoft.eval.minibank.dto.AccountResponse;
import ro.axonsoft.eval.minibank.dto.AccountsPageResponse;
import ro.axonsoft.eval.minibank.dto.CreateAccountRequest;
import ro.axonsoft.eval.minibank.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public AccountsPageResponse getAllAccounts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        return accountService.getAllAccounts(page, size);
    }

    @GetMapping
    public AccountResponse getAccountById(@RequestParam long id) {
        return accountService.getAccountById(id);
    }
}

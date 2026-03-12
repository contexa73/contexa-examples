package io.contexa.example.shadowenforce.controller;

import io.contexa.example.shadowenforce.domain.Account;
import io.contexa.example.shadowenforce.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public List<Account> listAccounts() {
        return accountService.findAll();
    }

    @GetMapping("/{userId}")
    public Account getBalance(@PathVariable Long userId) {
        return accountService.getBalance(userId);
    }

    @PostMapping("/transfer")
    public Map<String, Object> transfer(
            @RequestParam Long fromUserId,
            @RequestParam Long toUserId,
            @RequestParam BigDecimal amount) {
        Account updated = accountService.transfer(fromUserId, toUserId, amount);
        return Map.of(
                "fromUserId", fromUserId,
                "remainingBalance", updated.getBalance(),
                "transferredAmount", amount,
                "toUserId", toUserId
        );
    }
}

package io.contexa.example.shadowenforce.service;

import io.contexa.contexacommon.annotation.Protectable;
import io.contexa.example.shadowenforce.domain.Account;
import io.contexa.example.shadowenforce.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Account service with @Protectable methods.
 *
 * Behavior differs based on security.zerotrust.mode:
 *
 *   SHADOW mode:
 *     - AI evaluates every request and logs the decision
 *     - SecurityDecisionEnforcementHandler.canHandle() returns false
 *     - All requests proceed regardless of AI decision
 *     - Use this period to collect HCAD baselines and verify AI accuracy
 *
 *   ENFORCE mode:
 *     - AI evaluates every request and ENFORCES the decision
 *     - SecurityDecisionEnforcementHandler stores decisions in cache
 *     - ZeroTrustAccessControlFilter checks cached decisions on subsequent requests
 *     - BLOCK -> 403, CHALLENGE -> 401 (MFA required), ESCALATE -> 423
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Protectable
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Protectable(ownerField = "userId")
    public Account getBalance(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found for userId: " + userId));
    }

    /**
     * Transfer funds between accounts.
     *
     * In SHADOW mode: always executes, AI decision logged only.
     * In ENFORCE mode: BLOCK/CHALLENGE/ESCALATE will prevent execution.
     *
     * AI evaluates: amount relative to history, recipient familiarity,
     * time of day, IP location, and behavioral baseline.
     */
    @Transactional
    @Protectable(ownerField = "userId", sync = true)
    public Account transfer(Long userId, Long toUserId, BigDecimal amount) {
        Account from = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Account not found for userId: " + userId));
        Account to = accountRepository.findByUserId(toUserId)
                .orElseThrow(() -> new RuntimeException("Recipient not found for userId: " + toUserId));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);
        return from;
    }
}

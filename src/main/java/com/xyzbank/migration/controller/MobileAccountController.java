package com.xyzbank.migration.controller;

import com.xyzbank.migration.dto.MobileAccountSummaryResponse;
import com.xyzbank.migration.model.AccountBalance;
import com.xyzbank.migration.service.AccountBalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/v1/accounts")
public class MobileAccountController {

    private final AccountBalanceService accountBalanceService;

    public MobileAccountController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    @GetMapping("/{accountId}/summary")
    public ResponseEntity<MobileAccountSummaryResponse> getSummary(@PathVariable String accountId) {
        AccountBalance accountBalance = accountBalanceService.getBalance(accountId);
        return ResponseEntity.ok(MobileAccountSummaryResponse.fromEntity(accountBalance));
    }
}

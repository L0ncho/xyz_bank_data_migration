package com.xyzbank.migration.controller;

import com.xyzbank.migration.dto.AtmBalanceResponse;
import com.xyzbank.migration.model.AccountBalance;
import com.xyzbank.migration.service.AccountBalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/atm/v1/accounts")
public class AtmBalanceController {

    private final AccountBalanceService accountBalanceService;

    public AtmBalanceController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<AtmBalanceResponse> getBalance(@PathVariable String accountId) {
        AccountBalance accountBalance = accountBalanceService.getBalance(accountId);
        return ResponseEntity.ok(AtmBalanceResponse.fromEntity(accountBalance));
    }
}

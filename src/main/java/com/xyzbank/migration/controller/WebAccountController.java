package com.xyzbank.migration.controller;

import com.xyzbank.migration.dto.WebAccountDetailsResponse;
import com.xyzbank.migration.model.AccountBalance;
import com.xyzbank.migration.service.AccountBalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/web/v1/accounts")
public class WebAccountController {

    private final AccountBalanceService accountBalanceService;

    public WebAccountController(AccountBalanceService accountBalanceService) {
        this.accountBalanceService = accountBalanceService;
    }

    @GetMapping("/{accountId}/details")
    public ResponseEntity<WebAccountDetailsResponse> getDetails(@PathVariable String accountId) {
        AccountBalance accountBalance = accountBalanceService.getBalance(accountId);
        return ResponseEntity.ok(WebAccountDetailsResponse.fromEntity(accountBalance));
    }
}

package com.xyzbank.migration.service;

import com.xyzbank.migration.exception.ResourceNotFoundException;
import com.xyzbank.migration.model.AccountBalance;
import com.xyzbank.migration.repository.AccountBalanceRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountBalanceService {

    private final AccountBalanceRepository accountBalanceRepository;

    public AccountBalanceService(AccountBalanceRepository accountBalanceRepository) {
        this.accountBalanceRepository = accountBalanceRepository;
    }

    public AccountBalance getBalance(String accountId) {
        return accountBalanceRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
    }
}

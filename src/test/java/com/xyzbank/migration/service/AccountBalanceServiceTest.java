package com.xyzbank.migration.service;

import com.xyzbank.migration.exception.ResourceNotFoundException;
import com.xyzbank.migration.model.AccountBalance;
import com.xyzbank.migration.repository.AccountBalanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

    /*
     * Cases:
     * 1. returnsFinalBalanceForExistingAccount
     * 2. throwsWhenAccountDoesNotExist
     * 3. GET /api/atm/v1/accounts/101/balance → 200 + ATM DTO
     * 4. unauthenticated → 401
     * 5. ROLE_WEB on ATM route → 403
     * 6. ROLE_ATM → 200
     */

    @Mock
    private AccountBalanceRepository accountBalanceRepository;

    @InjectMocks
    private AccountBalanceService accountBalanceService;

    @Test
    void returnsFinalBalanceForExistingAccount() {
        AccountBalance storedBalance = new AccountBalance();
        storedBalance.setAccountId("101");
        storedBalance.setFinalBalance(5050.00);
        when(accountBalanceRepository.findByAccountId("101"))
                .thenReturn(Optional.of(storedBalance));

        AccountBalance result = accountBalanceService.getBalance("101");

        assertEquals("101", result.getAccountId());
        assertEquals(5050.00, result.getFinalBalance());
    }

    @Test
    void throwsWhenAccountDoesNotExist() {
        when(accountBalanceRepository.findByAccountId("999"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountBalanceService.getBalance("999"));
    }
}

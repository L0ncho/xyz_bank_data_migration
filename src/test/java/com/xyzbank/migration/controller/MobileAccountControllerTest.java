package com.xyzbank.migration.controller;

import com.xyzbank.migration.config.SecurityConfig;
import com.xyzbank.migration.exception.ResourceNotFoundException;
import com.xyzbank.migration.model.AccountBalance;
import com.xyzbank.migration.service.AccountBalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MobileAccountController.class)
@Import(SecurityConfig.class)
class MobileAccountControllerTest {

    /*
     * Cases:
     * 1. GET /api/mobile/v1/accounts/101/summary → 200 + Mobile DTO
     * 2. unauthenticated → 401
     * 3. ROLE_ATM or ROLE_WEB on mobile route → 403
     * 4. account not found → 404
     */

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountBalanceService accountBalanceService;

    @Test
    @WithMockUser(roles = "MOBILE")
    void returnsAccountSummaryForExistingAccount() throws Exception {
        AccountBalance storedBalance = new AccountBalance();
        storedBalance.setAccountId("101");
        storedBalance.setName("John Doe");
        storedBalance.setType("ahorro");
        storedBalance.setFinalBalance(5050.00);
        when(accountBalanceService.getBalance("101")).thenReturn(storedBalance);

        mockMvc.perform(get("/api/mobile/v1/accounts/101/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("101"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.finalBalance").value(5050.00))
                .andExpect(jsonPath("$.type").value("ahorro"));
    }

    @Test
    void rejectsRequestWithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/mobile/v1/accounts/101/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WEB")
    void rejectsMobileAccessWhenRoleIsWeb() throws Exception {
        mockMvc.perform(get("/api/mobile/v1/accounts/101/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MOBILE")
    void returnsNotFoundWhenAccountDoesNotExist() throws Exception {
        when(accountBalanceService.getBalance("999"))
                .thenThrow(new ResourceNotFoundException("Account", "999"));

        mockMvc.perform(get("/api/mobile/v1/accounts/999/summary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account with id 999 not found"));
    }
}

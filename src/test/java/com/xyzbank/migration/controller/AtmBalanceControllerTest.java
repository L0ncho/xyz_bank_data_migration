package com.xyzbank.migration.controller;

import com.xyzbank.migration.config.SecurityConfig;
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

@WebMvcTest(AtmBalanceController.class)
@Import(SecurityConfig.class)
class AtmBalanceControllerTest {

    /*
     * Cases:
     * 3. GET /api/atm/v1/accounts/101/balance → 200 + ATM DTO
     * 4. unauthenticated → 401
     * 5. ROLE_WEB on ATM route → 403
     * 6. ROLE_ATM → 200
     */

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountBalanceService accountBalanceService;

    @Test
    @WithMockUser(roles = "ATM")
    void returnsAccountBalanceForExistingAccount() throws Exception {
        AccountBalance storedBalance = new AccountBalance();
        storedBalance.setAccountId("101");
        storedBalance.setFinalBalance(5050.00);
        when(accountBalanceService.getBalance("101")).thenReturn(storedBalance);

        mockMvc.perform(get("/api/atm/v1/accounts/101/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("101"))
                .andExpect(jsonPath("$.balance").value(5050.00));
    }

    @Test
    void rejectsRequestWithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/atm/v1/accounts/101/balance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "WEB")
    void rejectsAtmAccessWhenRoleIsWeb() throws Exception {
        mockMvc.perform(get("/api/atm/v1/accounts/101/balance"))
                .andExpect(status().isForbidden());
    }
}

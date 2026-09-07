package com.xyzbank.migration.dto;

import com.xyzbank.migration.model.AccountBalance;

public class AtmBalanceResponse {

    private String accountId;
    private double balance;

    public static AtmBalanceResponse fromEntity(AccountBalance accountBalance) {
        AtmBalanceResponse response = new AtmBalanceResponse();
        response.accountId = accountBalance.getAccountId();
        response.balance = accountBalance.getFinalBalance();
        return response;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}

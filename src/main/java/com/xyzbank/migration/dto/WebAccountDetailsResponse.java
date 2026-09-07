package com.xyzbank.migration.dto;

import com.xyzbank.migration.model.AccountBalance;

public class WebAccountDetailsResponse {

    private String accountId;
    private String name;
    private double finalBalance;
    private String type;

    public static WebAccountDetailsResponse fromEntity(AccountBalance accountBalance) {
        WebAccountDetailsResponse response = new WebAccountDetailsResponse();
        response.accountId = accountBalance.getAccountId();
        response.name = accountBalance.getName();
        response.finalBalance = accountBalance.getFinalBalance();
        response.type = accountBalance.getType();
        return response;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getFinalBalance() {
        return finalBalance;
    }

    public void setFinalBalance(double finalBalance) {
        this.finalBalance = finalBalance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

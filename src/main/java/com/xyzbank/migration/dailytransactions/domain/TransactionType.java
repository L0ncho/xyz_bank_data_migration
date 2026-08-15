package com.xyzbank.migration.dailytransactions.domain;

import com.xyzbank.migration.shared.domain.DomainError;

public enum TransactionType {
    DEBIT,
    CREDIT;

    public static TransactionType from(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw DomainError.validation("Transaction type cannot be empty");
        }
        return switch (raw.trim().toLowerCase()) {
            case "debito" -> DEBIT;
            case "credito" -> CREDIT;
            default -> throw DomainError.validation("Unknown transaction type: " + raw);
        };
    }
}

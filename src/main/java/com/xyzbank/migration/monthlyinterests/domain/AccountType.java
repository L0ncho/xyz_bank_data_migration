package com.xyzbank.migration.monthlyinterests.domain;

import com.xyzbank.migration.shared.domain.DomainError;

public enum AccountType {
    SAVINGS,
    LOAN,
    MORTGAGE;

    public static AccountType from(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw DomainError.validation("Account type cannot be empty");
        }
        return switch (raw.trim().toLowerCase()) {
            case "ahorro" -> SAVINGS;
            case "prestamo" -> LOAN;
            case "hipoteca" -> MORTGAGE;
            default -> throw DomainError.validation("Unknown account type: " + raw);
        };
    }
}

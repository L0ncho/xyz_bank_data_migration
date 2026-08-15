package com.xyzbank.migration.annualreports.domain;

import com.xyzbank.migration.shared.domain.DomainError;

public enum MovementType {
    DEPOSIT,
    WITHDRAWAL,
    PURCHASE;

    public static MovementType from(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw DomainError.validation("Movement type cannot be empty");
        }
        return switch (raw.trim().toLowerCase()) {
            case "deposito" -> DEPOSIT;
            case "retiro" -> WITHDRAWAL;
            case "compra" -> PURCHASE;
            default -> throw DomainError.validation("Unknown movement type: " + raw);
        };
    }

    public boolean isOutgoing() {
        return this == WITHDRAWAL || this == PURCHASE;
    }
}

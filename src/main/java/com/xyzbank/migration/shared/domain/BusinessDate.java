package com.xyzbank.migration.shared.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public final class BusinessDate {

    private static final DateTimeFormatter DASH = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final LocalDate value;

    private BusinessDate(LocalDate value) {
        this.value = value;
    }

    public static BusinessDate create(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw DomainError.validation("Date cannot be empty");
        }
        String normalized = raw.trim();
        try {
            if (normalized.contains("/")) {
                return new BusinessDate(LocalDate.parse(normalized, SLASH));
            }
            return new BusinessDate(LocalDate.parse(normalized, DASH));
        } catch (DateTimeParseException exception) {
            throw DomainError.validation("Invalid date format: " + raw);
        }
    }

    public LocalDate value() {
        return value;
    }

    public String asIso() {
        return value.format(DASH);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof BusinessDate businessDate)) {
            return false;
        }
        return value.equals(businessDate.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

package com.xyzbank.migration.shared.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TheBusinessDateTest {

    /*
     * Cases:
     * 1. Accepts dash formatted date
     * 2. Normalizes slash format to LocalDate
     * 3. Does not allow null date
     * 4. Does not allow blank date
     * 5. Does not allow invalid date format
     */

    @Nested
    class TheBusinessDate {

        @Test
        void acceptsDashFormattedDate() {
            BusinessDate date = BusinessDate.create("2024-01-01");

            assertEquals(LocalDate.of(2024, 1, 1), date.value());
        }

        @Test
        void normalizesSlashFormatToLocalDate() {
            BusinessDate date = BusinessDate.create("2024/01/15");

            assertEquals(LocalDate.of(2024, 1, 15), date.value());
            assertEquals("2024-01-15", date.asIso());
        }

        @Test
        void doesNotAllowNullDate() {
            assertThrows(DomainError.class, () -> BusinessDate.create(null));
        }

        @Test
        void doesNotAllowBlankDate() {
            assertThrows(DomainError.class, () -> BusinessDate.create("  "));
        }

        @Test
        void doesNotAllowInvalidDateFormat() {
            assertThrows(DomainError.class, () -> BusinessDate.create("01-01-2024"));
        }
    }
}

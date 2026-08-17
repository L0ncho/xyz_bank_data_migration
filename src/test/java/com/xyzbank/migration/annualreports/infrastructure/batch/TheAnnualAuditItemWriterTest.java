package com.xyzbank.migration.annualreports.infrastructure.batch;

import com.xyzbank.migration.annualreports.application.ports.InMemoryAnnualAuditWriter;
import com.xyzbank.migration.annualreports.domain.AnnualAccountSummary;
import com.xyzbank.migration.annualreports.domain.AnnualMovement;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TheAnnualAuditItemWriterTest {

    /*
     * Cases:
     * 1. Does not persist until close
     * 2. Consolidates same account across chunks on close
     */

    @Nested
    class TheAnnualAuditItemWriter {

        @Test
        void doesNotPersistUntilClose() throws Exception {
            InMemoryAnnualAuditWriter port = new InMemoryAnnualAuditWriter();
            AnnualAuditItemWriter writer = new AnnualAuditItemWriter(port);

            writer.write(Chunk.of(
                    AnnualMovement.create("101", "2024-01-01", "deposito", 1000, "Ingreso")
            ));

            assertTrue(port.written().isEmpty());
        }

        @Test
        void consolidatesSameAccountAcrossChunksOnClose() throws Exception {
            InMemoryAnnualAuditWriter port = new InMemoryAnnualAuditWriter();
            AnnualAuditItemWriter writer = new AnnualAuditItemWriter(port);

            writer.write(Chunk.of(
                    AnnualMovement.create("101", "2024-01-01", "deposito", 1000, "Ingreso")
            ));
            writer.write(Chunk.of(
                    AnnualMovement.create("101", "2024-03-15", "retiro", -500, "Retiro")
            ));
            writer.close();

            List<AnnualAccountSummary> summaries = port.written();
            assertEquals(1, summaries.size());
            assertEquals("101", summaries.get(0).accountIdValue());
            assertEquals(1000.0, summaries.get(0).totalDepositsValue());
            assertEquals(500.0, summaries.get(0).totalWithdrawalsValue());
            assertEquals(500.0, summaries.get(0).netBalanceValue());
            assertEquals(2, summaries.get(0).movementCount());
        }
    }
}

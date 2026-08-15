package com.xyzbank.migration.dailytransactions.infrastructure.batch;

import com.xyzbank.migration.dailytransactions.application.ports.InMemoryDailyReportWriter;
import com.xyzbank.migration.dailytransactions.domain.AnomalyDetector;
import com.xyzbank.migration.dailytransactions.domain.ProcessedTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBatchTest
@SpringBootTest
@TestPropertySource(properties = {
        "spring.batch.job.enabled=false",
        "spring.main.allow-bean-definition-overriding=true",
        "migration.data.daily-transactions=file:data/semana_1/transacciones.csv"
})
class TheDailyTransactionsJobTest {

    /*
     * Cases:
     * 1. Writes valid transactions and omits invalid ones
     */

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("dailyTransactionsJob")
    private Job dailyTransactionsJob;

    @Autowired
    private InMemoryDailyReportWriter dailyReportWriter;

    @Test
    void writesValidTransactionsAndOmitsInvalidOnes() throws Exception {
        jobLauncherTestUtils.setJob(dailyTransactionsJob);

        JobExecution execution = jobLauncherTestUtils.launchJob(
                new JobParametersBuilder()
                        .addLong("run.id", System.currentTimeMillis())
                        .toJobParameters()
        );

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(7, dailyReportWriter.written().size());
        assertTrue(dailyReportWriter.written().stream().anyMatch(ProcessedTransaction::hasAnomaly));
    }

    @TestConfiguration
    static class TestWriters {

        @Bean
        @Primary
        InMemoryDailyReportWriter dailyReportWriter() {
            return new InMemoryDailyReportWriter();
        }

        @Bean
        @Primary
        AnomalyDetector anomalyDetector() {
            return new AnomalyDetector();
        }
    }
}

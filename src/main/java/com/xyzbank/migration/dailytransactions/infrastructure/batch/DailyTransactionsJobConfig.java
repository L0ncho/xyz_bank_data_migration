package com.xyzbank.migration.dailytransactions.infrastructure.batch;

import com.xyzbank.migration.dailytransactions.application.ports.DailyReportWriter;
import com.xyzbank.migration.dailytransactions.domain.AnomalyDetector;
import com.xyzbank.migration.dailytransactions.domain.ProcessedTransaction;
import com.xyzbank.migration.dailytransactions.infrastructure.adapters.JdbcDailyReportWriter;
import com.xyzbank.migration.shared.application.ports.MigrationExecutionPort;
import com.xyzbank.migration.shared.domain.DomainError;
import com.xyzbank.migration.shared.infrastructure.batch.JobSummaryListener;
import com.xyzbank.migration.shared.infrastructure.batch.LoggingSkipListener;
import com.xyzbank.migration.shared.infrastructure.batch.MigrationGuardTasklet;
import com.xyzbank.migration.shared.infrastructure.batch.MigrationLedgerListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DailyTransactionsJobConfig {

    @Bean
    @StepScope
    public AnomalyDetector anomalyDetector() {
        return new AnomalyDetector();
    }

    @Bean
    public DailyReportWriter dailyReportWriter(JdbcTemplate jdbcTemplate) {
        return new JdbcDailyReportWriter(jdbcTemplate);
    }

    @Bean
    @StepScope
    public FlatFileItemReader<DailyTransactionLine> dailyTransactionReader(
            @Value("${migration.data.daily-transactions}") Resource resource
    ) {
        return new FlatFileItemReaderBuilder<DailyTransactionLine>()
                .name("dailyTransactionReader")
                .resource(resource)
                .linesToSkip(1)
                .delimited()
                .names("id", "fecha", "monto", "tipo")
                .targetType(DailyTransactionLine.class)
                .build();
    }

    @Bean
    @StepScope
    public DailyTransactionProcessor dailyTransactionProcessor(AnomalyDetector anomalyDetector) {
        return new DailyTransactionProcessor(anomalyDetector);
    }

    @Bean
    public DailyReportItemWriter dailyReportItemWriter(DailyReportWriter dailyReportWriter) {
        return new DailyReportItemWriter(dailyReportWriter);
    }

    @Bean
    public Step checkDailyMigrationNotDone(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            MigrationExecutionPort migrationExecutionPort
    ) {
        return new StepBuilder("checkDailyMigrationNotDone", jobRepository)
                .tasklet(new MigrationGuardTasklet(migrationExecutionPort, "dailyTransactionsJob"), transactionManager)
                .build();
    }

    @Bean
    public Step processDailyTransactions(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<DailyTransactionLine> dailyTransactionReader,
            DailyTransactionProcessor dailyTransactionProcessor,
            DailyReportItemWriter dailyReportItemWriter
    ) {
        return new StepBuilder("processDailyTransactions", jobRepository)
                .<DailyTransactionLine, ProcessedTransaction>chunk(10, transactionManager)
                .reader(dailyTransactionReader)
                .processor(dailyTransactionProcessor)
                .writer(dailyReportItemWriter)
                .faultTolerant()
                .processorNonTransactional()
                .skip(DomainError.class)
                .skip(FlatFileParseException.class)
                .skipLimit(100)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .listener(new LoggingSkipListener<DailyTransactionLine, ProcessedTransaction>())
                .build();
    }

    @Bean
    public Job dailyTransactionsJob(
            JobRepository jobRepository,
            Step checkDailyMigrationNotDone,
            Step processDailyTransactions,
            JobSummaryListener jobSummaryListener,
            MigrationLedgerListener migrationLedgerListener
    ) {
        return new JobBuilder("dailyTransactionsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobSummaryListener)
                .listener(migrationLedgerListener)
                .start(checkDailyMigrationNotDone)
                .on(MigrationGuardTasklet.alreadyMigratedExitCode).end()
                .from(checkDailyMigrationNotDone).on("*").to(processDailyTransactions)
                .end()
                .build();
    }
}

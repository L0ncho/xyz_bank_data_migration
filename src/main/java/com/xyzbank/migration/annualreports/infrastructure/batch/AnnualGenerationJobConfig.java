package com.xyzbank.migration.annualreports.infrastructure.batch;

import com.xyzbank.migration.annualreports.application.ports.AnnualAuditWriter;
import com.xyzbank.migration.annualreports.domain.AnnualMovement;
import com.xyzbank.migration.annualreports.infrastructure.adapters.JdbcAnnualAuditWriter;
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
public class AnnualGenerationJobConfig {

    @Bean
    public AnnualAuditWriter annualAuditWriter(JdbcTemplate jdbcTemplate) {
        return new JdbcAnnualAuditWriter(jdbcTemplate);
    }

    @Bean
    @StepScope
    public FlatFileItemReader<AnnualMovementLine> annualMovementReader(
            @Value("${migration.data.annual-accounts}") Resource resource
    ) {
        return new FlatFileItemReaderBuilder<AnnualMovementLine>()
                .name("annualMovementReader")
                .resource(resource)
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "fecha", "transaccion", "monto", "descripcion")
                .targetType(AnnualMovementLine.class)
                .build();
    }

    @Bean
    @StepScope
    public AnnualMovementProcessor annualMovementProcessor() {
        return new AnnualMovementProcessor();
    }

    @Bean
    @StepScope
    public AnnualAuditItemWriter annualAuditItemWriter(AnnualAuditWriter annualAuditWriter) {
        return new AnnualAuditItemWriter(annualAuditWriter);
    }

    @Bean
    public Step checkAnnualMigrationNotDone(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            MigrationExecutionPort migrationExecutionPort
    ) {
        return new StepBuilder("checkAnnualMigrationNotDone", jobRepository)
                .tasklet(new MigrationGuardTasklet(migrationExecutionPort, "annualGenerationJob"), transactionManager)
                .build();
    }

    @Bean
    public Step compileAnnualAudit(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<AnnualMovementLine> annualMovementReader,
            AnnualMovementProcessor annualMovementProcessor,
            AnnualAuditItemWriter annualAuditItemWriter
    ) {
        return new StepBuilder("compileAnnualAudit", jobRepository)
                .<AnnualMovementLine, AnnualMovement>chunk(10, transactionManager)
                .reader(annualMovementReader)
                .processor(annualMovementProcessor)
                .writer(annualAuditItemWriter)
                .faultTolerant()
                .processorNonTransactional()
                .skip(DomainError.class)
                .skip(FlatFileParseException.class)
                .skipLimit(100)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .listener(new LoggingSkipListener<AnnualMovementLine, AnnualMovement>())
                .build();
    }

    @Bean
    public Job annualGenerationJob(
            JobRepository jobRepository,
            Step checkAnnualMigrationNotDone,
            Step compileAnnualAudit,
            JobSummaryListener jobSummaryListener,
            MigrationLedgerListener migrationLedgerListener
    ) {
        return new JobBuilder("annualGenerationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobSummaryListener)
                .listener(migrationLedgerListener)
                .start(checkAnnualMigrationNotDone)
                .on(MigrationGuardTasklet.alreadyMigratedExitCode).end()
                .from(checkAnnualMigrationNotDone).on("*").to(compileAnnualAudit)
                .end()
                .build();
    }
}

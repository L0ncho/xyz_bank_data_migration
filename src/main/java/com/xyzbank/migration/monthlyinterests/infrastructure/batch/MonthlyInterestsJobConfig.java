package com.xyzbank.migration.monthlyinterests.infrastructure.batch;

import com.xyzbank.migration.monthlyinterests.application.ports.AccountBalanceWriter;
import com.xyzbank.migration.monthlyinterests.domain.InterestApplied;
import com.xyzbank.migration.shared.domain.DomainError;
import com.xyzbank.migration.shared.infrastructure.batch.JobSummaryListener;
import com.xyzbank.migration.shared.infrastructure.batch.LoggingSkipListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MonthlyInterestsJobConfig {

    @Bean
    public AccountBalanceWriter accountBalanceWriter() {
        return new LoggingAccountBalanceWriter();
    }

    @Bean
    public FlatFileItemReader<InterestAccountLine> monthlyInterestReader(
            @Value("${migration.data.monthly-interests}") Resource resource
    ) {
        return new FlatFileItemReaderBuilder<InterestAccountLine>()
                .name("monthlyInterestReader")
                .resource(resource)
                .linesToSkip(1)
                .delimited()
                .names("cuentaId", "nombre", "saldo", "edad", "tipo")
                .targetType(InterestAccountLine.class)
                .build();
    }

    @Bean
    public MonthlyInterestProcessor monthlyInterestProcessor() {
        return new MonthlyInterestProcessor();
    }

    @Bean
    public AccountBalanceItemWriter accountBalanceItemWriter(AccountBalanceWriter accountBalanceWriter) {
        return new AccountBalanceItemWriter(accountBalanceWriter);
    }

    @Bean
    public Step calculateMonthlyInterests(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<InterestAccountLine> monthlyInterestReader,
            MonthlyInterestProcessor monthlyInterestProcessor,
            AccountBalanceItemWriter accountBalanceItemWriter
    ) {
        return new StepBuilder("calculateMonthlyInterests", jobRepository)
                .<InterestAccountLine, InterestApplied>chunk(10, transactionManager)
                .reader(monthlyInterestReader)
                .processor(monthlyInterestProcessor)
                .writer(accountBalanceItemWriter)
                .faultTolerant()
                .processorNonTransactional()
                .skip(DomainError.class)
                .skip(FlatFileParseException.class)
                .skipLimit(100)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .listener(new LoggingSkipListener<InterestAccountLine, InterestApplied>())
                .build();
    }

    @Bean
    public Job monthlyInterestsJob(
            JobRepository jobRepository,
            Step calculateMonthlyInterests,
            JobSummaryListener jobSummaryListener
    ) {
        return new JobBuilder("monthlyInterestsJob", jobRepository)
                .listener(jobSummaryListener)
                .start(calculateMonthlyInterests)
                .build();
    }
}

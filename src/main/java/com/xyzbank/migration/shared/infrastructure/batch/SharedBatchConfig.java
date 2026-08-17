package com.xyzbank.migration.shared.infrastructure.batch;

import com.xyzbank.migration.shared.application.ports.MigrationExecutionPort;
import com.xyzbank.migration.shared.infrastructure.adapters.JdbcMigrationExecutionAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SharedBatchConfig {

    @Bean
    public MigrationExecutionPort migrationExecutionPort(JdbcTemplate jdbcTemplate) {
        return new JdbcMigrationExecutionAdapter(jdbcTemplate);
    }

    @Bean
    public JobSummaryListener jobSummaryListener() {
        return new JobSummaryListener();
    }

    @Bean
    public MigrationLedgerListener migrationLedgerListener(MigrationExecutionPort migrationExecutionPort) {
        return new MigrationLedgerListener(migrationExecutionPort);
    }
}

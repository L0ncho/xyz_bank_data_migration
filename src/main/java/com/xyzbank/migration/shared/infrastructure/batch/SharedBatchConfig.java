package com.xyzbank.migration.shared.infrastructure.batch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedBatchConfig {

    @Bean
    public JobSummaryListener jobSummaryListener() {
        return new JobSummaryListener();
    }
}

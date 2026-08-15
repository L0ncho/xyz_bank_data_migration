package com.xyzbank.migration.shared.infrastructure.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

public class JobSummaryListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(JobSummaryListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("Starting job={}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.info(
                "Finished job={} status={}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus()
        );
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            logger.info(
                    "Step summary name={} read={} write={} skip={} filter={}",
                    stepExecution.getStepName(),
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getSkipCount(),
                    stepExecution.getFilterCount()
            );
        }
    }
}

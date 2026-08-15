package com.xyzbank.migration.shared.infrastructure.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

public class LoggingSkipListener<T, S> implements SkipListener<T, S> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingSkipListener.class);

    @Override
    public void onSkipInRead(Throwable throwable) {
        logger.warn("Skipped during read: {}", throwable.getMessage());
    }

    @Override
    public void onSkipInProcess(T item, Throwable throwable) {
        logger.warn("Skipped during process item={} reason={}", item, throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(S item, Throwable throwable) {
        logger.warn("Skipped during write item={} reason={}", item, throwable.getMessage());
    }
}

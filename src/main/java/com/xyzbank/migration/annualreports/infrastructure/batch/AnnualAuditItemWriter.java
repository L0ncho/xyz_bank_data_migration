package com.xyzbank.migration.annualreports.infrastructure.batch;

import com.xyzbank.migration.annualreports.application.ports.AnnualAuditWriter;
import com.xyzbank.migration.annualreports.domain.AnnualAccountCompiler;
import com.xyzbank.migration.annualreports.domain.AnnualMovement;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

public class AnnualAuditItemWriter implements ItemWriter<AnnualMovement>, ItemStream {

    private final AnnualAuditWriter annualAuditWriter;
    private final List<AnnualMovement> bufferedMovements = new ArrayList<>();

    public AnnualAuditItemWriter(AnnualAuditWriter annualAuditWriter) {
        this.annualAuditWriter = annualAuditWriter;
    }

    @Override
    public void write(Chunk<? extends AnnualMovement> chunk) {
        bufferedMovements.addAll(chunk.getItems());
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    @Override
    public void close() throws ItemStreamException {
        if (bufferedMovements.isEmpty()) {
            return;
        }
        annualAuditWriter.write(AnnualAccountCompiler.compile(bufferedMovements));
        bufferedMovements.clear();
    }
}

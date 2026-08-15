package com.xyzbank.migration.annualreports.infrastructure.batch;

import com.xyzbank.migration.annualreports.application.ports.AnnualAuditWriter;
import com.xyzbank.migration.annualreports.domain.AnnualAccountCompiler;
import com.xyzbank.migration.annualreports.domain.AnnualMovement;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

public class AnnualAuditItemWriter implements ItemWriter<AnnualMovement> {

    private final AnnualAuditWriter annualAuditWriter;

    public AnnualAuditItemWriter(AnnualAuditWriter annualAuditWriter) {
        this.annualAuditWriter = annualAuditWriter;
    }

    @Override
    public void write(Chunk<? extends AnnualMovement> chunk) {
        List<AnnualMovement> movements = new ArrayList<>(chunk.getItems());
        annualAuditWriter.write(AnnualAccountCompiler.compile(movements));
    }
}

package com.xyzbank.migration.monthlyinterests.infrastructure.batch;

import com.xyzbank.migration.monthlyinterests.domain.Account;
import com.xyzbank.migration.monthlyinterests.domain.InterestApplied;
import com.xyzbank.migration.monthlyinterests.domain.InterestRatePolicy;
import com.xyzbank.migration.shared.domain.DomainError;
import org.springframework.batch.item.ItemProcessor;

import java.util.HashSet;
import java.util.Set;

public class MonthlyInterestProcessor implements ItemProcessor<InterestAccountLine, InterestApplied> {

    private final Set<String> seenAccountIds = new HashSet<>();

    @Override
    public InterestApplied process(InterestAccountLine line) {
        if (line.getSaldo() == null) {
            throw DomainError.validation("Account balance cannot be empty");
        }
        if (line.getEdad() == null) {
            throw DomainError.validation("Account age cannot be empty");
        }

        Account account = Account.create(
                line.getCuentaId(),
                line.getNombre(),
                line.getSaldo(),
                line.getEdad(),
                line.getTipo()
        );

        if (isDuplicateAccount(account.idValue())) {
            throw DomainError.validation("Duplicate account skipped: " + account.idValue());
        }

        return InterestRatePolicy.apply(account);
    }

    private boolean isDuplicateAccount(String accountId) {
        if (seenAccountIds.contains(accountId)) {
            return true;
        }
        seenAccountIds.add(accountId);
        return false;
    }
}

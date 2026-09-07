package com.xyzbank.migration.repository;

import com.xyzbank.migration.model.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, String> {

    Optional<AccountBalance> findByAccountId(String accountId);
}

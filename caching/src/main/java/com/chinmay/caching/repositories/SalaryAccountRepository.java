package com.chinmay.caching.repositories;

import com.chinmay.caching.entities.SalaryAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalaryAccountRepository extends JpaRepository<SalaryAccount, Long> {
    @Override
    // Using pessimistic locking to ensure that when we fetch a SalaryAccount for update,
    // it is locked for other transactions until the current transaction is complete.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SalaryAccount> findById(Long id);
}

package com.chinmay.caching.services.impl;

import com.chinmay.caching.dto.SalaryAccountDto;
import com.chinmay.caching.entities.Employee;
import com.chinmay.caching.entities.SalaryAccount;
import com.chinmay.caching.repositories.SalaryAccountRepository;
import com.chinmay.caching.services.interfaace.SalaryAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryAccountServiceImpl implements SalaryAccountService {

    private final SalaryAccountRepository salaryAccountRepository;
    private final ModelMapper modelMapper;

    @Override
    public void  createAccount(Employee employee) {
        log.info("Creating salary account for employee: {}", employee.getId());

        // Adding this to demonstrate the transactional integrity, if the employee name is "test" then we will throw an exception and the employee will
        // not be created in the database and also the salary account will not be created in the database.
        if (employee.getName().equals("test"))
            throw new RuntimeException("Testing exception for employee: " + employee.getId());

        // Here is the salary account is not getting created then the employee will not be created in the database and
        // we can maintain the integrity of the data in the database.
        SalaryAccount account = SalaryAccount.builder()
                .balance(employee.getSalary())
                .build();
        salaryAccountRepository.save(account);

        // Link the salary account back to the employee so it's visible in the response
        employee.setSalaryAccount(account);

        log.info("Successfully created salary account for employee: {}", employee.getId());
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)  // Only ONE transaction at a time
    public SalaryAccountDto incrementBalance(Long accountId) {
        SalaryAccount salaryAccount = salaryAccountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Salary account not found for account id: {}", accountId);
                    return new RuntimeException("Salary account not found for account id: " + accountId);
                });

        Long previousBalance = salaryAccount.getBalance();
        Long newBalance =  previousBalance + 1; // Incrementing balance by 1 for demonstration

        salaryAccount.setBalance(newBalance);
        SalaryAccount savedSalaryAccount = salaryAccountRepository.save(salaryAccount);

        return modelMapper.map(savedSalaryAccount, SalaryAccountDto.class);
    }
}

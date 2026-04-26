package com.chinmay.caching.services.interfaace;

import com.chinmay.caching.dto.SalaryAccountDto;
import com.chinmay.caching.entities.Employee;

public interface SalaryAccountService {
    void  createAccount(Employee employee);

    SalaryAccountDto incrementBalance(Long employeeId);
}

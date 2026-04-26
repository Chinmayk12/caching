package com.chinmay.caching.services.impl;

import com.chinmay.caching.dto.EmployeeDto;
import com.chinmay.caching.entities.Employee;
import com.chinmay.caching.exceptions.ResourceNotFoundException;
import com.chinmay.caching.repositories.EmployeeRepository;
import com.chinmay.caching.repositories.SalaryAccountRepository;
import com.chinmay.caching.services.interfaace.EmployeeService;
import com.chinmay.caching.services.interfaace.SalaryAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SalaryAccountRepository salaryAccountRepository;
    private final ModelMapper modelMapper;
    private final SalaryAccountService salaryAccountService;
    private final String CACHE_NAME = "employees";

    @Override
    // Caching Ahead ------------------------------------------------------------------------------
    // THis annotation tells Spring to cache the result of this method using the "employees"
    // cache and the employee ID as the key.
    @Cacheable(cacheNames = CACHE_NAME,key = "#id")
    //---------------------------------------------------------------------------------------------
    public EmployeeDto getEmployeeById(Long id) {
        log.info("Fetching employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee not found with id: {}", id);
                    return new ResourceNotFoundException("Employee not found with id: " + id);
                });
        log.info("Successfully fetched employee with id: {}", id);
        return modelMapper.map(employee, EmployeeDto.class);
    }

    @Override
    @CachePut(cacheNames = CACHE_NAME , key = "#result.id")
    // Here to cache a result we use result keyword in expression language to specify that the result
    // that is obtained from EmployeeDto is to be cached with the employee ID as the key.
    // This ensures that when a new employee is created,
    @Transactional
    // By adding a transactional annotation  we can maintain the integrity by  if the employee salary account is not getting created
    // then at same time the user would be not created in the database, and we can maintain the integrity of the data in the database.
    public EmployeeDto createNewEmployee(EmployeeDto employeeDto) {
        log.info("Creating new employee with email: {}", employeeDto.getEmail());
        List<Employee> existingEmployees = employeeRepository.findByEmail(employeeDto.getEmail());

        if (!existingEmployees.isEmpty()) {
            log.error("Employee already exists with email: {}", employeeDto.getEmail());
            throw new RuntimeException("Employee already exists with email: " + employeeDto.getEmail());
        }
        Employee newEmployee = modelMapper.map(employeeDto, Employee.class);
        Employee savedEmployee = employeeRepository.save(newEmployee);


        // Creating a new salary account for the current user
        salaryAccountService.createAccount(savedEmployee);
        
        log.info("Successfully created new employee with id: {}", savedEmployee.getId());
        return modelMapper.map(savedEmployee, EmployeeDto.class);
    }

    @Override
    @CachePut(cacheNames = CACHE_NAME, key = "#id")    // Updating Cache
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        log.info("Updating employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee not found with id: {}", id);
                    return new ResourceNotFoundException("Employee not found with id: " + id);
                });

        if (employeeDto.getEmail() != null && !employee.getEmail().equals(employeeDto.getEmail())) {
            log.error("Attempted to update email for employee with id: {}", id);
            throw new RuntimeException("The email of the employee cannot be updated");
        }

        // Update fields
        if (employeeDto.getName() != null) {
            employee.setName(employeeDto.getName());
        }
        if (employeeDto.getSalary() != null) {
            employee.setSalary(employeeDto.getSalary());
        }
        
        employee.setId(id);

        Employee savedEmployee = employeeRepository.save(employee);
        EmployeeDto result = modelMapper.map(savedEmployee, EmployeeDto.class);
        log.info("Successfully updated employee with id: {}. Cache will be updated with: {}", id, result);
        return result;
    }

    @Override
    // Deleting specific id cache entry when an employee is deleted from the database.
    // This ensures that the cache remains consistent with the database.
    @CacheEvict(cacheNames = CACHE_NAME , key = "#id")
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with id: {}", id);
        boolean exists = employeeRepository.existsById(id);

        if (!exists) {
            log.error("Employee not found with id: {}", id);
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }

        employeeRepository.deleteById(id);
        log.info("Successfully deleted employee with id: {}", id);
    }

}
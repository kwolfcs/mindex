package com.mindex.challenge.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.exception.DuplicateEntryException;
import com.mindex.challenge.exception.EmployeeNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    public void testCreateEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeId("123");
        when(employeeRepository.insert(any(Employee.class))).thenAnswer(invocation -> {
            Employee arg = invocation.getArgument(0);
            arg.setEmployeeId("123");
            return arg;
        });

        Employee created = employeeService.create(employee);

        assertNotNull(created);
        assertEquals("123", created.getEmployeeId());

        verify(employeeRepository).insert(created);
    }

    @Test
    public void testCreateEmployeeDuplicate() {
        Employee employee = new Employee();
        when(employeeRepository.insert(any(Employee.class)))
                .thenThrow(new DuplicateEntryException("duplicate"));

        assertThrows(DuplicateEntryException.class, () -> {
            employeeService.create(employee);
        });
    }

    @Test
    public void testReadEmployee() {
        Employee employee = new Employee();

        when(employeeRepository.findByEmployeeId(employee.getEmployeeId())).thenReturn(employee);

        Employee result = employeeService.read(employee.getEmployeeId());

        assertNotNull(result);
        assertEquals(employee.getEmployeeId(), result.getEmployeeId());
    }

    @Test
    public void testReadByEmployeeNotFound() {
        when(employeeRepository.findByEmployeeId("a")).thenReturn(null);

        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeService.read("a");
        });
    }

    @Test
    public void testUpdateEmployee() {
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");

        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee updatedEmployee = employeeService.update(employee);

        assertNotNull(updatedEmployee);
        assertEquals("John", updatedEmployee.getFirstName());
        assertEquals("Doe", updatedEmployee.getLastName());

        verify(employeeRepository, times(1)).save(employee);
    }
}
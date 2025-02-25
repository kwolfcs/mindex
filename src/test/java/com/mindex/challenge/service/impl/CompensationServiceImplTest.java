package com.mindex.challenge.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.exception.CompensationNotFoundException;
import com.mindex.challenge.exception.DuplicateEntryException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CompensationServiceImplTest {

    @Mock
    private CompensationRepository compensationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private CompensationServiceImpl compensationService;

    @Test
    public void testCreateCompensation() {
        Employee employee = new Employee();
        employee.setEmployeeId("employee");

        // need employee id to be present for compensation to work
        when(employeeRepository.findByEmployeeId("employee")).thenReturn(employee);

        Compensation compensation = new Compensation();
        compensation.setCompensationId("123");
        compensation.setEmployeeId("employee")
        ;
        when(compensationRepository.insert(any(Compensation.class))).thenAnswer(invocation -> {
            Compensation arg = invocation.getArgument(0);
            arg.setCompensationId("123");
            return arg;
        });

        Compensation created = compensationService.create(compensation);

        assertNotNull(created);
        assertEquals("123", created.getCompensationId());

        verify(employeeRepository).findByEmployeeId("employee");
        verify(compensationRepository).insert(created);
    }

    @Test
    public void testCreateCompensationDuplicate() {
        Employee employee = new Employee();
        employee.setEmployeeId("employee");

        // need employee id to be present for compensation to work
        when(employeeRepository.findByEmployeeId("employee")).thenReturn(employee);

        Compensation compensation = new Compensation();
        compensation.setEmployeeId("employee");

        when(compensationRepository.insert(any(Compensation.class)))
                .thenThrow(new DuplicateEntryException("duplicate"));

        assertThrows(DuplicateEntryException.class, () -> {
            compensationService.create(compensation);
        });
    }

    @Test
    public void testReadByEmployee() {
        Compensation comp1 = new Compensation();
        comp1.setEmployeeId("a");
        Compensation comp2 = new Compensation();
        comp2.setEmployeeId("a");
        List<Compensation> compensationList = new ArrayList<>();
        compensationList.add(comp1);
        compensationList.add(comp2);

        when(compensationRepository.findByEmployeeId("a")).thenReturn(compensationList);

        List<Compensation> result = compensationService.readByEmployee("a");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testReadByEmployeeCompensationNotFound() {
        when(compensationRepository.findByEmployeeId("a")).thenReturn(null);

        assertThrows(CompensationNotFoundException.class, () -> {
            compensationService.readByEmployee("a");
        });
    }
}
package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.exception.EmployeeNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReportingStructureServiceImplTest {

    @InjectMocks
    private ReportingStructureServiceImpl reportingStructureService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    public void testEmployeeNotFound() {
        String nonExistingId = "nonexistent";
        when(employeeRepository.findByEmployeeId(nonExistingId)).thenReturn(null);

        assertThrows(EmployeeNotFoundException.class, () -> {
            reportingStructureService.get(nonExistingId);
        });
    }

    @Test
    public void testNoReports() {
        // Create employee with no direct reports
        Employee employee = new Employee();
        employee.setEmployeeId("A");
        employee.setDirectReports(Collections.emptyList());

        when(employeeRepository.findByEmployeeId("A")).thenReturn(employee);

        ReportingStructure reportingStructure = reportingStructureService.get("A");

        assertEquals(0, reportingStructure.numberOfReports());
        assertEquals(employee, reportingStructure.employee());
    }

    @Test
    public void testWithReports() {
        // Create employee tree
        Employee employeeA = new Employee();
        employeeA.setEmployeeId("John Lennon");

        Employee employeeB = new Employee();
        employeeB.setEmployeeId("Paul McCartney");

        Employee employeeC = new Employee();
        employeeC.setEmployeeId("Ringo Starr");

        Employee employeeD = new Employee();
        employeeD.setEmployeeId("Pete Best");

        Employee employeeE = new Employee();
        employeeE.setEmployeeId("George Harrison");
        employeeE.setDirectReports(new ArrayList<Employee>()); // test empty direct report (non - null), shouldn't matter

        employeeA.setDirectReports(Arrays.asList(employeeB, employeeC));
        employeeC.setDirectReports(Arrays.asList(employeeD, employeeE));

        when(employeeRepository.findByEmployeeId("John Lennon")).thenReturn(employeeA);
        when(employeeRepository.findByEmployeeId("Paul McCartney")).thenReturn(employeeB);
        when(employeeRepository.findByEmployeeId("Ringo Starr")).thenReturn(employeeC);
        when(employeeRepository.findByEmployeeId("Pete Best")).thenReturn(employeeD);
        when(employeeRepository.findByEmployeeId("George Harrison")).thenReturn(employeeE);

        ReportingStructure reportingStructure = reportingStructureService.get("John Lennon");

        assertEquals(4, reportingStructure.numberOfReports());
        assertEquals(employeeA, reportingStructure.employee());
    }

    @Test
    public void testCycleDetection() {
        // Create cycle - assume hierarchical direct report structure, otherwise breaks recursion
        Employee employeeA = new Employee();
        employeeA.setEmployeeId("A");

        Employee employeeB = new Employee();
        employeeB.setEmployeeId("B");

        Employee employeeC = new Employee();
        employeeC.setEmployeeId("C");

        employeeA.setDirectReports(Collections.singletonList(employeeB));
        employeeB.setDirectReports(Collections.singletonList(employeeC));
        employeeC.setDirectReports(Collections.singletonList(employeeA)); // Cycle A->B->C->A

        when(employeeRepository.findByEmployeeId("A")).thenReturn(employeeA);
        when(employeeRepository.findByEmployeeId("B")).thenReturn(employeeB);
        when(employeeRepository.findByEmployeeId("C")).thenReturn(employeeC);

        // expect exception if cycle is detected
        assertThrows(IllegalStateException.class, () -> reportingStructureService.get("A"));
    }

    @Test
    public void falseCycleDetection() {
        // more complex case where A has report B and C, and B has report C.
        // Should work as it's not a 'cycle', and C should only be counted ONCE
        Employee employeeA = new Employee();
        employeeA.setEmployeeId("A");

        Employee employeeB = new Employee();
        employeeB.setEmployeeId("B");

        Employee employeeC = new Employee();
        employeeC.setEmployeeId("C");

        ArrayList<Employee> reports = new ArrayList<>();
        reports.add(employeeB);
        reports.add(employeeC);

        employeeA.setDirectReports(reports);
        employeeB.setDirectReports(Collections.singletonList(employeeC));

        when(employeeRepository.findByEmployeeId("A")).thenReturn(employeeA);
        when(employeeRepository.findByEmployeeId("B")).thenReturn(employeeB);
        when(employeeRepository.findByEmployeeId("C")).thenReturn(employeeC);

        assertEquals(2, reportingStructureService.get("A").numberOfReports());
    }

    @Test
    public void testReportEmployeeNotFound() {
        // Create employee with non-existent direct report
        Employee employeeA = new Employee();
        employeeA.setEmployeeId("A");

        Employee employeeB = new Employee();
        employeeB.setEmployeeId("B");

        employeeA.setDirectReports(Collections.singletonList(employeeB));

        when(employeeRepository.findByEmployeeId("A")).thenReturn(employeeA);
        when(employeeRepository.findByEmployeeId("B")).thenReturn(null); // Employee B not found

        // exception expected if ID isn't associated with an employee
        assertThrows(IllegalStateException.class, () -> reportingStructureService.get("A"));
    }

}
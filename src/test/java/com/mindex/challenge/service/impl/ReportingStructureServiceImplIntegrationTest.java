package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.ReportingStructureService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportingStructureServiceImplIntegrationTest {

    private String reportingStructureUrl;

    @Autowired
    private ReportingStructureService reportingStructureService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        reportingStructureUrl = "http://localhost:" + port + "/employee/{id}/reportingStructure";
    }

    @Test
    public void testEmployeeNotFoundIntegration() {
        ResponseEntity<String> response = restTemplate.getForEntity(reportingStructureUrl, String.class, "nonexistent");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testNoReportsIntegration() {
        Employee testEmployee = new Employee();
        testEmployee.setEmployeeId("62c1084e-6e34-4630-93fd-9153afb65309");

        ReportingStructure testReportingStructure = new ReportingStructure(testEmployee, 0);

        ReportingStructure readReportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, "62c1084e-6e34-4630-93fd-9153afb65309").getBody();
        assertEquals(testReportingStructure.employee().getEmployeeId(), readReportingStructure.employee().getEmployeeId());
        assertEquals(testReportingStructure.numberOfReports(), readReportingStructure.numberOfReports());
    }

    @Test
    public void testWithReportsIntegration() {
        Employee testEmployee = new Employee();
        testEmployee.setEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");

        ReportingStructure testReportingStructure = new ReportingStructure(testEmployee, 4);

        ReportingStructure readReportingStructure = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, "16a596ae-edd3-4847-99fe-c4518e82c86f").getBody();
        assertEquals(testReportingStructure.employee().getEmployeeId(), readReportingStructure.employee().getEmployeeId());
        assertEquals(testReportingStructure.numberOfReports(), readReportingStructure.numberOfReports());
    }

    @Test
    public void testCycleDetectionIntegration() {
        Employee employeeA = new Employee();
        employeeA.setEmployeeId("a");

        Employee employeeB = new Employee();
        employeeB.setEmployeeId("b");

        Employee employeeC = new Employee();
        employeeC.setEmployeeId("c");

        employeeRepository.insert(employeeA);
        employeeRepository.insert(employeeB);
        employeeRepository.insert(employeeC);

        Employee refA = new Employee();
        refA.setEmployeeId("a");

        Employee refB = new Employee();
        refB.setEmployeeId("b");

        Employee refC = new Employee();
        refC.setEmployeeId("c");

        employeeA.setDirectReports(new ArrayList<>(List.of(refB)));
        employeeB.setDirectReports(new ArrayList<>(List.of(refC)));
        employeeC.setDirectReports(new ArrayList<>(List.of(refA)));

        employeeRepository.save(employeeA);
        employeeRepository.save(employeeB);
        employeeRepository.save(employeeC);

        ResponseEntity<String> response = restTemplate.getForEntity(reportingStructureUrl, String.class, "a");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testReportEmployeeNotFoundIntegration() {
        Employee employeeA = new Employee();
        employeeA.setEmployeeId("exists");
        Employee employeeB = new Employee();
        employeeB.setEmployeeId("notExists");

        employeeRepository.insert(employeeA);

        Employee refA = new Employee();
        refA.setEmployeeId("exists");

        Employee refB = new Employee();
        refB.setEmployeeId("notExists");

        employeeA.setDirectReports(new ArrayList<>(List.of(refB)));
        employeeRepository.save(employeeA);

        ResponseEntity<String> response = restTemplate.getForEntity(reportingStructureUrl, String.class, "exists");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}

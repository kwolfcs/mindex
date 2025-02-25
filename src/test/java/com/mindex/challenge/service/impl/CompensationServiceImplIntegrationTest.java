package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompensationRepository compensationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private String compensationUrl;
    private String compensationByEmployeeUrl;

    @Before
    public void setup() {
        compensationRepository.deleteAll();
        compensationUrl = "/compensation";
        compensationByEmployeeUrl = "/compensation/e/{id}";
    }

    @Test
    public void testCreateCompensationIntegration() {
        Employee employee = new Employee();
        employee.setEmployeeId("a");
        employeeRepository.save(employee);

        Compensation compensation = new Compensation();
        compensation.setEmployeeId("a");

        ResponseEntity<Compensation> comp = restTemplate.postForEntity(compensationUrl, compensation, Compensation.class);
        List<Compensation> response = compensationRepository.findByEmployeeId("a");

        assertEquals(HttpStatus.OK, comp.getStatusCode());
        assertNotNull(response);
        assertNotNull(response.get(0).getCompensationId());
        assertEquals("a", response.get(0).getEmployeeId());
        assertEquals(1, response.size());
    }

    @Test
    public void testReadByEmployeeIntegration() {
        Employee employee1 = new Employee();
        employee1.setEmployeeId("a");
        employeeRepository.save(employee1);
        Employee employee2 = new Employee();
        employee2.setEmployeeId("b");
        employeeRepository.save(employee2);

        Compensation compensation1 = new Compensation();
        compensation1.setEmployeeId("a");
        Compensation compensation2 = new Compensation();
        compensation2.setEmployeeId("a");
        Compensation compensation3 = new Compensation();
        compensation3.setEmployeeId("b");

        restTemplate.postForEntity(compensationUrl, compensation1, Compensation.class);
        restTemplate.postForEntity(compensationUrl, compensation2, Compensation.class);
        restTemplate.postForEntity(compensationUrl, compensation3, Compensation.class);

        ResponseEntity<Compensation[]> get1 = restTemplate.getForEntity(compensationByEmployeeUrl, Compensation[].class, "a");
        ResponseEntity<Compensation[]> get2 = restTemplate.getForEntity(compensationByEmployeeUrl, Compensation[].class, "b");

        assertEquals(HttpStatus.OK, get1.getStatusCode());
        assertEquals(HttpStatus.OK, get2.getStatusCode());
        assertEquals(2, get1.getBody().length);
        assertEquals(1, get2.getBody().length);
        assertEquals("b", get2.getBody()[0].getEmployeeId());
    }

    @Test
    public void testReadByEmployeeCompensationNotFoundIntegration() {
        ResponseEntity<String> get1 = restTemplate.getForEntity(compensationByEmployeeUrl, String.class, "a");
        assertEquals(HttpStatus.NOT_FOUND, get1.getStatusCode());
    }
}
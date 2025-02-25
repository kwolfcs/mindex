package com.mindex.challenge.controller;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import com.mindex.challenge.service.ReportingStructureService;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class EmployeeController {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ReportingStructureService reportingStructureService;

    @PostMapping("/employee")
    public ResponseEntity<Employee> create(@RequestBody(required=false) Employee employee) {
        LOG.debug("Received employee create request for [{}]", employee);

        Employee createdEmployee = employeeService.create(employee);
        return ResponseEntity.ok(createdEmployee);
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<Employee> read(@PathVariable @NotBlank(message = "Employee ID must not be blank") String id) {
        LOG.debug("Received employee read request for id [{}]", id);

        Employee employee = employeeService.read(id);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/employee/{id}")
    public ResponseEntity<Employee> update(@PathVariable @NotBlank(message = "Employee ID must not be blank") String id, @RequestBody Employee employee) {
        LOG.debug("Received employee update request for id [{}] and employee [{}]", id, employee);

        employee.setEmployeeId(id);
        Employee updatedEmployee = employeeService.update(employee);
        return ResponseEntity.ok(updatedEmployee);
    }

    @GetMapping("/employee/{id}/reportingStructure")
    public ResponseEntity<ReportingStructure> getReportingStructure(@PathVariable @NotBlank(message = "Employee ID must not be blank") String id) {
        LOG.debug("Received employee reporting structure get request for id [{}]", id);

        ReportingStructure reportingStructure = reportingStructureService.get(id);
        return ResponseEntity.ok(reportingStructure);
    }
}

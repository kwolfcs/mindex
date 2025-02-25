package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.exception.EmployeeNotFoundException;
import com.mindex.challenge.service.ReportingStructureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReportingStructureServiceImpl implements ReportingStructureService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingStructureServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public ReportingStructure get(String id) {
        LOG.debug("Getting reporting structure for id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);
        if (employee == null) {
            throw new EmployeeNotFoundException("Employee not found for ID: " + id);
        }

        Map<String, Integer> cache = new HashMap<>();
        int reportsNum = findAllReports(employee, cache);

        return new ReportingStructure(employee, reportsNum);
    }

    private int findAllReports(Employee employee, Map<String, Integer> cache) {
        if (employee.getDirectReports() == null) {
            cache.putIfAbsent(employee.getEmployeeId(), 0);
            return 0;
        }

        if (cache.containsKey(employee.getEmployeeId())) {
            Integer result = cache.get(employee.getEmployeeId());
            if (result == -1) {
                throw new IllegalStateException("Cycle detected for employee ID: " + employee.getEmployeeId());
            }
            return result;
        }

        // signify employee is being processed - this will catch a report loop
        cache.put(employee.getEmployeeId(), -1);

        int reportsNum = 0;
        for (Employee e : employee.getDirectReports()) {
            // since JSON only has employee ID for a direct report, need to re-fetch employee based on ID
            // This way, gets reports of reports.
            Employee fullReport = employeeRepository.findByEmployeeId(e.getEmployeeId());
            if (fullReport == null) {
                throw new IllegalStateException("Employee not found for ID: " + e.getEmployeeId());
            }

            // only increment by one if report is unique - NOT counting duplicate entries here
            // example: A has reports B and C, and B has report C
            if (!cache.containsKey(fullReport.getEmployeeId())) {
                reportsNum++;
            }

            reportsNum += findAllReports(fullReport, cache);
        }

        cache.put(employee.getEmployeeId(), reportsNum);

        return reportsNum;
    }
}

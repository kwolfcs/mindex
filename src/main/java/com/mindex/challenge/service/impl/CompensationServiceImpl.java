package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.exception.CompensationNotFoundException;
import com.mindex.challenge.exception.DuplicateEntryException;
import com.mindex.challenge.exception.EmployeeIDForCompensationNotFoundException;
import com.mindex.challenge.exception.EmployeeNotFoundException;
import com.mindex.challenge.service.CompensationService;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        if (employeeRepository.findByEmployeeId(compensation.getEmployeeId()) == null) {
            throw new EmployeeIDForCompensationNotFoundException("Employee ID does not exist for compensation: " + compensation.getEmployeeId());
        }

        compensation.setCompensationId(UUID.randomUUID().toString());

        try {
            compensationRepository.insert(compensation);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new DuplicateEntryException("Compensation ID already exists: " + compensation.getCompensationId());
        }
        return compensation;
    }

    @Override
    public List<Compensation> readByEmployee(String employeeId){
        LOG.debug("Reading all compensations for employee with id [{}]", employeeId);

        List<Compensation> compensations = compensationRepository.findByEmployeeId(employeeId);
        if (compensations == null || compensations.isEmpty()) {
            throw new CompensationNotFoundException("No compensations exist for employee ID: " + employeeId);
        }

        return compensations;
    }
}

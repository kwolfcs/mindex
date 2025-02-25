package com.mindex.challenge.service;

import com.mindex.challenge.data.Compensation;

import java.util.List;

public interface CompensationService {
    Compensation create(Compensation compensation);
    List<Compensation> readByEmployee(String employeeId);
}

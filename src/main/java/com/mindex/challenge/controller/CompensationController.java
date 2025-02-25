package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.CompensationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CompensationController {
    private static final Logger LOG = LoggerFactory.getLogger(CompensationController.class);

    @Autowired
    private CompensationService compensationService;

    @PostMapping("/compensation")
    public ResponseEntity<Compensation> create(@Valid @RequestBody Compensation compensation) {
        LOG.debug("Received compensation create request for [{}]", compensation);

        Compensation createdCompensation = compensationService.create(compensation);
        return ResponseEntity.ok(createdCompensation);
    }

    @GetMapping("/compensation/e/{id}")
    public ResponseEntity<List<Compensation>> readByEmployee(@PathVariable @NotBlank(message = "Compensation ID must not be blank") String id) {
        LOG.debug("Received compensations get request for employee [{}]", id);

        List<Compensation> compensations = compensationService.readByEmployee(id);
        return ResponseEntity.ok(compensations);
    }
}

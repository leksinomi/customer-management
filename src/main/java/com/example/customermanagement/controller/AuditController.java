package com.example.customermanagement.controller;

import com.example.customermanagement.dto.AuditEntryDTO;
import com.example.customermanagement.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-entries")
public class AuditController {

    private final AuditService auditService;

    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public Page<AuditEntryDTO> getAuditEntries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(defaultValue = "creationDatetime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return auditService.getAuditEntries(page, size, sortBy, sortDir);
    }
}

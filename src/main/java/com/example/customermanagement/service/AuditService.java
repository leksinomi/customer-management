package com.example.customermanagement.service;

import com.example.customermanagement.dto.AuditEntryDTO;
import com.example.customermanagement.mapper.AuditMapper;
import com.example.customermanagement.model.AuditEntry;
import com.example.customermanagement.repository.AuditRepository;
import com.example.customermanagement.utils.SortUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
            "id",
            "action",
            "customerId",
            "status",
            "creationDatetime"
    );

    private final AuditRepository auditRepository;
    private final AuditMapper auditMapper;

    @Autowired
    public AuditService(AuditRepository auditRepository, AuditMapper auditMapper) {
        this.auditRepository = auditRepository;
        this.auditMapper = auditMapper;
    }

    @Transactional
    public void createAuditEntry(String action, Long customerId, String request, String status) {
        AuditEntry auditEntry = AuditEntry.builder()
                .action(action)
                .customerId(customerId)
                .request(request)
                .status(status)
                .creationDatetime(LocalDateTime.now())
                .build();
        auditRepository.save(auditEntry);
    }

    @Transactional(readOnly = true)
    public Page<AuditEntryDTO> getAuditEntries(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = SortUtils.createPageable(page, size, sortBy, sortDir, ALLOWED_SORT_FIELDS);
        Page<AuditEntry> auditEntries = auditRepository.findAll(pageable);
        return auditEntries.map(auditMapper::toDTO);
    }
}

package com.example.customermanagement.mapper;

import com.example.customermanagement.dto.AuditEntryDTO;
import com.example.customermanagement.model.AuditEntry;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    AuditEntryDTO toDTO(AuditEntry auditEntry);
}

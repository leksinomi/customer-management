package com.example.customermanagement.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditEntryDTO {
    private Long id;
    private String action;
    private Long customerId;
    private String request;
    private String status;
    private String creationDatetime;
}

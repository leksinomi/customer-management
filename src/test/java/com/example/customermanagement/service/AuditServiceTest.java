package com.example.customermanagement.service;

import com.example.customermanagement.dto.AuditEntryDTO;
import com.example.customermanagement.exception.InvalidSortParameterException;
import com.example.customermanagement.mapper.AuditMapper;
import com.example.customermanagement.model.AuditEntry;
import com.example.customermanagement.repository.AuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditMapper auditMapper;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditRepository, auditMapper);
    }

    @Nested
    @DisplayName("Tests for createAuditEntry method")
    class CreateAuditEntryTests {

        @Test
        @DisplayName("Given valid parameters_When createAuditEntry_Then audit entry is created and saved successfully")
        void givenValidParameters_WhenCreateAuditEntry_ThenAuditEntryIsCreatedAndSavedSuccessfully() {
            String action = "CREATE";
            Long customerId = 1L;
            String request = "Initial request";
            String status = "SUCCESS";

            ArgumentCaptor<AuditEntry> auditEntryCaptor = ArgumentCaptor.forClass(AuditEntry.class);

            auditService.createAuditEntry(action, customerId, request, status);

            verify(auditRepository).save(auditEntryCaptor.capture());
            AuditEntry savedAuditEntry = auditEntryCaptor.getValue();

            assertNotNull(savedAuditEntry);
            assertEquals(action, savedAuditEntry.getAction());
            assertEquals(customerId, savedAuditEntry.getCustomerId());
            assertEquals(request, savedAuditEntry.getRequest());
            assertEquals(status, savedAuditEntry.getStatus());
            assertNotNull(savedAuditEntry.getCreationDatetime());
        }
    }

    @Nested
    @DisplayName("Tests for getAuditEntries method")
    class GetAuditEntriesTests {

        @Test
        @DisplayName("Given valid pagination and sorting parameters_When getAuditEntries_Then return list of audit entries")
        void givenValidPaginationAndSortingParameters_WhenGetAuditEntries_ThenReturnListOfAuditEntries() {
            int page = 0;
            int size = 10;
            String sortBy = "action";
            String sortDir = "asc";

            Sort sort = Sort.by("action").ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            AuditEntry auditEntry1 = AuditEntry.builder()
                    .id(1L)
                    .action("CREATE")
                    .customerId(1L)
                    .status("SUCCESS")
                    .creationDatetime(LocalDateTime.now())
                    .build();

            AuditEntry auditEntry2 = AuditEntry.builder()
                    .id(2L)
                    .action("UPDATE")
                    .customerId(2L)
                    .status("FAILED")
                    .creationDatetime(LocalDateTime.now())
                    .build();

            List<AuditEntry> auditEntries = Arrays.asList(auditEntry1, auditEntry2);
            Page<AuditEntry> auditEntryPage = new PageImpl<>(auditEntries, pageable, auditEntries.size());

            AuditEntryDTO auditEntryDTO1 = AuditEntryDTO.builder()
                    .id(1L)
                    .action("CREATE")
                    .customerId(1L)
                    .status("SUCCESS")
                    .creationDatetime(String.valueOf(auditEntry1.getCreationDatetime()))
                    .build();

            AuditEntryDTO auditEntryDTO2 = AuditEntryDTO.builder()
                    .id(2L)
                    .action("UPDATE")
                    .customerId(2L)
                    .status("FAILED")
                    .creationDatetime(String.valueOf(auditEntry2.getCreationDatetime()))
                    .build();

            when(auditRepository.findAll(pageable)).thenReturn(auditEntryPage);
            when(auditMapper.toDTO(auditEntry1)).thenReturn(auditEntryDTO1);
            when(auditMapper.toDTO(auditEntry2)).thenReturn(auditEntryDTO2);

            Page<AuditEntryDTO> result = auditService.getAuditEntries(page, size, sortBy, sortDir);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(auditEntryDTO1, result.getContent().get(0));
            assertEquals(auditEntryDTO2, result.getContent().get(1));
            verify(auditRepository).findAll(pageable);
            verify(auditMapper).toDTO(auditEntry1);
            verify(auditMapper).toDTO(auditEntry2);
        }

        @Test
        @DisplayName("Given invalid sortBy parameter_When getAuditEntries_Then throw InvalidSortParameterException")
        void givenInvalidSortByParameter_WhenGetAuditEntries_ThenThrowInvalidSortParameterException() {
            int page = 0;
            int size = 10;
            String sortBy = "invalidField";
            String sortDir = "asc";

            InvalidSortParameterException exception = assertThrows(InvalidSortParameterException.class, () -> {
                auditService.getAuditEntries(page, size, sortBy, sortDir);
            });

            assertTrue(exception.getMessage().contains("Invalid sort parameter"));
            verifyNoInteractions(auditRepository, auditMapper);
        }

        @Test
        @DisplayName("Given invalid sortDir parameter_When getAuditEntries_Then throw InvalidSortParameterException")
        void givenInvalidSortDirParameter_WhenGetAuditEntries_ThenThrowInvalidSortParameterException() {
            int page = 0;
            int size = 10;
            String sortBy = "action";
            String sortDir = "invalidDir";

            InvalidSortParameterException exception = assertThrows(InvalidSortParameterException.class, () -> {
                auditService.getAuditEntries(page, size, sortBy, sortDir);
            });

            assertTrue(exception.getMessage().contains("Invalid sort direction"));
            verifyNoInteractions(auditRepository, auditMapper);
        }

        @Test
        @DisplayName("Given no audit entries in repository_When getAuditEntries_Then return empty page")
        void givenNoAuditEntriesInRepository_WhenGetAuditEntries_ThenReturnEmptyPage() {
            int page = 0;
            int size = 10;
            String sortBy = "action";
            String sortDir = "asc";

            Sort sort = Sort.by("action").ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<AuditEntry> auditEntryPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(auditRepository.findAll(pageable)).thenReturn(auditEntryPage);

            Page<AuditEntryDTO> result = auditService.getAuditEntries(page, size, sortBy, sortDir);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(auditRepository).findAll(pageable);
            verifyNoInteractions(auditMapper);
        }
    }
}
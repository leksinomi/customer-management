package com.example.customermanagement.repository;

import com.example.customermanagement.model.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepository extends JpaRepository<AuditEntry, Long> {
}

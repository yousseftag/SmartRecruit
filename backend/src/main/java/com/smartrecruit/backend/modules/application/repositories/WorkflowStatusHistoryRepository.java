package com.smartrecruit.backend.modules.application.repositories;

import com.smartrecruit.backend.modules.application.entities.WorkflowStatusHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowStatusHistoryRepository
    extends JpaRepository<WorkflowStatusHistory, UUID> {

  List<WorkflowStatusHistory> findByApplicationIdOrderByChangedAtDesc(UUID applicationId);
}

package com.smartrecruit.backend.modules.dashboard.repositories;

import com.smartrecruit.backend.modules.dashboard.entities.WorkflowStatusHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowStatusHistoryRepository
    extends JpaRepository<WorkflowStatusHistory, UUID> {
  @Query("SELECT w FROM WorkflowStatusHistory w ORDER BY w.changedAt DESC")
  List<WorkflowStatusHistory> findLatest(Pageable pageable);
}

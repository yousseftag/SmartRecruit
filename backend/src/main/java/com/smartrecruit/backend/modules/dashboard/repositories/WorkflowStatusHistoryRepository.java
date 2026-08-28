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

  @Query(
      value =
          """
          SELECT
              'STATUS_CHANGE' as type,
              COALESCE(u.first_name || ' ' || u.last_name, 'System') as user_name,
              c.first_name || ' ' || c.last_name as target_name,
              LOWER(w.from_status) as from_status,
              LOWER(w.to_status) as to_status,
              w.changed_at as occurred_at
          FROM workflow_status_history w
          LEFT JOIN app_user u ON w.changed_by = u.id
          LEFT JOIN application a ON w.application_id = a.id
          LEFT JOIN candidate c ON a.candidate_id = c.id

          UNION ALL

          SELECT
              CASE WHEN o.created_at = o.updated_at THEN 'CREATE_OFFER' ELSE 'UPDATE_OFFER' END as type,
              COALESCE(u.first_name || ' ' || u.last_name, 'System') as user_name,
              o.title as target_name,
              NULL as from_status,
              NULL as to_status,
              o.updated_at as occurred_at
          FROM offer o
          LEFT JOIN app_user u ON o.updated_by = u.id

          ORDER BY occurred_at DESC
          LIMIT 10
          """,
      nativeQuery = true)
  List<Object[]> fetchRecentActivities();
}

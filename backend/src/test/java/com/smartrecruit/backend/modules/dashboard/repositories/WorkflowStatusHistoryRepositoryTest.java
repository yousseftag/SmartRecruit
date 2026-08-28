package com.smartrecruit.backend.modules.dashboard.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Use the test profile (in-memory H2 database)
class WorkflowStatusHistoryRepositoryTest {

  @Autowired private WorkflowStatusHistoryRepository repository;

  @Test
  void fetchRecentActivities_ShouldExecuteNativeUnionAll_WithoutSyntaxErrors() {
    // Edge Case: Verify that the native SQL UNION ALL query is syntactically correct
    // and successfully executes against the database.

    assertDoesNotThrow(
        () -> {
          List<Object[]> results = repository.fetchRecentActivities();

          // Flyway injects our dummy data (V4__dashboard_dummy_data.sql) even in the H2 database.
          // The result will not be empty. The most important thing is that there are no SQL syntax
          // errors.
          assertThat(results).isNotNull();
          assertThat(results).isNotEmpty();
        });
  }
}

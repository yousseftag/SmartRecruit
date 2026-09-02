package com.smartrecruit.backend.modules.application.services;

import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.application.entities.Application;
import com.smartrecruit.backend.modules.application.entities.WorkflowStatusHistory;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.application.repositories.ApplicationRepository;
import com.smartrecruit.backend.modules.application.repositories.WorkflowStatusHistoryRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final WorkflowStatusHistoryRepository workflowStatusHistoryRepository;
  private final CvIngestionService cvIngestionService;

  @Transactional
  public void updateApplicationStatus(UUID applicationId, ApplicationStatus newStatus) {
    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found."));

    ApplicationStatus oldStatus = application.getStatus();

    if (oldStatus != newStatus) {
      WorkflowStatusHistory history =
          WorkflowStatusHistory.builder()
              .application(application)
              .fromStatus(oldStatus)
              .toStatus(newStatus)
              .build();
      workflowStatusHistoryRepository.save(history);

      application.setStatus(newStatus);
      applicationRepository.save(application);
    }
  }

  @Transactional
  public void reExtractCv(UUID applicationId) {
    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found."));

    cvIngestionService.resetAndReExtract(application);
  }

  @Transactional
  public void deleteApplication(UUID applicationId) {
    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found."));

    // Note: Due to ON DELETE CASCADE on workflow_status_history,
    // deleting the application will automatically delete its history!
    applicationRepository.delete(application);
  }
}

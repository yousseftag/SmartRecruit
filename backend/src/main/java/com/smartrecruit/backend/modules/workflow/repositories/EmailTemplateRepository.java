package com.smartrecruit.backend.modules.workflow.repositories;

import com.smartrecruit.backend.modules.workflow.entities.EmailTemplate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {

  Optional<EmailTemplate> findFirstByTemplateKey(String templateKey);
}

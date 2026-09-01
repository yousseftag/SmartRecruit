package com.smartrecruit.backend.integration.modules.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApplicationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private OfferRepository offerRepository;

  @Test
  void shouldReturn201WhenValidApplicationSubmitted() throws Exception {
    UUID offerId =
        offerRepository.findAll().stream()
            .findFirst()
            .map(com.smartrecruit.backend.modules.offer.entities.Offer::getId)
            .orElse(UUID.fromString("22222222-2222-2222-2222-222222222222"));

    MockMultipartFile file =
        new MockMultipartFile("file", "resume.pdf", "application/pdf", "dummy content".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/public/applications/apply")
                .file(file)
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe." + UUID.randomUUID() + "@example.com")
                .param("offerId", offerId.toString()))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldReturn400WhenEmailIsMissing() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "resume.pdf", "application/pdf", "dummy content".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/public/applications/apply")
                .file(file)
                .param("firstName", "John")
                .param("lastName", "Doe")
                // Missing email
                .param("offerId", UUID.randomUUID().toString()))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenFileIsMissing() throws Exception {
    mockMvc
        .perform(
            multipart("/api/v1/public/applications/apply")
                // Missing file
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("offerId", UUID.randomUUID().toString()))
        .andExpect(status().isBadRequest());
  }
}

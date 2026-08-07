package com.smartrecruit.backend.integration.modules.candidate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartrecruit.backend.modules.application.services.CandidateApplicationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CandidateControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CandidateApplicationService applicationService;

  @Test
  void shouldReturn201WhenValidApplicationSubmitted() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "resume.pdf", "application/pdf", "dummy content".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/public/candidates/apply")
                .file(file)
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("offerId", UUID.randomUUID().toString()))
        .andExpect(status().isCreated());
  }

  @Test
  void shouldReturn400WhenEmailIsMissing() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("file", "resume.pdf", "application/pdf", "dummy content".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/public/candidates/apply")
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
            multipart("/api/v1/public/candidates/apply")
                // Missing file
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("offerId", UUID.randomUUID().toString()))
        .andExpect(status().isBadRequest());
  }
}

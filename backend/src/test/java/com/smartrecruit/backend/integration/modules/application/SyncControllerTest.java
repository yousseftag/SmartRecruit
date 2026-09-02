package com.smartrecruit.backend.integration.modules.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartrecruit.backend.modules.application.services.NlpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SyncControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private NlpService nlpService;

  @Test
  void shouldReturn200WhenValidPayload() throws Exception {
    String validJson =
        """
        {
          "applicationId": "123e4567-e89b-12d3-a456-426614174000",
          "offerId": "123e4567-e89b-12d3-a456-426614174001",
          "cvId": "123e4567-e89b-12d3-a456-426614174002",
          "extractionStatus": "SUCCESS",
          "extractedData": {
            "description_markdown": "test"
          },
          "extractedMatching": {
            "matched_criteria": {
              "skills": ["java"],
              "experience": true
            }
          },
          "categoryScores": {
            "skills": 20,
            "experience": 10,
            "coursework": 20,
            "languages": 10,
            "localization": 20
          },
          "totalScore": 80
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/internal/cv/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validJson))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturn400WhenTotalScoreIsOver100() throws Exception {
    String invalidJson =
        """
        {
          "applicationId": "123e4567-e89b-12d3-a456-426614174000",
          "offerId": "123e4567-e89b-12d3-a456-426614174001",
          "cvId": "123e4567-e89b-12d3-a456-426614174002",
          "extractedData": {},
          "extractedMatching": {},
          "categoryScores": {
            "skills": 20,
            "experience": 20,
            "coursework": 20,
            "languages": 20,
            "localization": 20
          },
          "totalScore": 150.0
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/internal/cv/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenSumOfCategoryScoresDoesNotMatchTotalScore() throws Exception {
    String invalidJson =
        """
        {
          "applicationId": "123e4567-e89b-12d3-a456-426614174000",
          "offerId": "123e4567-e89b-12d3-a456-426614174001",
          "cvId": "123e4567-e89b-12d3-a456-426614174002",
          "extractedData": {},
          "extractedMatching": {},
          "categoryScores": {
            "skills": 20,
            "experience": 20,
            "coursework": 20,
            "languages": 20,
            "localization": 20
          },
          "totalScore": 80.5
        }
        """;

    mockMvc
        .perform(
            post("/api/v1/internal/cv/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }
}

package com.smartrecruit.backend.integration.modules.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SyncControllerTest {

  private static final String SEEDED_APP_ID = "22000000-0000-0000-0000-000000000002";
  private static final String SEEDED_OFFER_ID = "22222222-2222-2222-2222-222222222222";
  private static final String SEEDED_CV_ID = "21000000-0000-0000-0000-000000000002";

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldReturn200WhenValidPayload() throws Exception {
    String validJson =
        """
        {
          "applicationId": "%s",
          "offerId": "%s",
          "cvId": "%s",
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
        """
            .formatted(SEEDED_APP_ID, SEEDED_OFFER_ID, SEEDED_CV_ID);

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
          "applicationId": "%s",
          "offerId": "%s",
          "cvId": "%s",
          "extractionStatus": "SUCCESS",
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
        """
            .formatted(SEEDED_APP_ID, SEEDED_OFFER_ID, SEEDED_CV_ID);

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
          "applicationId": "%s",
          "offerId": "%s",
          "cvId": "%s",
          "extractionStatus": "SUCCESS",
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
        """
            .formatted(SEEDED_APP_ID, SEEDED_OFFER_ID, SEEDED_CV_ID);

    mockMvc
        .perform(
            post("/api/v1/internal/cv/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }
}

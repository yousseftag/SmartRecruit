package com.smartrecruit.backend.modules.offer.controllers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.backend.config.SecurityConfig;
import com.smartrecruit.backend.exceptions.GlobalExceptionHandler;
import com.smartrecruit.backend.modules.offer.dtos.OfferSyncCallbackDto;
import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import com.smartrecruit.backend.modules.offer.services.OfferService;
import com.smartrecruit.backend.security.JwtAuthConverter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OfferSyncController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OfferSyncControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private OfferService offerService;
  @MockitoBean private JwtAuthConverter jwtAuthConverter;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final UUID offerId = UUID.randomUUID();

  @Test
  void testSyncOfferExtraction_WithoutJwt_ShouldReturn200Ok() throws Exception {
    Map<String, Object> requirements =
        Map.of(
            "missing_from_criteria",
            List.of("Docker", "CI/CD"),
            "insights",
            "Good match with minor gaps");

    OfferSyncCallbackDto callbackDto =
        new OfferSyncCallbackDto(offerId, OfferAiStatus.SUCCESS, requirements);

    mockMvc
        .perform(
            post("/api/v1/internal/offers/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackDto)))
        .andExpect(status().isOk());

    verify(offerService)
        .syncExtractedRequirements(eq(offerId), eq(OfferAiStatus.SUCCESS), eq(requirements));
  }

  @Test
  void testSyncOfferExtraction_WithFailedStatusAndNullRequirements_ShouldReturn200Ok()
      throws Exception {
    OfferSyncCallbackDto callbackDto =
        new OfferSyncCallbackDto(offerId, OfferAiStatus.FAILED, null);

    mockMvc
        .perform(
            post("/api/v1/internal/offers/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callbackDto)))
        .andExpect(status().isOk());

    verify(offerService).syncExtractedRequirements(eq(offerId), eq(OfferAiStatus.FAILED), eq(null));
  }

  @Test
  void testSyncOfferExtraction_WithMissingAiStatus_ShouldReturn400BadRequest() throws Exception {
    // aiStatus is null
    String invalidJson =
        """
        {
          "offerId": "%s"
        }
        """
            .formatted(offerId);

    mockMvc
        .perform(
            post("/api/v1/internal/offers/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }
}

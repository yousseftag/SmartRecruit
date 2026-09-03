package com.smartrecruit.backend.modules.offer.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartrecruit.backend.config.SecurityConfig;
import com.smartrecruit.backend.exceptions.GlobalExceptionHandler;
import com.smartrecruit.backend.exceptions.ResourceNotFoundException;
import com.smartrecruit.backend.modules.offer.dtos.CreateOfferRequest;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.enums.OfferAiStatus;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import com.smartrecruit.backend.modules.offer.services.OfferService;
import com.smartrecruit.backend.security.JwtAuthConverter;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OfferController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OfferControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private OfferService offerService;
  @MockitoBean private OfferRepository offerRepository;
  @MockitoBean private JwtAuthConverter jwtAuthConverter;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private final UUID offerId = UUID.randomUUID();

  // --- GET /offers (HR LIST) ---

  @Test
  void testGetAllOffers_WithViewerRole_ShouldReturn200AndList() throws Exception {
    Offer offer =
        Offer.builder()
            .id(offerId)
            .title("Full Stack Developer")
            .status("DRAFT")
            .offerAiStatus(OfferAiStatus.PENDING)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

    when(offerService.getAllOffers()).thenReturn(List.of(offer));

    mockMvc
        .perform(
            get("/api/v1/offers")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(offerId.toString()))
        .andExpect(jsonPath("$[0].title").value("Full Stack Developer"))
        .andExpect(jsonPath("$[0].status").value("DRAFT"))
        .andExpect(jsonPath("$[0].offerAiStatus").value("PENDING"));
  }

  @Test
  void testGetAllOffers_Unauthenticated_ShouldReturn401Unauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/offers")).andExpect(status().isUnauthorized());
  }

  // --- GET /offers/{id} (HR DETAIL) ---

  @Test
  void testGetInternalOfferById_WhenFound_ShouldReturn200() throws Exception {
    Offer offer =
        Offer.builder()
            .id(offerId)
            .title("Frontend Architect")
            .status("ACTIVE")
            .offerAiStatus(OfferAiStatus.SUCCESS)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

    when(offerService.getOfferById(offerId)).thenReturn(offer);

    mockMvc
        .perform(
            get("/api/v1/offers/{id}", offerId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(offerId.toString()))
        .andExpect(jsonPath("$.title").value("Frontend Architect"))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.offerAiStatus").value("SUCCESS"));
  }

  @Test
  void testGetInternalOfferById_WhenNotFound_ShouldReturn404NotFound() throws Exception {
    when(offerService.getOfferById(offerId))
        .thenThrow(new ResourceNotFoundException("Job offer not found."));

    mockMvc
        .perform(
            get("/api/v1/offers/{id}", offerId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR_ADMIN"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404));
  }

  // --- POST /offers (CREATE) ---

  @Test
  void testCreateOffer_WithRecruiterRole_ShouldReturn201Created() throws Exception {
    CreateOfferRequest request =
        new CreateOfferRequest(
            "Cloud Engineer",
            "Markdown description",
            Map.of("skills", 50, "experience", 50),
            Map.of("skills", List.of("AWS", "Kubernetes")),
            80,
            null,
            "CDI");

    Offer createdOffer =
        Offer.builder()
            .id(offerId)
            .title(request.title())
            .status("DRAFT")
            .offerAiStatus(OfferAiStatus.PENDING)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

    when(offerService.createOffer(any(), any())).thenReturn(createdOffer);

    mockMvc
        .perform(
            post("/api/v1/offers")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(offerId.toString()))
        .andExpect(jsonPath("$.title").value("Cloud Engineer"))
        .andExpect(jsonPath("$.status").value("DRAFT"));
  }

  @Test
  void testCreateOffer_WithViewerRole_ShouldReturn403Forbidden() throws Exception {
    CreateOfferRequest request =
        new CreateOfferRequest(
            "Cloud Engineer",
            "Markdown description",
            Map.of("skills", 50, "experience", 50),
            null,
            80,
            null,
            "CDI");

    mockMvc
        .perform(
            post("/api/v1/offers")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }

  @Test
  void testCreateOffer_WithInvalidWeights_ShouldReturn400BadRequest() throws Exception {
    // Weights sum to 70 != 100
    CreateOfferRequest invalidRequest =
        new CreateOfferRequest(
            "Cloud Engineer",
            "Markdown description",
            Map.of("skills", 50, "experience", 20),
            null,
            80,
            null,
            "CDI");

    mockMvc
        .perform(
            post("/api/v1/offers")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  void testCreateOffer_WithBlankTitle_ShouldReturn400BadRequest() throws Exception {
    CreateOfferRequest invalidRequest =
        new CreateOfferRequest(
            "   ",
            "Markdown description",
            Map.of("skills", 50, "experience", 50),
            null,
            80,
            null,
            "CDI");

    mockMvc
        .perform(
            post("/api/v1/offers")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  // --- PUT /offers/{id} (UPDATE) ---

  @Test
  void testUpdateOffer_WithRecruiterRole_ShouldReturn200Ok() throws Exception {
    CreateOfferRequest request =
        new CreateOfferRequest(
            "Updated Cloud Engineer",
            "Updated markdown",
            Map.of("skills", 60, "experience", 40),
            null,
            85,
            null,
            "CDI");

    Offer updatedOffer =
        Offer.builder()
            .id(offerId)
            .title("Updated Cloud Engineer")
            .status("DRAFT")
            .offerAiStatus(OfferAiStatus.PENDING)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();

    when(offerService.updateOffer(eq(offerId), any(), any())).thenReturn(updatedOffer);

    mockMvc
        .perform(
            put("/api/v1/offers/{id}", offerId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated Cloud Engineer"));
  }

  @Test
  void testUpdateOffer_WithViewerRole_ShouldReturn403Forbidden() throws Exception {
    CreateOfferRequest request =
        new CreateOfferRequest(
            "Updated Cloud Engineer",
            "Updated markdown",
            Map.of("skills", 60, "experience", 40),
            null,
            85,
            null,
            "CDI");

    mockMvc
        .perform(
            put("/api/v1/offers/{id}", offerId)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }
}

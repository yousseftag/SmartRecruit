package com.smartrecruit.backend.modules.reporting.controllers;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.smartrecruit.backend.config.SecurityConfig;
import com.smartrecruit.backend.exceptions.GlobalExceptionHandler;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import com.smartrecruit.backend.modules.reporting.dtos.CampaignStatsDto;
import com.smartrecruit.backend.modules.reporting.dtos.CandidateReportRowDto;
import com.smartrecruit.backend.modules.reporting.dtos.FunnelStageDto;
import com.smartrecruit.backend.modules.reporting.dtos.ReportingDashboardResponseDto;
import com.smartrecruit.backend.modules.reporting.dtos.ScoreDistributionDto;
import com.smartrecruit.backend.modules.reporting.enums.ReportingPeriod;
import com.smartrecruit.backend.modules.reporting.services.ExcelExportService;
import com.smartrecruit.backend.modules.reporting.services.PdfExportService;
import com.smartrecruit.backend.modules.reporting.services.ReportingService;
import com.smartrecruit.backend.security.JwtAuthConverter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportingController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ReportingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ReportingService reportingService;

  @MockitoBean private ExcelExportService excelExportService;

  @MockitoBean private PdfExportService pdfExportService;

  @MockitoBean private OfferRepository offerRepository;

  @MockitoBean private JwtAuthConverter jwtAuthConverter;

  private ReportingDashboardResponseDto mockReport;
  private List<CandidateReportRowDto> mockCandidates;

  @BeforeEach
  void setUp() {
    CampaignStatsDto kpis =
        new CampaignStatsDto(25L, 20L, 80.0, 76.5, 94.0, 10L, 40.0, 3L, 12.0, 5L);
    List<FunnelStageDto> funnel =
        List.of(
            new FunnelStageDto("Reçues", 25L, 100.0),
            new FunnelStageDto("Admissibles IA", 10L, 40.0),
            new FunnelStageDto("Recrutés", 3L, 12.0));
    ScoreDistributionDto dist = new ScoreDistributionDto(4L, 6L, 10L, 5L);

    CandidateReportRowDto candidate =
        new CandidateReportRowDto(
            1,
            UUID.randomUUID(),
            "Youssef Alami",
            "youssef@example.com",
            "+212611223344",
            "Lead Java Developer",
            94.0,
            true,
            ApplicationStatus.INTERVIEWING,
            OffsetDateTime.now(ZoneOffset.UTC),
            Collections.emptyMap());

    mockCandidates = List.of(candidate);
    mockReport = new ReportingDashboardResponseDto(kpis, funnel, dist, mockCandidates);
  }

  @Test
  void getDashboardStats_WithHrAdminRole_ShouldReturn200AndStatsJson() throws Exception {
    when(reportingService.getDashboardReport(null, ReportingPeriod.ALL)).thenReturn(mockReport);

    mockMvc
        .perform(
            get("/api/v1/reporting/stats")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_HR_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.kpis.totalApplications").value(25))
        .andExpect(jsonPath("$.kpis.averageScore").value(76.5))
        .andExpect(jsonPath("$.kpis.hiredCount").value(3))
        .andExpect(jsonPath("$.funnel.length()").value(3))
        .andExpect(jsonPath("$.funnel[0].stage").value("Reçues"))
        .andExpect(jsonPath("$.scoreDistribution.excellentCount").value(4))
        .andExpect(jsonPath("$.topCandidates.length()").value(1))
        .andExpect(jsonPath("$.topCandidates[0].fullName").value("Youssef Alami"));
  }

  @Test
  void getDashboardStats_WithFilters_ShouldForwardOfferIdAndPeriod() throws Exception {
    UUID offerId = UUID.randomUUID();
    when(reportingService.getDashboardReport(eq(offerId), eq(ReportingPeriod.LAST_30_DAYS)))
        .thenReturn(mockReport);

    mockMvc
        .perform(
            get("/api/v1/reporting/stats")
                .param("offerId", offerId.toString())
                .param("period", "30d")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.kpis.totalApplications").value(25));

    verify(reportingService).getDashboardReport(offerId, ReportingPeriod.LAST_30_DAYS);
  }

  @Test
  void getRankedCandidates_WithRecruiterRole_ShouldReturn200AndList() throws Exception {
    when(reportingService.getRankedCandidates(null, ReportingPeriod.ALL, 10))
        .thenReturn(mockCandidates);

    mockMvc
        .perform(
            get("/api/v1/reporting/candidates")
                .param("limit", "10")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].rank").value(1))
        .andExpect(jsonPath("$[0].fullName").value("Youssef Alami"))
        .andExpect(jsonPath("$[0].totalScore").value(94.0));
  }

  @Test
  void exportExcel_WithViewerRole_ShouldReturn200AndExcelAttachment() throws Exception {
    UUID offerId = UUID.randomUUID();
    Offer mockOffer = new Offer();
    mockOffer.setTitle("Senior Cloud Architect");

    byte[] fakeExcel = new byte[] {80, 75, 3, 4}; // Zip / XLSX header bytes
    when(reportingService.getRankedCandidates(eq(offerId), eq(ReportingPeriod.LAST_90_DAYS), eq(0)))
        .thenReturn(mockCandidates);
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(mockOffer));
    when(excelExportService.exportRankedCandidates(
            eq(mockCandidates), eq("Senior Cloud Architect")))
        .thenReturn(fakeExcel);

    mockMvc
        .perform(
            get("/api/v1/reporting/export/excel")
                .param("offerId", offerId.toString())
                .param("period", "90d")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_VIEWER"))))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"reporting-candidats-senior-cloud-architect.xlsx\""))
        .andExpect(
            header()
                .string(
                    HttpHeaders.CONTENT_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .andExpect(content().bytes(fakeExcel));
  }

  @Test
  void exportPdf_WithRecruiterRole_ShouldReturn200AndPdfAttachment() throws Exception {
    UUID offerId = UUID.randomUUID();
    Offer mockOffer = new Offer();
    mockOffer.setTitle("Tech Lead Spring Boot");

    byte[] fakePdf = new byte[] {37, 80, 68, 70}; // %PDF magic bytes
    when(reportingService.getDashboardReport(eq(offerId), eq(ReportingPeriod.THIS_YEAR)))
        .thenReturn(mockReport);
    when(offerRepository.findById(offerId)).thenReturn(Optional.of(mockOffer));
    when(pdfExportService.exportExecutiveReport(
            eq(mockReport), eq("Tech Lead Spring Boot"), eq(ReportingPeriod.THIS_YEAR)))
        .thenReturn(fakePdf);

    mockMvc
        .perform(
            get("/api/v1/reporting/export/pdf")
                .param("offerId", offerId.toString())
                .param("period", "1y")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_RECRUITER"))))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"rapport-synthese-tech-lead-spring-boot.pdf\""))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
        .andExpect(content().bytes(fakePdf));
  }

  @Test
  void reportingEndpoints_WithUnauthorizedRole_ShouldReturn403Forbidden() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/reporting/stats")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_UNAUTHORIZED_GUEST"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void reportingEndpoints_WithoutAuthentication_ShouldReturn401Unauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/reporting/stats")).andExpect(status().isUnauthorized());
  }
}

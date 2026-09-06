package com.smartrecruit.backend.modules.reporting.services;

import static org.junit.jupiter.api.Assertions.*;

import com.lowagie.text.pdf.PdfReader;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.reporting.dtos.CampaignStatsDto;
import com.smartrecruit.backend.modules.reporting.dtos.CandidateReportRowDto;
import com.smartrecruit.backend.modules.reporting.dtos.FunnelStageDto;
import com.smartrecruit.backend.modules.reporting.dtos.ReportingDashboardResponseDto;
import com.smartrecruit.backend.modules.reporting.dtos.ScoreDistributionDto;
import com.smartrecruit.backend.modules.reporting.enums.ReportingPeriod;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PdfExportServiceTest {

  private PdfExportService pdfExportService;

  @BeforeEach
  void setUp() {
    pdfExportService = new PdfExportService();
  }

  @Test
  void exportExecutiveReport_WhenReportProvided_ShouldGenerateValidPdf() throws IOException {
    CampaignStatsDto kpis = new CampaignStatsDto(50, 45, 90.0, 78.4, 95.0, 20, 40.0, 5, 10.0, 10);

    List<FunnelStageDto> funnel =
        List.of(
            new FunnelStageDto("Reçues", 50, 100.0),
            new FunnelStageDto("Admissibles IA", 20, 40.0),
            new FunnelStageDto("Présélectionnés", 12, 24.0),
            new FunnelStageDto("Entretiens", 8, 16.0),
            new FunnelStageDto("Recrutés", 5, 10.0));

    ScoreDistributionDto dist = new ScoreDistributionDto(8, 15, 18, 9);

    CandidateReportRowDto candidate1 =
        new CandidateReportRowDto(
            1,
            UUID.randomUUID(),
            "Karim Benjelloun",
            "k.benjelloun@email.com",
            "+212611223344",
            "Senior Java Developer",
            94.5,
            true,
            ApplicationStatus.INTERVIEWING,
            OffsetDateTime.now(ZoneOffset.UTC),
            Map.of("skills", 95, "experience", 90));

    CandidateReportRowDto candidate2 =
        new CandidateReportRowDto(
            2,
            UUID.randomUUID(),
            "Fatima Zahra",
            "f.zahra@email.com",
            "+212655443322",
            "Senior Java Developer",
            89.0,
            true,
            ApplicationStatus.SHORTLISTED,
            OffsetDateTime.now(ZoneOffset.UTC).minusDays(1),
            Map.of("skills", 88, "experience", 90));

    ReportingDashboardResponseDto report =
        new ReportingDashboardResponseDto(kpis, funnel, dist, List.of(candidate1, candidate2));

    byte[] pdfBytes =
        pdfExportService.exportExecutiveReport(
            report, "Campagne Senior Java", ReportingPeriod.LAST_30_DAYS);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);

    // Verify PDF Magic Bytes (%PDF-)
    assertEquals('%', (char) pdfBytes[0]);
    assertEquals('P', (char) pdfBytes[1]);
    assertEquals('D', (char) pdfBytes[2]);
    assertEquals('F', (char) pdfBytes[3]);
    assertEquals('-', (char) pdfBytes[4]);

    // Inspect via OpenPDF PdfReader
    PdfReader reader = new PdfReader(pdfBytes);
    assertTrue(reader.getNumberOfPages() >= 1);
    assertEquals("Rapport Exécutif - Campagne Senior Java", reader.getInfo().get("Title"));
    assertEquals("SmartRecruit Platform", reader.getInfo().get("Author"));
    reader.close();
  }

  @Test
  void exportExecutiveReport_WhenEmptyData_ShouldGenerateValidPdfWithoutErrors()
      throws IOException {
    CampaignStatsDto emptyKpis = new CampaignStatsDto(0, 0, 0.0, 0.0, 0.0, 0, 0.0, 0, 0.0, 0);
    ReportingDashboardResponseDto emptyReport =
        new ReportingDashboardResponseDto(
            emptyKpis,
            Collections.emptyList(),
            new ScoreDistributionDto(0, 0, 0, 0),
            Collections.emptyList());

    byte[] pdfBytes =
        pdfExportService.exportExecutiveReport(emptyReport, "Campagne Vide", ReportingPeriod.ALL);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);

    // Verify PDF Magic Bytes (%PDF-)
    assertEquals('%', (char) pdfBytes[0]);
    assertEquals('P', (char) pdfBytes[1]);
    assertEquals('D', (char) pdfBytes[2]);
    assertEquals('F', (char) pdfBytes[3]);

    PdfReader reader = new PdfReader(pdfBytes);
    assertTrue(reader.getNumberOfPages() >= 1);
    reader.close();
  }

  @Test
  void exportExecutiveReport_WithNullParameters_ShouldHandleGracefully() throws IOException {
    byte[] pdfBytes = pdfExportService.exportExecutiveReport(null, null, null);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);

    // Verify PDF Magic Bytes
    assertEquals('%', (char) pdfBytes[0]);
    assertEquals('P', (char) pdfBytes[1]);
    assertEquals('D', (char) pdfBytes[2]);
    assertEquals('F', (char) pdfBytes[3]);

    PdfReader reader = new PdfReader(pdfBytes);
    assertTrue(reader.getNumberOfPages() >= 1);
    assertEquals("Rapport Exécutif - Consolidé (Toutes les offres)", reader.getInfo().get("Title"));
    reader.close();
  }

  @Test
  void exportExecutiveReport_WhenTenCandidatesProvided_ShouldRenderFullLeaderboard()
      throws IOException {
    List<CandidateReportRowDto> tenCandidates = new java.util.ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      tenCandidates.add(
          new CandidateReportRowDto(
              i,
              UUID.randomUUID(),
              "Candidat " + i,
              "candidat" + i + "@email.com",
              "+21260000000" + i,
              "Développeur Fullstack",
              95.0 - (i * 3),
              i <= 5,
              i == 1 ? ApplicationStatus.HIRED : ApplicationStatus.INTERVIEWING,
              OffsetDateTime.now(ZoneOffset.UTC).minusDays(i),
              Map.of("skills", 90 - i, "experience", 85 - i)));
    }

    CampaignStatsDto kpis = new CampaignStatsDto(50, 45, 90.0, 78.4, 95.0, 20, 40.0, 5, 10.0, 10);
    List<FunnelStageDto> funnel =
        List.of(
            new FunnelStageDto("Reçues", 50, 100.0),
            new FunnelStageDto("Admissibles IA", 20, 40.0),
            new FunnelStageDto("Présélectionnés", 12, 24.0),
            new FunnelStageDto("Entretiens", 8, 16.0),
            new FunnelStageDto("Recrutés", 5, 10.0));
    ScoreDistributionDto dist = new ScoreDistributionDto(8, 15, 18, 9);

    ReportingDashboardResponseDto report =
        new ReportingDashboardResponseDto(kpis, funnel, dist, tenCandidates);

    byte[] pdfBytes =
        pdfExportService.exportExecutiveReport(
            report, "Campagne Top 10", ReportingPeriod.LAST_30_DAYS);

    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 0);

    PdfReader reader = new PdfReader(pdfBytes);
    assertTrue(reader.getNumberOfPages() >= 1);
    assertEquals("Rapport Exécutif - Campagne Top 10", reader.getInfo().get("Title"));
    reader.close();
  }
}

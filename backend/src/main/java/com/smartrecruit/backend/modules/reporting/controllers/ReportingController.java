package com.smartrecruit.backend.modules.reporting.controllers;

import com.smartrecruit.backend.modules.offer.entities.Offer;
import com.smartrecruit.backend.modules.offer.repositories.OfferRepository;
import com.smartrecruit.backend.modules.reporting.dtos.CandidateReportRowDto;
import com.smartrecruit.backend.modules.reporting.dtos.ReportingDashboardResponseDto;
import com.smartrecruit.backend.modules.reporting.enums.ReportingPeriod;
import com.smartrecruit.backend.modules.reporting.services.ExcelExportService;
import com.smartrecruit.backend.modules.reporting.services.PdfExportService;
import com.smartrecruit.backend.modules.reporting.services.ReportingService;
import java.text.Normalizer;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for recruitment reporting analytics, candidate ranking
 * previews, and binary exports (Excel and PDF).
 */
@RestController
@RequestMapping("/api/v1/reporting")
@PreAuthorize("hasAnyRole('HR_ADMIN', 'RECRUITER', 'VIEWER')")
public class ReportingController {

  private static final MediaType EXCEL_MEDIA_TYPE =
      MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

  private final ReportingService reportingService;
  private final ExcelExportService excelExportService;
  private final PdfExportService pdfExportService;
  private final OfferRepository offerRepository;

  public ReportingController(
      ReportingService reportingService,
      ExcelExportService excelExportService,
      PdfExportService pdfExportService,
      OfferRepository offerRepository) {
    this.reportingService = reportingService;
    this.excelExportService = excelExportService;
    this.pdfExportService = pdfExportService;
    this.offerRepository = offerRepository;
  }

  /**
   * Retrieves campaign analytics including KPIs, recruitment funnel, score distribution, and top
   * ranked candidates for preview.
   *
   * @param offerId Optional offer UUID filter; null indicates consolidated metrics across all
   *     offers.
   * @param period Analytical period window (e.g., "ALL", "30d", "90d", "1y").
   */
  @GetMapping("/stats")
  public ResponseEntity<ReportingDashboardResponseDto> getDashboardStats(
      @RequestParam(required = false) UUID offerId,
      @RequestParam(required = false, defaultValue = "ALL") String period) {
    ReportingPeriod reportingPeriod = ReportingPeriod.fromString(period);
    ReportingDashboardResponseDto report =
        reportingService.getDashboardReport(offerId, reportingPeriod);
    return ResponseEntity.ok(report);
  }

  /**
   * Retrieves the ranked candidate dataset for the specified campaign and period filter.
   *
   * @param offerId Optional offer UUID filter.
   * @param period Analytical period window.
   * @param limit Maximum candidate rows to retrieve (0 for unpaged).
   */
  @GetMapping("/candidates")
  public ResponseEntity<List<CandidateReportRowDto>> getRankedCandidates(
      @RequestParam(required = false) UUID offerId,
      @RequestParam(required = false, defaultValue = "ALL") String period,
      @RequestParam(required = false, defaultValue = "0") int limit) {
    ReportingPeriod reportingPeriod = ReportingPeriod.fromString(period);
    List<CandidateReportRowDto> candidates =
        reportingService.getRankedCandidates(offerId, reportingPeriod, limit);
    return ResponseEntity.ok(candidates);
  }

  /**
   * Streams a formatted Excel (.xlsx) workbook containing the ranked candidate dataset.
   *
   * @param offerId Optional offer UUID filter.
   * @param period Analytical period window.
   * @param limit Maximum candidate rows to include (0 for unlimited export).
   */
  @GetMapping("/export/excel")
  public ResponseEntity<byte[]> exportExcel(
      @RequestParam(required = false) UUID offerId,
      @RequestParam(required = false, defaultValue = "ALL") String period,
      @RequestParam(required = false, defaultValue = "0") int limit,
      @RequestHeader(value = "X-Timezone", required = false) String timezoneHeader) {
    ReportingPeriod reportingPeriod = ReportingPeriod.fromString(period);
    List<CandidateReportRowDto> candidates =
        reportingService.getRankedCandidates(offerId, reportingPeriod, limit);
    String campaignTitle = resolveCampaignTitle(offerId);
    ZoneId zoneId = resolveZoneId(timezoneHeader);
    byte[] excelBytes =
        excelExportService.exportRankedCandidates(candidates, campaignTitle, zoneId);
    String slug = toSlug(campaignTitle);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"reporting-candidats-" + slug + ".xlsx\"")
        .contentType(EXCEL_MEDIA_TYPE)
        .body(excelBytes);
  }

  /**
   * Streams a formatted executive synthesis report in PDF format.
   *
   * @param offerId Optional offer UUID filter.
   * @param period Analytical period window.
   */
  @GetMapping("/export/pdf")
  public ResponseEntity<byte[]> exportPdf(
      @RequestParam(required = false) UUID offerId,
      @RequestParam(required = false, defaultValue = "ALL") String period,
      @RequestHeader(value = "X-Timezone", required = false) String timezoneHeader) {
    ReportingPeriod reportingPeriod = ReportingPeriod.fromString(period);
    ReportingDashboardResponseDto report =
        reportingService.getDashboardReport(offerId, reportingPeriod);
    String campaignTitle = resolveCampaignTitle(offerId);
    ZoneId zoneId = resolveZoneId(timezoneHeader);
    byte[] pdfBytes =
        pdfExportService.exportExecutiveReport(report, campaignTitle, reportingPeriod, zoneId);
    String slug = toSlug(campaignTitle);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"rapport-synthese-" + slug + ".pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdfBytes);
  }

  private ZoneId resolveZoneId(String timezoneHeader) {
    if (timezoneHeader != null && !timezoneHeader.isBlank()) {
      try {
        return ZoneId.of(timezoneHeader.trim());
      } catch (Exception ignored) {
      }
    }
    return ZoneId.systemDefault();
  }

  private String resolveCampaignTitle(UUID offerId) {
    if (offerId == null) {
      return "Consolidé (Toutes les offres)";
    }
    return offerRepository.findById(offerId).map(Offer::getTitle).orElse("Offre Inconnue");
  }

  private String toSlug(String input) {
    if (input == null || input.isBlank()) {
      return "consolide";
    }
    String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
    return normalized
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("^-+|-+$", "");
  }
}

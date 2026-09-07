package com.smartrecruit.backend.modules.reporting.services;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.reporting.dtos.CampaignStatsDto;
import com.smartrecruit.backend.modules.reporting.dtos.CandidateReportRowDto;
import com.smartrecruit.backend.modules.reporting.dtos.FunnelStageDto;
import com.smartrecruit.backend.modules.reporting.dtos.ReportingDashboardResponseDto;
import com.smartrecruit.backend.modules.reporting.dtos.ScoreDistributionDto;
import com.smartrecruit.backend.modules.reporting.enums.ReportingPeriod;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * High-performance PDF export service generating executive summary reports using OpenPDF in-memory
 * streaming.
 */
@Service
public class PdfExportService {

  private static final Logger log = LoggerFactory.getLogger(PdfExportService.class);
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  // Corporate design palette
  private static final Color COLOR_NAVY = new Color(30, 41, 59); // #1E293B
  private static final Color COLOR_SLATE = new Color(100, 116, 139); // #64748B
  private static final Color COLOR_INK = new Color(15, 23, 42); // #0F172A
  private static final Color COLOR_CARD_BG = new Color(248, 250, 252); // #F8FAFC
  private static final Color COLOR_BORDER = new Color(226, 232, 240); // #E2E8F0
  private static final Color COLOR_WHITE = Color.WHITE;

  // Typography
  private static final Font FONT_HEADER_BRAND =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, COLOR_NAVY);
  private static final Font FONT_HEADER_SUBTITLE =
      FontFactory.getFont(FontFactory.HELVETICA, 8.5f, COLOR_SLATE);
  private static final Font FONT_HEADER_META_LABEL =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, COLOR_NAVY);
  private static final Font FONT_HEADER_META_VALUE =
      FontFactory.getFont(FontFactory.HELVETICA, 8f, COLOR_SLATE);

  private static final Font FONT_SECTION_TITLE =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f, COLOR_NAVY);

  private static final Font FONT_CARD_TITLE =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f, COLOR_SLATE);
  private static final Font FONT_CARD_VALUE =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, COLOR_INK);
  private static final Font FONT_CARD_SUBTITLE =
      FontFactory.getFont(FontFactory.HELVETICA, 7.5f, COLOR_SLATE);

  private static final Font FONT_TH =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, COLOR_WHITE);
  private static final Font FONT_TD = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, COLOR_INK);
  private static final Font FONT_TD_BOLD =
      FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, COLOR_INK);
  private static final Font FONT_FOOTER =
      FontFactory.getFont(FontFactory.HELVETICA, 7.5f, COLOR_SLATE);

  /**
   * Generates a corporate executive PDF report stream for a recruitment campaign.
   *
   * @param report Complete dashboard data payload (KPIs, funnel, distribution, top candidates).
   * @param campaignTitle Title of the targeted job offer or campaign name.
   * @param period Analytical period filter.
   * @return Valid PDF document as a byte array.
   */
  public byte[] exportExecutiveReport(
      ReportingDashboardResponseDto report, String campaignTitle, ReportingPeriod period) {

    ReportingDashboardResponseDto safeReport =
        report != null ? report : createEmptyFallbackReport();
    String safeTitle =
        campaignTitle != null && !campaignTitle.isBlank()
            ? campaignTitle
            : "Consolidé (Toutes les offres)";
    ReportingPeriod safePeriod = period != null ? period : ReportingPeriod.ALL;

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      Document document = new Document(PageSize.A4, 36, 36, 36, 36);
      PdfWriter writer = PdfWriter.getInstance(document, outputStream);

      // Register metadata
      document.addTitle("Rapport Exécutif - " + safeTitle);
      document.addSubject("Recruitment Campaign Analytics & Candidate Ranking");
      document.addAuthor("SmartRecruit Platform");
      document.addCreator("SmartRecruit Analytics Engine");

      // Register footer event for page numbering and confidentiality notice
      writer.setPageEvent(new PdfFooterHelper());

      document.open();

      addHeaderBlock(document, safeTitle, safePeriod);
      addKpiGrid(document, safeReport.kpis());
      addFunnelAndDistributionSection(
          document, safeReport.funnel(), safeReport.scoreDistribution());
      addTopCandidatesTable(document, safeReport.topCandidates());

      document.close();
      return outputStream.toByteArray();
    } catch (DocumentException | IOException e) {
      log.error("Failed to generate executive PDF report", e);
      throw new IllegalStateException("Failed to generate PDF report", e);
    }
  }

  private void addHeaderBlock(Document document, String title, ReportingPeriod period)
      throws DocumentException {
    PdfPTable headerTable = new PdfPTable(new float[] {55f, 45f});
    headerTable.setWidthPercentage(100f);
    headerTable.setSpacingAfter(10f);

    // Left Cell: Branding
    PdfPCell brandCell = new PdfPCell();
    brandCell.setBorder(Rectangle.NO_BORDER);
    brandCell.setPadding(0);

    Paragraph brandTitle = new Paragraph("SMARTRECRUIT", FONT_HEADER_BRAND);
    brandTitle.setSpacingAfter(2f);
    brandCell.addElement(brandTitle);

    Paragraph brandSubtitle =
        new Paragraph("Rapport Analytique & Synthèse Exécutive", FONT_HEADER_SUBTITLE);
    brandCell.addElement(brandSubtitle);
    headerTable.addCell(brandCell);

    // Right Cell: Metadata
    PdfPCell metaCell = new PdfPCell();
    metaCell.setBorder(Rectangle.NO_BORDER);
    metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    metaCell.setPadding(0);

    Paragraph pTitle = new Paragraph();
    pTitle.setAlignment(Element.ALIGN_RIGHT);
    pTitle.add(new Phrase("Campagne : ", FONT_HEADER_META_LABEL));
    pTitle.add(new Phrase(title, FONT_HEADER_META_VALUE));
    metaCell.addElement(pTitle);

    Paragraph pPeriod = new Paragraph();
    pPeriod.setAlignment(Element.ALIGN_RIGHT);
    pPeriod.add(new Phrase("Période : ", FONT_HEADER_META_LABEL));
    pPeriod.add(new Phrase(formatPeriodLabel(period), FONT_HEADER_META_VALUE));
    metaCell.addElement(pPeriod);

    Paragraph pDate = new Paragraph();
    pDate.setAlignment(Element.ALIGN_RIGHT);
    pDate.add(new Phrase("Généré le : ", FONT_HEADER_META_LABEL));
    pDate.add(new Phrase(OffsetDateTime.now().format(DATE_TIME_FORMATTER), FONT_HEADER_META_VALUE));
    metaCell.addElement(pDate);

    headerTable.addCell(metaCell);
    document.add(headerTable);

    // Divider Line
    PdfPTable divider = new PdfPTable(1);
    divider.setWidthPercentage(100f);
    divider.setSpacingAfter(12f);
    PdfPCell lineCell = new PdfPCell();
    lineCell.setBorder(Rectangle.BOTTOM);
    lineCell.setBorderColorBottom(COLOR_BORDER);
    lineCell.setBorderWidthBottom(1f);
    lineCell.setFixedHeight(1f);
    divider.addCell(lineCell);
    document.add(divider);
  }

  private void addKpiGrid(Document document, CampaignStatsDto kpis) throws DocumentException {
    PdfPTable kpiTable = new PdfPTable(4);
    kpiTable.setWidthPercentage(100f);
    kpiTable.setSpacingAfter(14f);

    long total = kpis != null ? kpis.totalApplications() : 0;
    long screened = kpis != null ? kpis.screenedApplications() : 0;
    double screenedRate = kpis != null ? kpis.screenedRate() : 0.0;
    double avgScore = kpis != null ? kpis.averageScore() : 0.0;
    double maxScore = kpis != null ? kpis.maxScore() : 0.0;
    long qualified = kpis != null ? kpis.qualifiedCount() : 0;
    double qualRate = kpis != null ? kpis.qualificationRate() : 0.0;
    long hired = kpis != null ? kpis.hiredCount() : 0;
    double convRate = kpis != null ? kpis.conversionRate() : 0.0;
    long rejected = kpis != null ? kpis.rejectedCount() : 0;

    // Card 1: Volume
    kpiTable.addCell(
        createKpiCard(
            "VOLUME CANDIDATURES",
            total + " profils",
            String.format(Locale.US, "%.1f%% évaluées (%d)", screenedRate, screened)));

    // Card 2: Quality
    kpiTable.addCell(
        createKpiCard(
            "QUALITÉ TALENTS",
            String.format(Locale.US, "%.1f %%", avgScore),
            String.format(Locale.US, "Score max: %.1f %%", maxScore)));

    // Card 3: AI Selectivity
    kpiTable.addCell(
        createKpiCard(
            "SÉLECTIVITÉ IA",
            String.format(Locale.US, "%.1f %%", qualRate),
            qualified + " profils retenus"));

    // Card 4: Recruitment Outcomes
    kpiTable.addCell(
        createKpiCard(
            "BILAN RECRUTEMENT",
            hired + " Recrutés",
            String.format(Locale.US, "Taux: %.1f%% (%d rejetés)", convRate, rejected)));

    document.add(kpiTable);
  }

  private PdfPCell createKpiCard(String title, String bigValue, String subtitle) {
    PdfPCell cell = new PdfPCell();
    cell.setBackgroundColor(COLOR_CARD_BG);
    cell.setBorderColor(COLOR_BORDER);
    cell.setBorderWidth(1f);
    cell.setPadding(8f);

    Paragraph pTitle = new Paragraph(title, FONT_CARD_TITLE);
    pTitle.setSpacingAfter(3f);
    cell.addElement(pTitle);

    Paragraph pVal = new Paragraph(bigValue, FONT_CARD_VALUE);
    pVal.setSpacingAfter(2f);
    cell.addElement(pVal);

    Paragraph pSub = new Paragraph(subtitle, FONT_CARD_SUBTITLE);
    cell.addElement(pSub);

    return cell;
  }

  private void addFunnelAndDistributionSection(
      Document document, List<FunnelStageDto> funnel, ScoreDistributionDto dist)
      throws DocumentException {
    PdfPTable sectionTable = new PdfPTable(new float[] {52f, 48f});
    sectionTable.setWidthPercentage(100f);
    sectionTable.setSpacingAfter(14f);

    // Left Cell: Funnel Table
    PdfPCell funnelCell = new PdfPCell();
    funnelCell.setBorder(Rectangle.NO_BORDER);
    funnelCell.setPaddingRight(8f);

    Paragraph funnelTitle = new Paragraph("Entonnoir de Recrutement", FONT_SECTION_TITLE);
    funnelTitle.setSpacingAfter(6f);
    funnelCell.addElement(funnelTitle);

    PdfPTable fTable = new PdfPTable(new float[] {50f, 25f, 25f});
    fTable.setWidthPercentage(100f);
    addTableHeader(fTable, "Étape", "Volume", "Taux");

    if (funnel != null && !funnel.isEmpty()) {
      for (int i = 0; i < funnel.size(); i++) {
        FunnelStageDto stage = funnel.get(i);
        Color rowBg = (i % 2 == 0) ? COLOR_WHITE : COLOR_CARD_BG;
        addTableCell(fTable, stage.stage(), Element.ALIGN_LEFT, rowBg, FONT_TD);
        addTableCell(fTable, String.valueOf(stage.count()), Element.ALIGN_CENTER, rowBg, FONT_TD);
        addTableCell(
            fTable,
            String.format(Locale.US, "%.1f %%", stage.percentage()),
            Element.ALIGN_RIGHT,
            rowBg,
            FONT_TD_BOLD);
      }
    }
    funnelCell.addElement(fTable);
    sectionTable.addCell(funnelCell);

    // Right Cell: Score Distribution Table
    PdfPCell distCell = new PdfPCell();
    distCell.setBorder(Rectangle.NO_BORDER);
    distCell.setPaddingLeft(8f);

    Paragraph distTitle = new Paragraph("Répartition des Scores IA", FONT_SECTION_TITLE);
    distTitle.setSpacingAfter(6f);
    distCell.addElement(distTitle);

    PdfPTable dTable = new PdfPTable(new float[] {65f, 35f});
    dTable.setWidthPercentage(100f);
    addTableHeader(dTable, "Palier de Score", "Candidats");

    long exc = dist != null ? dist.excellentCount() : 0;
    long qua = dist != null ? dist.qualifiedCount() : 0;
    long mod = dist != null ? dist.moderateCount() : 0;
    long ins = dist != null ? dist.insufficientCount() : 0;

    addTableCell(dTable, "Excellents (>= 85%)", Element.ALIGN_LEFT, COLOR_WHITE, FONT_TD);
    addTableCell(dTable, String.valueOf(exc), Element.ALIGN_CENTER, COLOR_WHITE, FONT_TD_BOLD);

    addTableCell(dTable, "Qualifiés (70 - 84%)", Element.ALIGN_LEFT, COLOR_CARD_BG, FONT_TD);
    addTableCell(dTable, String.valueOf(qua), Element.ALIGN_CENTER, COLOR_CARD_BG, FONT_TD_BOLD);

    addTableCell(dTable, "Moyens (50 - 69%)", Element.ALIGN_LEFT, COLOR_WHITE, FONT_TD);
    addTableCell(dTable, String.valueOf(mod), Element.ALIGN_CENTER, COLOR_WHITE, FONT_TD_BOLD);

    addTableCell(dTable, "Insuffisants (< 50%)", Element.ALIGN_LEFT, COLOR_CARD_BG, FONT_TD);
    addTableCell(dTable, String.valueOf(ins), Element.ALIGN_CENTER, COLOR_CARD_BG, FONT_TD_BOLD);

    distCell.addElement(dTable);
    sectionTable.addCell(distCell);

    document.add(sectionTable);
  }

  private void addTopCandidatesTable(Document document, List<CandidateReportRowDto> candidates)
      throws DocumentException {
    Paragraph sectionTitle = new Paragraph("Top 10 Candidats du Classement", FONT_SECTION_TITLE);
    sectionTitle.setSpacingAfter(6f);
    document.add(sectionTitle);

    PdfPTable table = new PdfPTable(new float[] {7f, 25f, 26f, 22f, 10f, 10f});
    table.setWidthPercentage(100f);

    addTableHeader(table, "Rang", "Candidat", "Email", "Offre", "Score", "Statut");

    if (candidates == null || candidates.isEmpty()) {
      PdfPCell emptyCell =
          new PdfPCell(new Phrase("Aucune candidature trouvée pour cette sélection.", FONT_TD));
      emptyCell.setColspan(6);
      emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
      emptyCell.setPadding(10f);
      emptyCell.setBackgroundColor(COLOR_CARD_BG);
      emptyCell.setBorderColor(COLOR_BORDER);
      table.addCell(emptyCell);
    } else {
      for (int i = 0; i < candidates.size(); i++) {
        CandidateReportRowDto c = candidates.get(i);
        Color rowBg = (i % 2 == 0) ? COLOR_WHITE : COLOR_CARD_BG;

        addTableCell(table, "#" + c.rank(), Element.ALIGN_CENTER, rowBg, FONT_TD_BOLD);
        addTableCell(
            table, c.fullName() != null ? c.fullName() : "-", Element.ALIGN_LEFT, rowBg, FONT_TD);
        addTableCell(
            table, c.email() != null ? c.email() : "-", Element.ALIGN_LEFT, rowBg, FONT_TD);
        addTableCell(
            table,
            c.offerTitle() != null ? c.offerTitle() : "-",
            Element.ALIGN_LEFT,
            rowBg,
            FONT_TD);

        String scoreText =
            c.totalScore() != null ? String.format(Locale.US, "%.1f %%", c.totalScore()) : "-";
        addTableCell(table, scoreText, Element.ALIGN_CENTER, rowBg, FONT_TD_BOLD);

        addTableCell(table, formatStatus(c.status()), Element.ALIGN_CENTER, rowBg, FONT_TD);
      }
    }

    document.add(table);
  }

  private void addTableHeader(PdfPTable table, String... headers) {
    for (String header : headers) {
      PdfPCell cell = new PdfPCell(new Phrase(header, FONT_TH));
      cell.setBackgroundColor(COLOR_NAVY);
      cell.setBorderColor(COLOR_NAVY);
      cell.setHorizontalAlignment(Element.ALIGN_CENTER);
      cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
      cell.setPadding(5f);
      table.addCell(cell);
    }
  }

  private void addTableCell(PdfPTable table, String text, int alignment, Color bgColor, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setBackgroundColor(bgColor);
    cell.setBorderColor(COLOR_BORDER);
    cell.setBorderWidth(0.5f);
    cell.setHorizontalAlignment(alignment);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setPadding(4f);
    table.addCell(cell);
  }

  private String formatPeriodLabel(ReportingPeriod period) {
    if (period == null) return "Tout l'historique";
    return switch (period) {
      case ALL -> "Tout l'historique";
      case LAST_30_DAYS -> "30 derniers jours";
      case LAST_90_DAYS -> "90 derniers jours";
      case THIS_YEAR -> "Cette année (2026)";
    };
  }

  private String formatStatus(ApplicationStatus status) {
    if (status == null) return "-";
    return switch (status) {
      case NEW -> "Nouveau";
      case SHORTLISTED -> "Présél.";
      case INTERVIEWING -> "Entretien";
      case FOLLOW_UP -> "Relance";
      case HIRED -> "Recruté";
      case REJECTED -> "Rejeté";
      case ARCHIVED -> "Archivé";
    };
  }

  private ReportingDashboardResponseDto createEmptyFallbackReport() {
    return new ReportingDashboardResponseDto(
        new CampaignStatsDto(0, 0, 0.0, 0.0, 0.0, 0, 0.0, 0, 0.0, 0),
        Collections.emptyList(),
        new ScoreDistributionDto(0, 0, 0, 0),
        Collections.emptyList());
  }

  /** Footer event helper to render page numbers and confidentiality stamp on every page. */
  private static class PdfFooterHelper extends PdfPageEventHelper {

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
      PdfContentByte cb = writer.getDirectContent();

      // Top line of footer
      cb.saveState();
      cb.setColorStroke(COLOR_BORDER);
      cb.setLineWidth(0.5f);
      cb.moveTo(36, 30);
      cb.lineTo(document.getPageSize().getWidth() - 36, 30);
      cb.stroke();
      cb.restoreState();

      // Confidentiality disclaimer
      Phrase leftPhrase =
          new Phrase("Confidentiel - Usage interne SmartRecruit Platform", FONT_FOOTER);
      ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, leftPhrase, 36, 18, 0);

      // Page number
      Phrase rightPhrase = new Phrase("Page " + writer.getPageNumber(), FONT_FOOTER);
      ColumnText.showTextAligned(
          cb, Element.ALIGN_RIGHT, rightPhrase, document.getPageSize().getWidth() - 36, 18, 0);
    }
  }
}

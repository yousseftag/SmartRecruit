package com.smartrecruit.backend.modules.reporting.services;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.reporting.dtos.CandidateReportRowDto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * High-performance Excel export service generating professional, corporate-styled .xlsx
 * spreadsheets for ranked candidate campaign reports.
 */
@Service
public class ExcelExportService {

  private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private static final String[] HEADERS = {
    "Rang",
    "Nom Complet",
    "Email",
    "Téléphone",
    "Intitulé de l'Offre",
    "Date Candidature",
    "Statut",
    "Score Global IA (%)",
    "Admissible",
    "Score Compétences",
    "Score Expérience",
    "Score Formation",
    "Score Langues"
  };

  // Corporate design palette
  private static final byte[] COLOR_NAVY_HEADER = new byte[] {30, 41, 59}; // #1E293B
  private static final byte[] COLOR_ZEBRA_ODD =
      new byte[] {(byte) 248, (byte) 250, (byte) 252}; // #F8FAFC
  private static final byte[] COLOR_BORDER =
      new byte[] {(byte) 203, (byte) 213, (byte) 225}; // #CBD5E1
  private static final byte[] COLOR_GREEN_TEXT = new byte[] {22, (byte) 163, 74}; // #16A34A
  private static final byte[] COLOR_RED_TEXT = new byte[] {(byte) 220, 38, 38}; // #DC2626

  /**
   * Generates a fully-styled Excel workbook containing the ranked candidate dataset.
   *
   * @param candidates Pre-ranked candidate rows from ReportingService.
   * @param campaignTitle Optional campaign or offer title for metadata display.
   * @return Raw byte array of the valid .xlsx file.
   */
  public byte[] exportRankedCandidates(
      List<CandidateReportRowDto> candidates, String campaignTitle) {
    try (XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

      // Set document core properties
      var coreProperties = workbook.getProperties().getCoreProperties();
      coreProperties.setTitle(
          campaignTitle != null && !campaignTitle.isBlank()
              ? campaignTitle
              : "Classement Candidats");
      coreProperties.setCreator("SmartRecruit Platform");

      Sheet sheet = workbook.createSheet("Classement Candidats");
      sheet.setDisplayGridlines(true);
      sheet.createFreezePane(0, 1);

      DefaultIndexedColorMap colorMap = new DefaultIndexedColorMap();
      StyleBundle styles = initializeStyles(workbook, colorMap);

      createHeaderRow(sheet, styles.headerStyle);

      if (candidates == null || candidates.isEmpty()) {
        createEmptyStateRow(sheet, styles.emptyStyle);
      } else {
        populateDataRows(sheet, candidates, styles);
      }

      autoSizeColumns(sheet, HEADERS.length);

      workbook.write(outputStream);
      return outputStream.toByteArray();
    } catch (IOException e) {
      log.error("Failed to generate candidate Excel report", e);
      throw new IllegalStateException("Failed to generate Excel report", e);
    }
  }

  private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
    Row headerRow = sheet.createRow(0);
    headerRow.setHeightInPoints(28);

    for (int col = 0; col < HEADERS.length; col++) {
      Cell cell = headerRow.createCell(col);
      cell.setCellValue(HEADERS[col]);
      cell.setCellStyle(headerStyle);
    }
  }

  private void createEmptyStateRow(Sheet sheet, CellStyle emptyStyle) {
    Row row = sheet.createRow(1);
    row.setHeightInPoints(24);
    Cell cell = row.createCell(0);
    cell.setCellValue("Aucune candidature trouvée pour cette sélection.");
    cell.setCellStyle(emptyStyle);

    // Apply empty style across merged cells for consistent border rendering
    for (int col = 1; col < HEADERS.length; col++) {
      Cell extraCell = row.createCell(col);
      extraCell.setCellStyle(emptyStyle);
    }
    sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, HEADERS.length - 1));
  }

  private void populateDataRows(
      Sheet sheet, List<CandidateReportRowDto> candidates, StyleBundle styles) {
    for (int i = 0; i < candidates.size(); i++) {
      CandidateReportRowDto candidate = candidates.get(i);
      Row row = sheet.createRow(i + 1);
      row.setHeightInPoints(22);

      boolean isOdd = (i % 2) != 0;
      RowStyleGroup rowStyle = isOdd ? styles.oddGroup : styles.evenGroup;

      // 0. Rang (Numeric)
      Cell cellRank = row.createCell(0);
      cellRank.setCellValue(candidate.rank());
      cellRank.setCellStyle(rowStyle.rankStyle);

      // 1. Nom Complet
      Cell cellName = row.createCell(1);
      cellName.setCellValue(candidate.fullName() != null ? candidate.fullName() : "-");
      cellName.setCellStyle(rowStyle.leftStyle);

      // 2. Email
      Cell cellEmail = row.createCell(2);
      cellEmail.setCellValue(candidate.email() != null ? candidate.email() : "-");
      cellEmail.setCellStyle(rowStyle.leftStyle);

      // 3. Téléphone
      Cell cellPhone = row.createCell(3);
      cellPhone.setCellValue(candidate.phone() != null ? candidate.phone() : "-");
      cellPhone.setCellStyle(rowStyle.leftStyle);

      // 4. Intitulé de l'Offre
      Cell cellOffer = row.createCell(4);
      cellOffer.setCellValue(candidate.offerTitle() != null ? candidate.offerTitle() : "-");
      cellOffer.setCellStyle(rowStyle.leftStyle);

      // 5. Date Candidature
      Cell cellDate = row.createCell(5);
      cellDate.setCellValue(
          candidate.appliedAt() != null ? candidate.appliedAt().format(DATE_FORMATTER) : "-");
      cellDate.setCellStyle(rowStyle.centerStyle);

      // 6. Statut
      Cell cellStatus = row.createCell(6);
      cellStatus.setCellValue(formatStatus(candidate.status()));
      cellStatus.setCellStyle(rowStyle.centerStyle);

      // 7. Score Global IA (Stored as numeric Double with decimal format)
      Cell cellScore = row.createCell(7);
      if (candidate.totalScore() != null) {
        cellScore.setCellValue(candidate.totalScore());
        cellScore.setCellStyle(rowStyle.scoreStyle);
      } else {
        cellScore.setCellValue("-");
        cellScore.setCellStyle(rowStyle.centerStyle);
      }

      // 8. Admissible
      Cell cellAdmissible = row.createCell(8);
      cellAdmissible.setCellValue(candidate.isAdmissible() ? "Oui" : "Non");
      cellAdmissible.setCellStyle(
          candidate.isAdmissible() ? rowStyle.admissibleYesStyle : rowStyle.admissibleNoStyle);

      // 9. Score Compétences (Numeric)
      setSubScoreCell(
          row.createCell(9), candidate.categoryScores(), rowStyle, "skills", "technical");

      // 10. Score Expérience (Numeric)
      setSubScoreCell(row.createCell(10), candidate.categoryScores(), rowStyle, "experience");

      // 11. Score Formation (Numeric)
      setSubScoreCell(
          row.createCell(11), candidate.categoryScores(), rowStyle, "coursework", "education");

      // 12. Score Langues (Numeric)
      setSubScoreCell(row.createCell(12), candidate.categoryScores(), rowStyle, "languages");
    }
  }

  private void setSubScoreCell(
      Cell cell, Map<String, Object> categoryScores, RowStyleGroup rowStyle, String... keys) {
    Double score = extractCategoryScoreValue(categoryScores, keys);
    if (score != null) {
      cell.setCellValue(score);
      cell.setCellStyle(rowStyle.subScoreStyle);
    } else {
      cell.setCellValue("-");
      cell.setCellStyle(rowStyle.centerStyle);
    }
  }

  private Double extractCategoryScoreValue(Map<String, Object> categoryScores, String... keys) {
    if (categoryScores == null || categoryScores.isEmpty()) {
      return null;
    }
    for (String key : keys) {
      Object value = categoryScores.get(key);
      if (value instanceof Number number) {
        return number.doubleValue();
      } else if (value != null) {
        try {
          return Double.parseDouble(value.toString());
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return null;
  }

  private String formatStatus(ApplicationStatus status) {
    if (status == null) {
      return "-";
    }
    return switch (status) {
      case NEW -> "Nouveau";
      case SHORTLISTED -> "Présélectionné";
      case INTERVIEWING -> "En Entretien";
      case FOLLOW_UP -> "Relance";
      case HIRED -> "Recruté";
      case REJECTED -> "Rejeté";
      case ARCHIVED -> "Archivé";
    };
  }

  private void autoSizeColumns(Sheet sheet, int columnCount) {
    for (int col = 0; col < columnCount; col++) {
      sheet.autoSizeColumn(col);
      int currentWidth = sheet.getColumnWidth(col);
      sheet.setColumnWidth(col, Math.max(currentWidth + 1024, 3200));
    }
  }

  private StyleBundle initializeStyles(XSSFWorkbook workbook, DefaultIndexedColorMap colorMap) {
    DataFormat dataFormat = workbook.createDataFormat();
    short decimalFormat = dataFormat.getFormat("0.0");

    // 1. Header Font & Style
    XSSFFont headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 11);
    headerFont.setColor(new XSSFColor(new byte[] {(byte) 255, (byte) 255, (byte) 255}, colorMap));

    XSSFCellStyle headerStyle = workbook.createCellStyle();
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(new XSSFColor(COLOR_NAVY_HEADER, colorMap));
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headerStyle.setAlignment(HorizontalAlignment.CENTER);
    headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    applyBorders(headerStyle, new XSSFColor(COLOR_NAVY_HEADER, colorMap));

    // 2. Empty State Style
    XSSFFont italicFont = workbook.createFont();
    italicFont.setItalic(true);
    italicFont.setColor(new XSSFColor(new byte[] {100, 116, (byte) 139}, colorMap)); // #64748B

    XSSFCellStyle emptyStyle = workbook.createCellStyle();
    emptyStyle.setFont(italicFont);
    emptyStyle.setAlignment(HorizontalAlignment.CENTER);
    emptyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    applyBorders(emptyStyle, new XSSFColor(COLOR_BORDER, colorMap));

    // 3. Row Styles Groups (Even and Odd)
    XSSFColor borderColor = new XSSFColor(COLOR_BORDER, colorMap);
    RowStyleGroup evenGroup =
        createRowStyleGroup(workbook, colorMap, null, borderColor, decimalFormat);
    RowStyleGroup oddGroup =
        createRowStyleGroup(
            workbook,
            colorMap,
            new XSSFColor(COLOR_ZEBRA_ODD, colorMap),
            borderColor,
            decimalFormat);

    return new StyleBundle(headerStyle, emptyStyle, evenGroup, oddGroup);
  }

  private RowStyleGroup createRowStyleGroup(
      XSSFWorkbook workbook,
      DefaultIndexedColorMap colorMap,
      XSSFColor bgColor,
      XSSFColor borderColor,
      short decimalFormat) {

    XSSFFont regularFont = workbook.createFont();
    regularFont.setFontHeightInPoints((short) 10);

    XSSFFont boldFont = workbook.createFont();
    boldFont.setBold(true);
    boldFont.setFontHeightInPoints((short) 10);

    XSSFFont greenBoldFont = workbook.createFont();
    greenBoldFont.setBold(true);
    greenBoldFont.setFontHeightInPoints((short) 10);
    greenBoldFont.setColor(new XSSFColor(COLOR_GREEN_TEXT, colorMap));

    XSSFFont redBoldFont = workbook.createFont();
    redBoldFont.setBold(true);
    redBoldFont.setFontHeightInPoints((short) 10);
    redBoldFont.setColor(new XSSFColor(COLOR_RED_TEXT, colorMap));

    XSSFCellStyle left = createBaseCellStyle(workbook, bgColor, borderColor);
    left.setFont(regularFont);
    left.setAlignment(HorizontalAlignment.LEFT);

    XSSFCellStyle center = createBaseCellStyle(workbook, bgColor, borderColor);
    center.setFont(regularFont);
    center.setAlignment(HorizontalAlignment.CENTER);

    XSSFCellStyle rank = createBaseCellStyle(workbook, bgColor, borderColor);
    rank.setFont(boldFont);
    rank.setAlignment(HorizontalAlignment.CENTER);

    XSSFCellStyle score = createBaseCellStyle(workbook, bgColor, borderColor);
    score.setFont(boldFont);
    score.setAlignment(HorizontalAlignment.RIGHT);
    score.setDataFormat(decimalFormat);

    XSSFCellStyle subScore = createBaseCellStyle(workbook, bgColor, borderColor);
    subScore.setFont(regularFont);
    subScore.setAlignment(HorizontalAlignment.CENTER);
    subScore.setDataFormat(decimalFormat);

    XSSFCellStyle admissibleYes = createBaseCellStyle(workbook, bgColor, borderColor);
    admissibleYes.setFont(greenBoldFont);
    admissibleYes.setAlignment(HorizontalAlignment.CENTER);

    XSSFCellStyle admissibleNo = createBaseCellStyle(workbook, bgColor, borderColor);
    admissibleNo.setFont(redBoldFont);
    admissibleNo.setAlignment(HorizontalAlignment.CENTER);

    return new RowStyleGroup(left, center, rank, score, subScore, admissibleYes, admissibleNo);
  }

  private XSSFCellStyle createBaseCellStyle(
      XSSFWorkbook workbook, XSSFColor bgColor, XSSFColor borderColor) {
    XSSFCellStyle style = workbook.createCellStyle();
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    if (bgColor != null) {
      style.setFillForegroundColor(bgColor);
      style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }
    applyBorders(style, borderColor);
    return style;
  }

  private void applyBorders(XSSFCellStyle style, XSSFColor color) {
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setTopBorderColor(color);
    style.setBottomBorderColor(color);
    style.setLeftBorderColor(color);
    style.setRightBorderColor(color);
  }

  private record StyleBundle(
      XSSFCellStyle headerStyle,
      XSSFCellStyle emptyStyle,
      RowStyleGroup evenGroup,
      RowStyleGroup oddGroup) {}

  private record RowStyleGroup(
      XSSFCellStyle leftStyle,
      XSSFCellStyle centerStyle,
      XSSFCellStyle rankStyle,
      XSSFCellStyle scoreStyle,
      XSSFCellStyle subScoreStyle,
      XSSFCellStyle admissibleYesStyle,
      XSSFCellStyle admissibleNoStyle) {}
}

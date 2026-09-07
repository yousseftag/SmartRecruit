package com.smartrecruit.backend.modules.reporting.services;

import static org.junit.jupiter.api.Assertions.*;

import com.smartrecruit.backend.modules.application.enums.ApplicationStatus;
import com.smartrecruit.backend.modules.reporting.dtos.CandidateReportRowDto;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExcelExportServiceTest {

  private ExcelExportService excelExportService;

  @BeforeEach
  void setUp() {
    excelExportService = new ExcelExportService();
  }

  @Test
  void exportRankedCandidates_WhenListEmpty_ShouldReturnValidWorkbookWithHeaders()
      throws IOException {
    byte[] excelBytes =
        excelExportService.exportRankedCandidates(Collections.emptyList(), "Campagne Vide");

    assertNotNull(excelBytes);
    assertTrue(excelBytes.length > 0);

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
      assertEquals("Campagne Vide", workbook.getProperties().getCoreProperties().getTitle());
      assertEquals(1, workbook.getNumberOfSheets());
      Sheet sheet = workbook.getSheetAt(0);
      assertEquals("Classement Candidats", sheet.getSheetName());

      // Header row
      Row headerRow = sheet.getRow(0);
      assertNotNull(headerRow);
      assertEquals("Rang", headerRow.getCell(0).getStringCellValue());
      assertEquals("Nom Complet", headerRow.getCell(1).getStringCellValue());
      assertEquals("Email", headerRow.getCell(2).getStringCellValue());
      assertEquals("Téléphone", headerRow.getCell(3).getStringCellValue());
      assertEquals("Intitulé de l'Offre", headerRow.getCell(4).getStringCellValue());
      assertEquals("Date Candidature", headerRow.getCell(5).getStringCellValue());
      assertEquals("Statut", headerRow.getCell(6).getStringCellValue());
      assertEquals("Score Global IA (%)", headerRow.getCell(7).getStringCellValue());
      assertEquals("Admissible", headerRow.getCell(8).getStringCellValue());
      assertEquals("Score Compétences", headerRow.getCell(9).getStringCellValue());
      assertEquals("Score Expérience", headerRow.getCell(10).getStringCellValue());
      assertEquals("Score Formation", headerRow.getCell(11).getStringCellValue());
      assertEquals("Score Langues", headerRow.getCell(12).getStringCellValue());

      // Empty placeholder row
      Row emptyRow = sheet.getRow(1);
      assertNotNull(emptyRow);
      assertEquals(
          "Aucune candidature trouvée pour cette sélection.",
          emptyRow.getCell(0).getStringCellValue());
    }
  }

  @Test
  void exportRankedCandidates_WhenCandidatesProvided_ShouldPopulateDataAndStyles()
      throws IOException {
    OffsetDateTime now = OffsetDateTime.of(2026, 3, 1, 14, 30, 0, 0, ZoneOffset.UTC);
    CandidateReportRowDto candidate1 =
        new CandidateReportRowDto(
            1,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Karim Benjelloun",
            "k.benjelloun@email.com",
            "+212611223344",
            "Senior Java Developer",
            94.5,
            true,
            ApplicationStatus.INTERVIEWING,
            now,
            Map.of("skills", 95.0, "experience", 90.0, "coursework", 85.0, "languages", 80.0));

    CandidateReportRowDto candidate2 =
        new CandidateReportRowDto(
            2,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Fatima Zahra",
            "f.zahra@email.com",
            "+212655443322",
            "Senior Java Developer",
            48.0,
            false,
            ApplicationStatus.REJECTED,
            now.minusDays(2),
            Map.of("technical", 45, "experience", 40));

    byte[] excelBytes =
        excelExportService.exportRankedCandidates(List.of(candidate1, candidate2), "Campagne Java");

    assertNotNull(excelBytes);
    assertTrue(excelBytes.length > 0);

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
      assertEquals("Campagne Java", workbook.getProperties().getCoreProperties().getTitle());
      assertEquals(
          "SmartRecruit Platform", workbook.getProperties().getCoreProperties().getCreator());

      Sheet sheet = workbook.getSheetAt(0);

      // Verify Row 1 (Candidate 1)
      Row row1 = sheet.getRow(1);
      assertNotNull(row1);
      assertEquals(1, (int) row1.getCell(0).getNumericCellValue());
      assertEquals("Karim Benjelloun", row1.getCell(1).getStringCellValue());
      assertEquals("k.benjelloun@email.com", row1.getCell(2).getStringCellValue());
      assertEquals("+212611223344", row1.getCell(3).getStringCellValue());
      assertEquals("Senior Java Developer", row1.getCell(4).getStringCellValue());
      assertEquals("2026-03-01 14:30", row1.getCell(5).getStringCellValue());
      assertEquals("En Entretien", row1.getCell(6).getStringCellValue());

      // Numeric total score
      assertEquals(94.5, row1.getCell(7).getNumericCellValue(), 0.01);
      assertEquals("Oui", row1.getCell(8).getStringCellValue());

      // Numeric sub-scores
      assertEquals(95.0, row1.getCell(9).getNumericCellValue(), 0.01);
      assertEquals(90.0, row1.getCell(10).getNumericCellValue(), 0.01);
      assertEquals(85.0, row1.getCell(11).getNumericCellValue(), 0.01);
      assertEquals(80.0, row1.getCell(12).getNumericCellValue(), 0.01);

      // Verify Row 2 (Candidate 2)
      Row row2 = sheet.getRow(2);
      assertNotNull(row2);
      assertEquals(2, (int) row2.getCell(0).getNumericCellValue());
      assertEquals("Fatima Zahra", row2.getCell(1).getStringCellValue());
      assertEquals("Rejeté", row2.getCell(6).getStringCellValue());

      // Numeric total score
      assertEquals(48.0, row2.getCell(7).getNumericCellValue(), 0.01);
      assertEquals("Non", row2.getCell(8).getStringCellValue());

      // Numeric sub-scores (including fallback from "technical")
      assertEquals(45.0, row2.getCell(9).getNumericCellValue(), 0.01);
      assertEquals(40.0, row2.getCell(10).getNumericCellValue(), 0.01);
      assertEquals("-", row2.getCell(11).getStringCellValue());
      assertEquals("-", row2.getCell(12).getStringCellValue());
    }
  }

  @Test
  void exportRankedCandidates_WithMissingOptionalFields_ShouldHandleGracefully()
      throws IOException {
    CandidateReportRowDto minimalCandidate =
        new CandidateReportRowDto(
            1,
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            null);

    byte[] excelBytes = excelExportService.exportRankedCandidates(List.of(minimalCandidate), null);

    assertNotNull(excelBytes);
    assertTrue(excelBytes.length > 0);

    try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
      Sheet sheet = workbook.getSheetAt(0);
      Row row = sheet.getRow(1);
      assertNotNull(row);
      assertEquals(1, (int) row.getCell(0).getNumericCellValue());
      assertEquals("-", row.getCell(1).getStringCellValue());
      assertEquals("-", row.getCell(2).getStringCellValue());
      assertEquals("-", row.getCell(3).getStringCellValue());
      assertEquals("-", row.getCell(4).getStringCellValue());
      assertEquals("-", row.getCell(5).getStringCellValue());
      assertEquals("-", row.getCell(6).getStringCellValue());
      assertEquals("-", row.getCell(7).getStringCellValue());
      assertEquals("Non", row.getCell(8).getStringCellValue());
      assertEquals("-", row.getCell(9).getStringCellValue());
      assertEquals("-", row.getCell(10).getStringCellValue());
      assertEquals("-", row.getCell(11).getStringCellValue());
      assertEquals("-", row.getCell(12).getStringCellValue());
    }
  }
}

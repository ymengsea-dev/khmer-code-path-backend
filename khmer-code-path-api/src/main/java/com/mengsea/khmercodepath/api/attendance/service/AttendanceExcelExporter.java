package com.mengsea.khmercodepath.api.attendance.service;

import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRosterPayload;
import com.mengsea.khmercodepath.api.attendance.payload.AttendanceRosterRowPayload;
import com.mengsea.khmercodepath.commons.constant.AttendanceStatus;
import com.mengsea.khmercodepath.commons.domain.AttendanceRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AttendanceExcelExporter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_HEADER_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    public byte[] export(AttendanceRosterPayload roster, List<AttendanceRecord> records) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            writeSummarySheet(workbook, roster, headerStyle);
            writeSessionLogSheet(workbook, records, roster, headerStyle);
            writeMatrixSheet(workbook, records, roster, headerStyle);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to build attendance export", ex);
        }
    }

    private void writeSummarySheet(
            Workbook workbook,
            AttendanceRosterPayload roster,
            CellStyle headerStyle
    ) {
        Sheet sheet = workbook.createSheet("Summary");
        String[] headers = {
                "Student",
                "Student ID",
                "Present",
                "Late",
                "Absent",
                "Total",
                "Rate (%)",
                "Quality",
                "Warning"
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (AttendanceRosterRowPayload row : roster.getRows()) {
            Row dataRow = sheet.createRow(rowIndex++);
            dataRow.createCell(0).setCellValue(row.getStudentName());
            dataRow.createCell(1).setCellValue(row.getStudentCode() != null ? row.getStudentCode() : "");
            dataRow.createCell(2).setCellValue(row.getPresent());
            dataRow.createCell(3).setCellValue(row.getLate());
            dataRow.createCell(4).setCellValue(row.getAbsent());
            dataRow.createCell(5).setCellValue(row.getTotal());
            if (row.getAttendanceRate() != null) {
                dataRow.createCell(6).setCellValue(row.getAttendanceRate());
            } else {
                dataRow.createCell(6).setCellValue("");
            }
            dataRow.createCell(7).setCellValue(row.getQualityLabel());
            dataRow.createCell(8).setCellValue(row.isWarned() ? "Warned" : "");
        }

        if (roster.getClassAverageRate() != null) {
            Row avgRow = sheet.createRow(rowIndex + 1);
            avgRow.createCell(0).setCellValue("Class average");
            avgRow.createCell(6).setCellValue(roster.getClassAverageRate());
        }

        autosizeColumns(sheet, headers.length);
    }

    private void writeSessionLogSheet(
            Workbook workbook,
            List<AttendanceRecord> records,
            AttendanceRosterPayload roster,
            CellStyle headerStyle
    ) {
        Sheet sheet = workbook.createSheet("Session log");
        String[] headers = {"Student", "Student ID", "Session date", "Status"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        Set<String> studentIds = roster.getRows().stream()
                .map(AttendanceRosterRowPayload::getStudentId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<AttendanceRecord> filtered = records.stream()
                .filter(record -> studentIds.contains(record.getStudent().getUuid()))
                .sorted(Comparator
                        .comparing(AttendanceRecord::getSessionDate)
                        .thenComparing(record -> record.getStudent().getUsername(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        int rowIndex = 1;
        for (AttendanceRecord record : filtered) {
            Row dataRow = sheet.createRow(rowIndex++);
            dataRow.createCell(0).setCellValue(record.getStudent().getUsername());
            dataRow.createCell(1).setCellValue(
                    record.getStudent().getStudentId() != null ? record.getStudent().getStudentId() : ""
            );
            dataRow.createCell(2).setCellValue(record.getSessionDate().format(DATE_FMT));
            dataRow.createCell(3).setCellValue(formatStatus(record.getStatus()));
        }

        autosizeColumns(sheet, headers.length);
    }

    private void writeMatrixSheet(
            Workbook workbook,
            List<AttendanceRecord> records,
            AttendanceRosterPayload roster,
            CellStyle headerStyle
    ) {
        Sheet sheet = workbook.createSheet("By date");
        Set<String> studentIds = roster.getRows().stream()
                .map(AttendanceRosterRowPayload::getStudentId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<LocalDate> dates = records.stream()
                .filter(record -> studentIds.contains(record.getStudent().getUuid()))
                .map(AttendanceRecord::getSessionDate)
                .distinct()
                .sorted()
                .toList();

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Student");
        headerRow.getCell(0).setCellStyle(headerStyle);
        headerRow.createCell(1).setCellValue("Student ID");
        headerRow.getCell(1).setCellStyle(headerStyle);
        for (int i = 0; i < dates.size(); i++) {
            Cell cell = headerRow.createCell(i + 2);
            cell.setCellValue(dates.get(i).format(DATE_HEADER_FMT));
            cell.setCellStyle(headerStyle);
        }

        Map<String, Map<LocalDate, String>> matrix = new LinkedHashMap<>();
        for (AttendanceRosterRowPayload row : roster.getRows()) {
            matrix.put(row.getStudentId(), new LinkedHashMap<>());
        }
        for (AttendanceRecord record : records) {
            String studentId = record.getStudent().getUuid();
            if (!matrix.containsKey(studentId)) {
                continue;
            }
            matrix.get(studentId).put(record.getSessionDate(), statusCode(record.getStatus()));
        }

        int rowIndex = 1;
        for (AttendanceRosterRowPayload row : roster.getRows()) {
            Row dataRow = sheet.createRow(rowIndex++);
            dataRow.createCell(0).setCellValue(row.getStudentName());
            dataRow.createCell(1).setCellValue(row.getStudentCode() != null ? row.getStudentCode() : "");
            Map<LocalDate, String> statuses = matrix.getOrDefault(row.getStudentId(), Map.of());
            for (int i = 0; i < dates.size(); i++) {
                String code = statuses.get(dates.get(i));
                if (code != null) {
                    dataRow.createCell(i + 2).setCellValue(code);
                }
            }
        }

        autosizeColumns(sheet, Math.max(2, dates.size() + 2));
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void autosizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String formatStatus(String status) {
        try {
            return switch (AttendanceStatus.valueOf(status)) {
                case PRESENT -> "Present";
                case LATE -> "Late";
                case ABSENT -> "Absent";
            };
        } catch (IllegalArgumentException ex) {
            return status;
        }
    }

    private String statusCode(String status) {
        try {
            return switch (AttendanceStatus.valueOf(status)) {
                case PRESENT -> "P";
                case LATE -> "L";
                case ABSENT -> "A";
            };
        } catch (IllegalArgumentException ex) {
            return status;
        }
    }
}

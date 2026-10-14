package mz.co.amaterasu.gestao.document;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelExportStrategy implements ExportStrategy {

    @Override
    public byte[] export(ReportData data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(sheetName(data.getTitle()));
            int rowIndex = 0;

            rowIndex = writeHeader(sheet, workbook, data, rowIndex);
            rowIndex = writeColumnHeaders(sheet, workbook, data, rowIndex);
            rowIndex = writeRows(sheet, data, rowIndex);
            writeTotals(sheet, workbook, data, rowIndex);

            for (int i = 0; i < data.getHeaders().size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private int writeHeader(Sheet sheet, Workbook workbook, ReportData data, int rowIndex) {
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        Cell titleCell = sheet.createRow(rowIndex++).createCell(0);
        titleCell.setCellValue(data.getTitle());
        titleCell.setCellStyle(titleStyle);

        if (data.getSubtitle() != null && !data.getSubtitle().isBlank()) {
            sheet.createRow(rowIndex++).createCell(0).setCellValue(data.getSubtitle());
        }

        for (Map.Entry<String, String> entry : data.getMetadata().entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }

        return rowIndex + 1;
    }

    private int writeColumnHeaders(Sheet sheet, Workbook workbook, ReportData data, int rowIndex) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BROWN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);

        Row row = sheet.createRow(rowIndex++);
        for (int i = 0; i < data.getHeaders().size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(data.getHeaders().get(i));
            cell.setCellStyle(style);
        }
        return rowIndex;
    }

    private int writeRows(Sheet sheet, ReportData data, int rowIndex) {
        for (List<String> values : data.getRows()) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < values.size(); i++) {
                row.createCell(i).setCellValue(values.get(i) == null ? "" : values.get(i));
            }
        }
        return rowIndex;
    }

    private void writeTotals(Sheet sheet, Workbook workbook, ReportData data, int rowIndex) {
        if (data.getTotals().isEmpty()) {
            return;
        }
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);

        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < data.getTotals().size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(data.getTotals().get(i) == null ? "" : data.getTotals().get(i));
            cell.setCellStyle(style);
        }
    }

    private String sheetName(String title) {
        if (title == null || title.isBlank()) {
            return "Relatorio";
        }
        String clean = title.replaceAll("[\\\\/*?:\\[\\]]", " ").trim();
        return clean.length() > 31 ? clean.substring(0, 31) : clean;
    }

    @Override
    public String contentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String fileExtension() {
        return "xlsx";
    }
}

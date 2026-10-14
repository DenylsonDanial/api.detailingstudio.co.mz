package mz.co.amaterasu.gestao.document;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PdfExportStrategy implements ExportStrategy {

    /** Cor primaria da marca Detailing Studio (ajustar quando houver manual de marca). */
    private static final BaseColor BRAND = new BaseColor(0x16, 0x1C, 0x24);
    private static final BaseColor BRAND_LIGHT = new BaseColor(0xE6, 0xEC, 0xF2);

    @Override
    public byte[] export(ReportData data) throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 28, 28, 32, 32);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            writeHeader(document, data);
            writeTable(document, data);
            document.close();
        } catch (DocumentException e) {
            throw new IOException("Falha ao gerar o PDF: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    private void writeHeader(Document document, ReportData data) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BRAND);
        Paragraph title = new Paragraph(data.getTitle(), titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        document.add(title);

        if (data.getSubtitle() != null && !data.getSubtitle().isBlank()) {
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
            document.add(new Paragraph(data.getSubtitle(), subtitleFont));
        }

        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        for (Map.Entry<String, String> entry : data.getMetadata().entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue(), metaFont));
        }

        document.add(new Paragraph(" "));
    }

    private void writeTable(Document document, ReportData data) throws DocumentException {
        if (data.getHeaders().isEmpty()) {
            return;
        }

        PdfPTable table = new PdfPTable(data.getHeaders().size());
        table.setWidthPercentage(100);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        for (String header : data.getHeaders()) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BRAND);
            cell.setPadding(6);
            table.addCell(cell);
        }

        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
        for (List<String> row : data.getRows()) {
            for (String value : row) {
                PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, bodyFont));
                cell.setPadding(5);
                table.addCell(cell);
            }
        }

        if (!data.getTotals().isEmpty()) {
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BRAND);
            for (String value : data.getTotals()) {
                PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, totalFont));
                cell.setBackgroundColor(BRAND_LIGHT);
                cell.setPadding(6);
                table.addCell(cell);
            }
        }

        document.add(table);
    }

    @Override
    public String contentType() {
        return "application/pdf";
    }

    @Override
    public String fileExtension() {
        return "pdf";
    }
}

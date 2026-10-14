package mz.co.amaterasu.gestao.document;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * PDF comercial em A4 retrato com cabeçalho da empresa, cliente, linhas e totais.
 */
public class CommercialPdfRenderer {

    private static final BaseColor BRAND = new BaseColor(0x3B, 0x20, 0x20);
    private static final BaseColor BRAND_LIGHT = new BaseColor(0xFF, 0xE4, 0xDB);
    private static final BaseColor MUTED = new BaseColor(0x66, 0x66, 0x66);

    public byte[] render(CommercialDocumentData data) throws IOException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            writeIssuer(document, data);
            writeTitle(document, data);
            writeParties(document, data);
            writeTable(document, data);
            writeTotals(document, data);
            writeBlock(document, "Termos e condições", data.getTerms());
            writeBlock(document, "Notas", data.getNotes());
            document.close();
        } catch (DocumentException e) {
            throw new IOException("Falha ao gerar o PDF: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    private void writeIssuer(Document document, CommercialDocumentData data) throws DocumentException {
        PdfPTable header = new PdfPTable(new float[]{1.2f, 3f});
        header.setWidthPercentage(100);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Image logo = decodeLogo(data.getLogoBase64());
        if (logo != null) {
            logo.scaleToFit(90, 50);
            logoCell.addElement(logo);
        }
        header.addCell(logoCell);

        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BRAND);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED);
        PdfPCell info = new PdfPCell();
        info.setBorder(Rectangle.NO_BORDER);
        info.addElement(new Paragraph(nullToEmpty(data.getCompanyName()), nameFont));
        if (notBlank(data.getCompanyNuit())) {
            info.addElement(new Paragraph("NUIT: " + data.getCompanyNuit(), metaFont));
        }
        if (notBlank(data.getCompanyAddress())) {
            info.addElement(new Paragraph(data.getCompanyAddress(), metaFont));
        }
        String contact = join("  ·  ", data.getCompanyPhone(), data.getCompanyEmail());
        if (notBlank(contact)) {
            info.addElement(new Paragraph(contact, metaFont));
        }
        header.addCell(info);
        document.add(header);
        document.add(new Paragraph(" "));
    }

    private void writeTitle(Document document, CommercialDocumentData data) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BRAND);
        Paragraph title = new Paragraph(nullToEmpty(data.getDocumentTitle()), titleFont);
        title.setSpacingAfter(4);
        document.add(title);

        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED);
        document.add(new Paragraph("Número: " + nullToEmpty(data.getDocumentNumber()), metaFont));
        if (notBlank(data.getIssueDate())) {
            document.add(new Paragraph("Emissão: " + data.getIssueDate(), metaFont));
        }
        if (notBlank(data.getValidUntil())) {
            document.add(new Paragraph("Validade: " + data.getValidUntil(), metaFont));
        }
        if (notBlank(data.getCurrencyLabel())) {
            document.add(new Paragraph("Moeda: " + data.getCurrencyLabel(), metaFont));
        }
        document.add(new Paragraph(" "));
    }

    private void writeParties(Document document, CommercialDocumentData data) throws DocumentException {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BRAND);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        document.add(new Paragraph("Cliente", labelFont));
        document.add(new Paragraph(nullToEmpty(data.getClientName()), bodyFont));
        if (notBlank(data.getClientNuit())) {
            document.add(new Paragraph("NUIT: " + data.getClientNuit(), bodyFont));
        }
        if (notBlank(data.getClientAddress())) {
            document.add(new Paragraph(data.getClientAddress(), bodyFont));
        }
        document.add(new Paragraph(" "));
    }

    private void writeTable(Document document, CommercialDocumentData data) throws DocumentException {
        if (data.getHeaders() == null || data.getHeaders().isEmpty()) {
            return;
        }
        PdfPTable table = new PdfPTable(data.getHeaders().size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(8);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.WHITE);
        for (String header : data.getHeaders()) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(BRAND);
            cell.setPadding(6);
            table.addCell(cell);
        }

        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
        if (data.getRows() != null) {
            for (List<String> row : data.getRows()) {
                for (String value : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(value == null ? "" : value, bodyFont));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }
        }
        document.add(table);
    }

    private void writeTotals(Document document, CommercialDocumentData data) throws DocumentException {
        if (data.getTotals() == null || data.getTotals().isEmpty()) {
            return;
        }
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BRAND);
        for (String line : data.getTotals()) {
            Paragraph p = new Paragraph(line, totalFont);
            p.setAlignment(Element.ALIGN_RIGHT);
            document.add(p);
        }
        document.add(new Paragraph(" "));
    }

    private void writeBlock(Document document, String title, String body) throws DocumentException {
        if (!notBlank(body)) {
            return;
        }
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BRAND);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        document.add(new Paragraph(title, labelFont));
        document.add(new Paragraph(body, bodyFont));
        document.add(new Paragraph(" "));
    }

    private Image decodeLogo(String raw) {
        if (!notBlank(raw)) {
            return null;
        }
        try {
            String payload = raw;
            int comma = raw.indexOf("base64,");
            if (comma >= 0) {
                payload = raw.substring(comma + "base64,".length());
            }
            byte[] bytes = Base64.getDecoder().decode(payload);
            return Image.getInstance(bytes);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String join(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (notBlank(part)) {
                if (sb.length() > 0) {
                    sb.append(sep);
                }
                sb.append(part);
            }
        }
        return sb.toString();
    }
}

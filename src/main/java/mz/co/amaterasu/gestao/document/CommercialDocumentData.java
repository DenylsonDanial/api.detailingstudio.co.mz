package mz.co.amaterasu.gestao.document;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Documento comercial em retrato (cotação, contrato). Distinto do ReportData tabular.
 */
@Data
@Builder
public class CommercialDocumentData {

    private String documentTitle;
    private String documentNumber;
    private String issueDate;
    private String validUntil;

    private String companyName;
    private String companyNuit;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    /** Base64 puro ou data URL; pode ser nulo. */
    private String logoBase64;

    private String clientName;
    private String clientNuit;
    private String clientAddress;

    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();

    @Builder.Default
    private List<String> headers = new ArrayList<>();
    @Builder.Default
    private List<List<String>> rows = new ArrayList<>();
    @Builder.Default
    private List<String> totals = new ArrayList<>();

    private String terms;
    private String notes;
    private String currencyLabel;
}

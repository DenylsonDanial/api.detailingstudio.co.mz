package mz.co.amaterasu.gestao.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelo tabular neutro que alimenta qualquer exportacao (PDF ou Excel).
 * Os modulos de negocio constroem um ReportData e delegam a formatacao a uma ExportStrategy.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportData {

    private String title;
    private String subtitle;

    /** Pares chave/valor apresentados no cabecalho (periodo, cliente, responsavel, ...). */
    private Map<String, String> metadata = new LinkedHashMap<>();

    private List<String> headers = new ArrayList<>();
    private List<List<String>> rows = new ArrayList<>();

    /** Linha de totais opcional, alinhada com headers. */
    private List<String> totals = new ArrayList<>();

    public ReportData(String title, List<String> headers) {
        this.title = title;
        this.headers = headers;
    }

    public ReportData addMeta(String label, String value) {
        this.metadata.put(label, value);
        return this;
    }

    public ReportData addRow(List<String> row) {
        this.rows.add(row);
        return this;
    }
}

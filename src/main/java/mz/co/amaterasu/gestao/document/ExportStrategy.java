package mz.co.amaterasu.gestao.document;

import java.io.IOException;

public interface ExportStrategy {

    byte[] export(ReportData data) throws IOException;

    String contentType();

    String fileExtension();
}

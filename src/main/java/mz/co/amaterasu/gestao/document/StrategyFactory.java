package mz.co.amaterasu.gestao.document;

public class StrategyFactory {

    public static ExportStrategy getStrategy(String strategyName) {
        if (strategyName.equalsIgnoreCase("pdf")) {
            return new PdfExportStrategy();
        }
        return new ExcelExportStrategy();
    }
}

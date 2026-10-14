package mz.co.amaterasu.gestao.entry.enums;

/** Estado do trabalho, independente do estado de pagamento (especificacao, seccao 7). */
public enum WorkStatus {
    PENDING("Por iniciar"),
    IN_PROGRESS("Em curso"),
    COMPLETED("Concluído"),
    DELIVERED("Entregue"),
    CANCELLED("Cancelada");

    private final String label;

    WorkStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

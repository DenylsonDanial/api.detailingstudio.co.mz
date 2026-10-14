package mz.co.amaterasu.gestao.entry.enums;

/** Calculado pela vista vw_entries a partir das afectacoes; nunca editavel. */
public enum EntryPaymentStatus {
    UNPAID("Por pagar"),
    PARTIAL("Parcialmente pago"),
    PAID("Pago"),
    CANCELLED("Cancelada");

    private final String label;

    EntryPaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

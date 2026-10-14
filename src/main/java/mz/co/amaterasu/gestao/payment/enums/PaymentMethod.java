package mz.co.amaterasu.gestao.payment.enums;

/** Metodos de pagamento iniciais (especificacao, seccao 7). Usado tambem nas despesas. */
public enum PaymentMethod {
    CASH("Numerário"),
    MPESA("M-Pesa"),
    EMOLA("e-Mola"),
    TRANSFER("Transferência"),
    POS("POS");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

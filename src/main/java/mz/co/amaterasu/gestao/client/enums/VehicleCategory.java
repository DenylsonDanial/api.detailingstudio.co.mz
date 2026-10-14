package mz.co.amaterasu.gestao.client.enums;

/** Categoria de preco da viatura (especificacao, seccao 5). */
public enum VehicleCategory {
    TURISMO("Turismo"),
    SUV("SUV/Carrinha"),
    BUS("Bus"),
    MOTA("Mota");

    private final String label;

    VehicleCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

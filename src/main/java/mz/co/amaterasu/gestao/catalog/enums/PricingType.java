package mz.co.amaterasu.gestao.catalog.enums;

/** Comportamento de preco de um servico (especificacao, seccao 6). */
public enum PricingType {
    /** Preco por categoria de viatura (Turismo, SUV/Carrinha, Bus, Mota). */
    BY_CATEGORY,
    /** Preco unico. */
    FIXED,
    /** Preco introduzido em cada trabalho (ex.: PPF, fumados). */
    VARIABLE,
    /** Tarifa por hora; a quantidade sao horas e aceita decimais. */
    HOURLY
}

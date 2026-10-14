package mz.co.amaterasu.gestao.base.service;

public interface DocumentSequenceService {

    String TYPE_ENTRY = "ENTRY";
    String TYPE_RECEIPT = "RECEIPT";
    String TYPE_REVERSAL = "REVERSAL";
    String TYPE_EXPENSE = "EXPENSE";

    /**
     * Atribui o próximo número no formato PREFIXO-AAAA-NNNN, com bloqueio de linha.
     * Tem de ser chamado dentro da transacção que grava o documento.
     */
    String nextNumber(String documentType);
}

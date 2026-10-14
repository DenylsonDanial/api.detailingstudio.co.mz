-- =============================================================================
-- Base de dados de TENANT (uma por empresa cliente) - Detailing Studio.
-- Este V1 contem apenas o nucleo partilhado (definicoes, moedas, numeracao).
-- O dominio (clientes, viaturas, entradas, pagamentos, despesas) entra nas
-- migracoes seguintes. Contem exclusivamente dados de negocio. A identidade (utilizadores, perfis,
-- permissoes) vive na base de dados master.
-- Convencoes:
--   * dinheiro  -> numeric(18,2)
--   * estados   -> varchar com CHECK, para serem legiveis em SQL directo
--   * auditoria -> created_by / created_at / updated_by / updated_at em todas
--                  as tabelas de negocio, complementados pelo audit_log (V2)
-- =============================================================================

-- --- Definicoes da empresa ---------------------------------------------------

CREATE TABLE tb_currencies (
    id         bigserial    PRIMARY KEY,
    name       varchar(100) NOT NULL,
    short_name varchar(10)  NOT NULL,
    symbol     varchar(10),
    created_by varchar(255),
    updated_by varchar(255),
    created_at date,
    updated_at date
);

CREATE TABLE tb_business_settings (
    id             bigserial    PRIMARY KEY,
    business_name  varchar(255) NOT NULL,
    nuit           int8,
    email          varchar(255),
    address        varchar(255),
    logo           text,
    phone          varchar(32),
    alt_phone      varchar(32),
    province       varchar(100),
    city           varchar(100),
    db_tenant_name varchar(63)  NOT NULL,
    currency_id    bigint       REFERENCES tb_currencies (id),
    is_active      boolean      NOT NULL DEFAULT true,
    activity_start date,
    created_by     varchar(255),
    updated_by     varchar(255),
    created_at     date,
    updated_at     date
);

-- Numeracao automatica de documentos: ENT-2026-0001, REC-2026-0001, ...
CREATE TABLE tb_document_sequences (
    id            bigserial   PRIMARY KEY,
    document_type varchar(30) NOT NULL,
    prefix        varchar(10) NOT NULL,
    year          int4        NOT NULL,
    last_number   int4        NOT NULL DEFAULT 0,
    padding       int4        NOT NULL DEFAULT 4,
    CONSTRAINT uk_doc_seq UNIQUE (document_type, year)
);

INSERT INTO tb_document_sequences (document_type, prefix, year) VALUES
    ('ENTRY',    'ENT', EXTRACT(YEAR FROM now())),
    ('RECEIPT',  'REC', EXTRACT(YEAR FROM now())),
    ('REVERSAL', 'EST', EXTRACT(YEAR FROM now())),
    ('EXPENSE',  'DES', EXTRACT(YEAR FROM now()));

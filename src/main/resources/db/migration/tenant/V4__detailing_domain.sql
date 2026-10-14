-- =============================================================================
-- Dominio do Detailing Studio (especificacao, seccoes 5 a 9):
-- clientes, viaturas, catalogo de servicos e precos, entradas, pagamentos com
-- afectacoes, despesas e historico de alteracoes com motivo.
--
-- Nenhum saldo e guardado: o valor pago de uma entrada e a soma das suas
-- afectacoes; o estado de pagamento e o saldo vem da vista vw_entries.
-- Estornos sao movimentos de sinal negativo ligados ao original.
-- =============================================================================

-- --- Clientes e viaturas ----------------------------------------------------

CREATE TABLE tb_clients (
    id             bigserial    PRIMARY KEY,
    name           varchar(255) NOT NULL,
    client_type    varchar(20)  NOT NULL DEFAULT 'INDIVIDUAL'
                   CHECK (client_type IN ('INDIVIDUAL', 'COMPANY')),
    phone          varchar(32),
    credit_allowed boolean      NOT NULL DEFAULT false,
    notes          text,
    status         varchar(20)  NOT NULL DEFAULT 'ACTIVE'
                   CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_by     varchar(255) NOT NULL,
    created_at     timestamp    NOT NULL,
    updated_by     varchar(255) NOT NULL,
    updated_at     timestamp    NOT NULL
);

CREATE INDEX idx_clients_name  ON tb_clients (lower(name));
CREATE INDEX idx_clients_phone ON tb_clients (phone);

CREATE TABLE tb_vehicles (
    id               bigserial    PRIMARY KEY,
    client_id        bigint       NOT NULL REFERENCES tb_clients (id),
    plate            varchar(32)  NOT NULL,
    plate_normalized varchar(32)  NOT NULL,
    brand            varchar(100),
    model            varchar(100),
    category         varchar(20)  NOT NULL
                     CHECK (category IN ('TURISMO', 'SUV', 'BUS', 'MOTA')),
    notes            text,
    status           varchar(20)  NOT NULL DEFAULT 'ACTIVE'
                     CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_by       varchar(255) NOT NULL,
    created_at       timestamp    NOT NULL,
    updated_by       varchar(255) NOT NULL,
    updated_at       timestamp    NOT NULL
);

-- Uma matricula activa nao pode existir duas vezes.
CREATE UNIQUE INDEX uk_vehicles_active_plate ON tb_vehicles (plate_normalized) WHERE status = 'ACTIVE';
CREATE INDEX idx_vehicles_client ON tb_vehicles (client_id);

-- --- Catalogo de servicos ---------------------------------------------------

CREATE TABLE tb_services (
    id           bigserial     PRIMARY KEY,
    code         varchar(20),
    name         varchar(255)  NOT NULL,
    pricing_type varchar(20)   NOT NULL
                 CHECK (pricing_type IN ('BY_CATEGORY', 'FIXED', 'VARIABLE', 'HOURLY')),
    -- Preco unico (FIXED) ou tarifa por hora (HOURLY).
    base_price   numeric(18,2) CHECK (base_price IS NULL OR base_price >= 0),
    -- Categoria a que o servico se restringe (ex.: lavagem de mota); nulo = todas.
    category     varchar(20)   CHECK (category IS NULL OR category IN ('TURISMO', 'SUV', 'BUS', 'MOTA')),
    sort_order   int4          NOT NULL DEFAULT 0,
    status       varchar(20)   NOT NULL DEFAULT 'ACTIVE'
                 CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_by   varchar(255)  NOT NULL,
    created_at   timestamp     NOT NULL,
    updated_by   varchar(255)  NOT NULL,
    updated_at   timestamp     NOT NULL
);

CREATE TABLE tb_service_prices (
    id         bigserial     PRIMARY KEY,
    service_id bigint        NOT NULL REFERENCES tb_services (id) ON DELETE CASCADE,
    category   varchar(20)   NOT NULL CHECK (category IN ('TURISMO', 'SUV', 'BUS', 'MOTA')),
    price      numeric(18,2) NOT NULL CHECK (price >= 0),
    CONSTRAINT uk_service_price UNIQUE (service_id, category)
);

-- --- Entradas (atendimentos) ------------------------------------------------

CREATE TABLE tb_entries (
    id                     bigserial     PRIMARY KEY,
    number                 varchar(30)   NOT NULL UNIQUE,
    client_id              bigint        NOT NULL REFERENCES tb_clients (id),
    vehicle_id             bigint        REFERENCES tb_vehicles (id),
    -- Valores do momento do registo: nao mudam se a ficha mudar.
    client_name            varchar(255)  NOT NULL,
    vehicle_plate          varchar(32),
    vehicle_description    varchar(255),
    vehicle_category       varchar(20),
    entry_at               timestamptz   NOT NULL,
    work_status            varchar(20)   NOT NULL DEFAULT 'PENDING'
                           CHECK (work_status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'DELIVERED', 'CANCELLED')),
    total                  numeric(18,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    opening_balance        boolean       NOT NULL DEFAULT false,
    responsible            varchar(255)  NOT NULL,
    notes                  text,
    delivered_at           timestamptz,
    delivered_by           varchar(255),
    credit_authorized_by   varchar(255),
    credit_reason          text,
    cancelled_at           timestamptz,
    cancelled_by           varchar(255),
    cancel_reason          text,
    created_by             varchar(255)  NOT NULL,
    created_at             timestamp     NOT NULL,
    updated_by             varchar(255)  NOT NULL,
    updated_at             timestamp     NOT NULL
);

CREATE INDEX idx_entries_client  ON tb_entries (client_id, entry_at);
CREATE INDEX idx_entries_vehicle ON tb_entries (vehicle_id);
CREATE INDEX idx_entries_date    ON tb_entries (entry_at);

CREATE TABLE tb_entry_lines (
    id            bigserial     PRIMARY KEY,
    entry_id      bigint        NOT NULL REFERENCES tb_entries (id) ON DELETE CASCADE,
    service_id    bigint        REFERENCES tb_services (id),
    service_name  varchar(255)  NOT NULL,
    pricing_type  varchar(20)   NOT NULL,
    category      varchar(20),
    quantity      numeric(10,2) NOT NULL CHECK (quantity > 0),
    catalog_price numeric(18,2),
    unit_price    numeric(18,2) NOT NULL CHECK (unit_price >= 0),
    subtotal      numeric(18,2) NOT NULL CHECK (subtotal >= 0),
    adjust_reason text,
    sort_order    int4          NOT NULL DEFAULT 0
);

CREATE INDEX idx_entry_lines_entry ON tb_entry_lines (entry_id);

-- --- Pagamentos e afectacoes ------------------------------------------------

CREATE TABLE tb_payments (
    id              bigserial     PRIMARY KEY,
    number          varchar(30)   NOT NULL UNIQUE,
    client_id       bigint        NOT NULL REFERENCES tb_clients (id),
    client_name     varchar(255)  NOT NULL,
    kind            varchar(20)   NOT NULL DEFAULT 'PAYMENT'
                    CHECK (kind IN ('PAYMENT', 'REVERSAL')),
    payment_date    date          NOT NULL,
    -- Positivo nos pagamentos; negativo nos estornos.
    amount          numeric(18,2) NOT NULL CHECK (amount <> 0),
    method          varchar(20)   NOT NULL
                    CHECK (method IN ('CASH', 'MPESA', 'EMOLA', 'TRANSFER', 'POS')),
    reference       varchar(255),
    notes           text,
    status          varchar(20)   NOT NULL DEFAULT 'VALID'
                    CHECK (status IN ('VALID', 'REVERSED')),
    reversal_of_id  bigint        REFERENCES tb_payments (id),
    reason          text,
    -- Estorno com devolucao efectiva de dinheiro (vs. simples correccao de registo).
    refund          boolean       NOT NULL DEFAULT false,
    idempotency_key varchar(64)   UNIQUE,
    created_by      varchar(255)  NOT NULL,
    created_at      timestamp     NOT NULL,
    updated_by      varchar(255)  NOT NULL,
    updated_at      timestamp     NOT NULL,
    CHECK ((kind = 'PAYMENT' AND amount > 0) OR (kind = 'REVERSAL' AND amount < 0 AND reversal_of_id IS NOT NULL))
);

CREATE UNIQUE INDEX uk_payments_single_reversal ON tb_payments (reversal_of_id) WHERE reversal_of_id IS NOT NULL;
CREATE INDEX idx_payments_client ON tb_payments (client_id, payment_date);
CREATE INDEX idx_payments_date   ON tb_payments (payment_date);

CREATE TABLE tb_payment_allocations (
    id         bigserial     PRIMARY KEY,
    payment_id bigint        NOT NULL REFERENCES tb_payments (id),
    entry_id   bigint        NOT NULL REFERENCES tb_entries (id),
    amount     numeric(18,2) NOT NULL CHECK (amount <> 0)
);

CREATE INDEX idx_allocations_entry   ON tb_payment_allocations (entry_id);
CREATE INDEX idx_allocations_payment ON tb_payment_allocations (payment_id);

-- --- Despesas ---------------------------------------------------------------

CREATE TABLE tb_expense_categories (
    id     bigserial    PRIMARY KEY,
    name   varchar(100) NOT NULL UNIQUE,
    active boolean      NOT NULL DEFAULT true
);

CREATE TABLE tb_expenses (
    id             bigserial     PRIMARY KEY,
    number         varchar(30)   NOT NULL UNIQUE,
    kind           varchar(20)   NOT NULL DEFAULT 'EXPENSE'
                   CHECK (kind IN ('EXPENSE', 'REVERSAL')),
    description    varchar(255)  NOT NULL,
    category_id    bigint        NOT NULL REFERENCES tb_expense_categories (id),
    expense_date   date          NOT NULL,
    amount         numeric(18,2) NOT NULL CHECK (amount <> 0),
    method         varchar(20)   NOT NULL
                   CHECK (method IN ('CASH', 'MPESA', 'EMOLA', 'TRANSFER', 'POS')),
    notes          text,
    status         varchar(20)   NOT NULL DEFAULT 'VALID'
                   CHECK (status IN ('VALID', 'REVERSED')),
    reversal_of_id bigint        REFERENCES tb_expenses (id),
    reason         text,
    created_by     varchar(255)  NOT NULL,
    created_at     timestamp     NOT NULL,
    updated_by     varchar(255)  NOT NULL,
    updated_at     timestamp     NOT NULL,
    CHECK ((kind = 'EXPENSE' AND amount > 0) OR (kind = 'REVERSAL' AND amount < 0 AND reversal_of_id IS NOT NULL))
);

CREATE UNIQUE INDEX uk_expenses_single_reversal ON tb_expenses (reversal_of_id) WHERE reversal_of_id IS NOT NULL;
CREATE INDEX idx_expenses_date ON tb_expenses (expense_date);

-- --- Historico de alteracoes relevantes (autor, motivo, antes/depois) -------
-- O audit_log regista o utilizador da base de dados; este regista o utilizador
-- da aplicacao e o motivo, que e o que a especificacao exige.

CREATE TABLE tb_change_history (
    id          bigserial    PRIMARY KEY,
    entity_type varchar(30)  NOT NULL,
    entity_id   bigint       NOT NULL,
    action      varchar(50)  NOT NULL,
    reason      text,
    old_value   text,
    new_value   text,
    changed_by  varchar(255) NOT NULL,
    changed_at  timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX idx_change_history_entity ON tb_change_history (entity_type, entity_id, changed_at DESC);

-- --- Vista de saldos das entradas -------------------------------------------

CREATE VIEW vw_entries AS
SELECT e.id,
       e.number,
       e.client_id,
       e.vehicle_id,
       e.client_name,
       e.vehicle_plate,
       e.vehicle_description,
       e.vehicle_category,
       e.entry_at,
       (e.entry_at AT TIME ZONE 'Africa/Maputo')::date AS service_date,
       e.work_status,
       e.total,
       e.opening_balance,
       e.responsible,
       e.notes,
       COALESCE(a.paid, 0)                              AS paid,
       CASE WHEN e.work_status = 'CANCELLED' THEN 0
            ELSE e.total - COALESCE(a.paid, 0) END      AS balance,
       CASE WHEN e.work_status = 'CANCELLED' THEN 'CANCELLED'
            WHEN e.total - COALESCE(a.paid, 0) <= 0 THEN 'PAID'
            WHEN COALESCE(a.paid, 0) > 0 THEN 'PARTIAL'
            ELSE 'UNPAID' END                           AS payment_status
FROM tb_entries e
LEFT JOIN (SELECT entry_id, SUM(amount) AS paid
           FROM tb_payment_allocations
           GROUP BY entry_id) a ON a.entry_id = e.id;

-- --- Dados iniciais (especificacao, seccao 6) -------------------------------

INSERT INTO tb_services (code, name, pricing_type, base_price, category, sort_order, created_by, created_at, updated_by, updated_at)
VALUES ('A',    'Lavagem normal',          'BY_CATEGORY', NULL,    NULL,   10,  'system', now(), 'system', now()),
       ('B',    'Lavagem de motor',        'BY_CATEGORY', NULL,    NULL,   20,  'system', now(), 'system', now()),
       ('C',    'Lavagem de chassis',      'BY_CATEGORY', NULL,    NULL,   30,  'system', now(), 'system', now()),
       ('ABC',  'Pacote A + B + C',        'BY_CATEGORY', NULL,    NULL,   40,  'system', now(), 'system', now()),
       ('VAL',  'Vallet',                  'BY_CATEGORY', NULL,    NULL,   50,  'system', now(), 'system', now()),
       ('EXT',  'Lavagem exterior',        'BY_CATEGORY', NULL,    NULL,   60,  'system', now(), 'system', now()),
       ('ASP',  'Aspiração',               'BY_CATEGORY', NULL,    NULL,   70,  'system', now(), 'system', now()),
       ('MOTA', 'Lavagem de mota',         'FIXED',       300.00,  'MOTA', 80,  'system', now(), 'system', now()),
       ('POLG', 'Polimento geral',         'FIXED',       8500.00, NULL,   90,  'system', now(), 'system', now()),
       ('POLN', 'Polimento normal',        'FIXED',       6500.00, NULL,   100, 'system', now(), 'system', now()),
       ('POLM', 'Polimento à mão',         'FIXED',       1000.00, NULL,   110, 'system', now(), 'system', now()),
       ('COU',  'Tratamento de couro',     'FIXED',       4500.00, NULL,   120, 'system', now(), 'system', now()),
       ('PPF',  'PPF',                     'VARIABLE',    NULL,    NULL,   130, 'system', now(), 'system', now()),
       ('FUM',  'Fumados',                 'VARIABLE',    NULL,    NULL,   140, 'system', now(), 'system', now()),
       ('MO',   'Service / mão de obra',   'HOURLY',      1750.00, NULL,   150, 'system', now(), 'system', now());

INSERT INTO tb_service_prices (service_id, category, price)
SELECT s.id, p.category, p.price
FROM (VALUES
          ('A',   'TURISMO', 500.00),  ('A',   'SUV', 600.00),  ('A',   'BUS', 700.00),
          ('B',   'TURISMO', 400.00),  ('B',   'SUV', 500.00),  ('B',   'BUS', 500.00),
          ('C',   'TURISMO', 650.00),  ('C',   'SUV', 850.00),  ('C',   'BUS', 1000.00),
          ('ABC', 'TURISMO', 1300.00), ('ABC', 'SUV', 1700.00), ('ABC', 'BUS', 2000.00),
          ('VAL', 'TURISMO', 4000.00), ('VAL', 'SUV', 4500.00), ('VAL', 'BUS', 6500.00),
          ('EXT', 'TURISMO', 300.00),  ('EXT', 'SUV', 350.00),  ('EXT', 'BUS', 400.00),
          ('ASP', 'TURISMO', 300.00),  ('ASP', 'SUV', 350.00),  ('ASP', 'BUS', 400.00)
     ) AS p(code, category, price)
JOIN tb_services s ON s.code = p.code;

INSERT INTO tb_expense_categories (name) VALUES ('Água'), ('Energia'), ('Materiais'), ('Outros');

INSERT INTO tb_document_sequences (document_type, prefix, year)
SELECT 'ENTRY', 'ENT', EXTRACT(YEAR FROM now())
WHERE NOT EXISTS (SELECT 1 FROM tb_document_sequences WHERE document_type = 'ENTRY');

-- --- Auditoria das tabelas novas --------------------------------------------

DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY['tb_clients', 'tb_vehicles', 'tb_services', 'tb_service_prices', 'tb_entries',
                             'tb_entry_lines', 'tb_payments', 'tb_payment_allocations',
                             'tb_expense_categories', 'tb_expenses', 'tb_change_history']
    LOOP
        EXECUTE format(
            'DROP TRIGGER IF EXISTS %I_audit_trigger ON %I; '
            'CREATE TRIGGER %I_audit_trigger '
            'AFTER INSERT OR UPDATE OR DELETE ON %I '
            'FOR EACH ROW EXECUTE FUNCTION track_changes()',
            t, t, t, t
        );
    END LOOP;
END $$;

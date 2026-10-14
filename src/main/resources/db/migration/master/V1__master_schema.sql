-- =============================================================================
-- Base de dados MASTER
-- Identidade partilhada entre todos os tenants: utilizadores, perfis,
-- permissoes, registo de empresas e dados geograficos de referencia.
-- Os dados de negocio vivem na base de dados de cada tenant (ver db/migration/tenant).
-- =============================================================================

CREATE TABLE tb_users (
    id                  bigserial      PRIMARY KEY,
    username            varchar(255)   NOT NULL UNIQUE,
    email               varchar(255)   NOT NULL UNIQUE,
    password            varchar(255)   NOT NULL,
    name                varchar(255),
    first_name          varchar(255),
    middle_name         varchar(255),
    surname             varchar(255),
    gender              int2,
    nuit                int8           UNIQUE,
    phone               varchar(32),
    photo               varchar(255),
    user_status         int2           NOT NULL DEFAULT 1,
    email_verified_at   timestamptz(6),
    password_changed_at timestamptz(6),
    last_login_at       timestamptz(6),
    failed_login_count  int4           NOT NULL DEFAULT 0,
    locked_until        timestamptz(6),
    two_factor_enabled  boolean        NOT NULL DEFAULT false,
    two_factor_secret   varchar(255),
    created_at          timestamptz(6) DEFAULT now(),
    updated_at          timestamptz(6)
);

CREATE TABLE tb_roles (
    id           bigserial    PRIMARY KEY,
    name         varchar(255) NOT NULL UNIQUE,
    display_name varchar(255) NOT NULL UNIQUE,
    description  varchar(500),
    is_system    boolean      NOT NULL DEFAULT false
);

CREATE TABLE tb_permissions (
    id           bigserial    PRIMARY KEY,
    name         varchar(255) NOT NULL UNIQUE,
    display_name varchar(255) NOT NULL,
    context_path varchar(255),
    bundler      varchar(255) NOT NULL
);

CREATE TABLE tb_roles_users (
    user_id bigint NOT NULL REFERENCES tb_users (id) ON DELETE CASCADE,
    role_id bigint NOT NULL REFERENCES tb_roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE tb_permissions_roles (
    role_id       bigint NOT NULL REFERENCES tb_roles (id) ON DELETE CASCADE,
    permission_id bigint NOT NULL REFERENCES tb_permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Registo de empresas. Cada tenant corresponde a uma base de dados dedicada.
CREATE TABLE tb_tenants (
    id                bigserial    PRIMARY KEY,
    tenant_id         varchar(63)  NOT NULL UNIQUE,
    tenant_name       varchar(255) NOT NULL,
    database_url      varchar(255),
    database_username varchar(255),
    database_password varchar(255),
    plan              varchar(50)  NOT NULL DEFAULT 'STANDARD',
    is_active         boolean      NOT NULL DEFAULT true,
    created_at        timestamptz(6) DEFAULT now()
);

CREATE TABLE tb_user_tenants (
    id       bigserial    PRIMARY KEY,
    username varchar(255) NOT NULL,
    tenant   varchar(63)  NOT NULL,
    CONSTRAINT uk_user_tenant UNIQUE (username, tenant)
);

-- Tokens de refresh, rotativos e revogaveis.
CREATE TABLE tb_refresh_tokens (
    id         bigserial      PRIMARY KEY,
    user_id    bigint         NOT NULL REFERENCES tb_users (id) ON DELETE CASCADE,
    token_hash varchar(255)   NOT NULL UNIQUE,
    expires_at timestamptz(6) NOT NULL,
    revoked_at timestamptz(6),
    user_agent varchar(255),
    ip_address varchar(64),
    created_at timestamptz(6) DEFAULT now()
);

CREATE TABLE tb_password_reset_tokens (
    id         bigserial      PRIMARY KEY,
    user_id    bigint         NOT NULL REFERENCES tb_users (id) ON DELETE CASCADE,
    token_hash varchar(255)   NOT NULL UNIQUE,
    expires_at timestamptz(6) NOT NULL,
    used_at    timestamptz(6),
    created_at timestamptz(6) DEFAULT now()
);

-- --- Geografia de referencia (Mocambique) -----------------------------------

CREATE TABLE tb_countries (
    id          bigserial    PRIMARY KEY,
    description varchar(255) NOT NULL,
    slug        varchar(255),
    symbol      varchar(255),
    created_at  timestamp(6),
    created_by  int8
);

CREATE TABLE tb_provinces (
    id          bigserial    PRIMARY KEY,
    description varchar(255) NOT NULL,
    slug        varchar(255),
    country_id  bigint       REFERENCES tb_countries (id),
    created_at  timestamp(6),
    created_by  int8
);

CREATE TABLE tb_districts (
    id          bigserial    PRIMARY KEY,
    description varchar(255) NOT NULL,
    slug        varchar(255),
    province_id bigint       REFERENCES tb_provinces (id),
    created_at  timestamp(6),
    created_by  int8
);

CREATE TABLE tb_address (
    id              bigserial PRIMARY KEY,
    address_details varchar(255),
    house_nr        varchar(255),
    quarter         varchar(255),
    district        varchar(255),
    province        varchar(255)
);

CREATE INDEX idx_users_status        ON tb_users (user_status);
CREATE INDEX idx_user_tenants_user   ON tb_user_tenants (username);
CREATE INDEX idx_refresh_user        ON tb_refresh_tokens (user_id);
CREATE INDEX idx_refresh_expires     ON tb_refresh_tokens (expires_at);
CREATE INDEX idx_permissions_bundler ON tb_permissions (bundler);

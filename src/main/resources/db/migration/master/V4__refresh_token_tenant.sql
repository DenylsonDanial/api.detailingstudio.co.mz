-- Empresa activa da sessao: sobrevive ao reload do browser, quando o access
-- token (em memoria) desaparece e a sessao e reconstruida pelo refresh token.
ALTER TABLE tb_refresh_tokens
    ADD COLUMN tenant_id varchar(63);

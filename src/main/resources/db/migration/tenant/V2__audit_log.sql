-- =============================================================================
-- Registo de actividades ao nivel da base de dados.
-- Qualquer INSERT/UPDATE/DELETE em tabelas de negocio fica registado em JSONB,
-- sem necessidade de codigo aplicacional. Requisito 17 do escopo tecnico.
-- =============================================================================

CREATE TABLE IF NOT EXISTS audit_log (
    id         serial    PRIMARY KEY,
    table_name text      NOT NULL,
    operation  text      NOT NULL,
    old_data   jsonb,
    new_data   jsonb,
    changed_by text      NOT NULL DEFAULT current_user,
    changed_at timestamp NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_table ON audit_log (table_name, changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_when  ON audit_log (changed_at DESC);

CREATE OR REPLACE FUNCTION track_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'audit_log' THEN
        RETURN NULL;
    END IF;

    IF TG_OP = 'INSERT' THEN
        INSERT INTO audit_log (table_name, operation, new_data, changed_by)
        VALUES (TG_TABLE_NAME, 'INSERT', row_to_json(NEW)::jsonb, current_user);
    ELSIF TG_OP = 'UPDATE' THEN
        INSERT INTO audit_log (table_name, operation, old_data, new_data, changed_by)
        VALUES (TG_TABLE_NAME, 'UPDATE', row_to_json(OLD)::jsonb, row_to_json(NEW)::jsonb, current_user);
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO audit_log (table_name, operation, old_data, changed_by)
        VALUES (TG_TABLE_NAME, 'DELETE', row_to_json(OLD)::jsonb, current_user);
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Liga o trigger a todas as tabelas existentes no schema public.
-- Ao criar tabelas novas numa migracao futura, voltar a executar este bloco.
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN SELECT tablename FROM pg_tables WHERE schemaname = 'public' LOOP
        IF r.tablename NOT IN ('audit_log', 'flyway_schema_history') THEN
            EXECUTE format(
                'DROP TRIGGER IF EXISTS %I_audit_trigger ON %I; '
                'CREATE TRIGGER %I_audit_trigger '
                'AFTER INSERT OR UPDATE OR DELETE ON %I '
                'FOR EACH ROW EXECUTE FUNCTION track_changes()',
                r.tablename, r.tablename, r.tablename, r.tablename
            );
        END IF;
    END LOOP;
END $$;

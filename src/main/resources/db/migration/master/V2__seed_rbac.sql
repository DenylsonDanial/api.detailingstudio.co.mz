-- =============================================================================
-- Catalogo de perfis e permissoes - Detailing Studio.
-- Convencao de nome de permissao: <accao>:<recurso>  (ex.: create:entries)
-- O utilizador administrador NAO e criado aqui: e criado no arranque por
-- AdminBootstrapRunner a partir de ADMIN_BOOTSTRAP_* para nao guardar
-- credenciais em controlo de versoes.
--
-- Perfis da especificacao (seccao 8): Funcionario e Gestor. O SA gere a
-- plataforma (empresas) e e o perfil do administrador de arranque.
-- =============================================================================

INSERT INTO tb_roles (name, display_name, description, is_system) VALUES
    ('SA',          'Super Administrador', 'Gestao da plataforma e das empresas',                         true),
    ('MANAGER',     'Gestor',              'Controlo total da empresa: precos, conta corrente, estornos, relatorios e utilizadores', true),
    ('STAFF',       'Funcionario',         'Operacao diaria: clientes, viaturas, entradas, pagamentos e despesas', true);

-- Gera <accao>:<recurso> para cada recurso do sistema.
DO $$
DECLARE
    modulo   RECORD;
    accao    RECORD;
    etiqueta TEXT;
BEGIN
    FOR modulo IN
        SELECT * FROM (VALUES
            ('dashboard',   'Resumo',                 'Resumo'),
            ('entries',     'Entradas de viaturas',   'Operacao diaria'),
            ('payments',    'Pagamentos',             'Operacao diaria'),
            ('clients',     'Clientes',               'Clientes e contas'),
            ('vehicles',    'Viaturas',               'Clientes e contas'),
            ('statements',  'Extractos',              'Clientes e contas'),
            ('expenses',    'Despesas',               'Despesas'),
            ('services',    'Servicos e precos',      'Servicos e precos'),
            ('reports',     'Relatorios',             'Relatorios'),
            ('users',       'Utilizadores',           'Administracao'),
            ('roles',       'Perfis',                 'Administracao'),
            ('permissions', 'Permissoes',             'Administracao'),
            ('settings',    'Definicoes',             'Administracao'),
            ('audit',       'Registo de Actividades', 'Administracao'),
            ('companies',   'Empresas',               'Plataforma')
        ) AS t(recurso, nome, bundler)
    LOOP
        FOR accao IN
            SELECT * FROM (VALUES
                ('read',    'Consultar'),
                ('create',  'Criar'),
                ('update',  'Editar'),
                ('delete',  'Eliminar'),
                ('export',  'Exportar')
            ) AS a(accao, verbo)
        LOOP
            etiqueta := accao.verbo || ' ' || lower(modulo.nome);
            INSERT INTO tb_permissions (name, display_name, bundler)
            VALUES (accao.accao || ':' || modulo.recurso, etiqueta, modulo.bundler);
        END LOOP;
    END LOOP;
END $$;

-- Accoes reservadas ao gestor (especificacao, seccoes 5, 6, 7 e 8).
INSERT INTO tb_permissions (name, display_name, bundler) VALUES
    ('manage:credit',          'Alterar conta corrente autorizada do cliente',      'Clientes e contas'),
    ('reassign:vehicles',      'Reassociar viatura a outro cliente',                'Clientes e contas'),
    ('adjust:prices',          'Ajustar preco numa linha de servico (com motivo)',  'Operacao diaria'),
    ('authorize:credit',       'Autorizar entrega com saldo pendente (com motivo)', 'Operacao diaria'),
    ('cancel:entries',         'Cancelar entradas',                                  'Operacao diaria'),
    ('manage:delivered',       'Alterar entradas depois da entrega',                 'Operacao diaria'),
    ('reverse:payments',       'Estornar pagamentos e registar devolucoes',          'Operacao diaria'),
    ('reverse:expenses',       'Corrigir despesas por estorno',                      'Despesas'),
    ('backdate:movements',     'Lancamentos com data retroactiva',                   'Operacao diaria');

-- --- Atribuicao de permissoes aos perfis -------------------------------------

-- SA: tudo.
INSERT INTO tb_permissions_roles (role_id, permission_id)
SELECT r.id, p.id FROM tb_roles r CROSS JOIN tb_permissions p WHERE r.name = 'SA';

-- MANAGER (Gestor): tudo excepto a gestao de empresas da plataforma.
INSERT INTO tb_permissions_roles (role_id, permission_id)
SELECT r.id, p.id FROM tb_roles r CROSS JOIN tb_permissions p
WHERE r.name = 'MANAGER' AND p.name NOT LIKE '%:companies';

-- STAFF (Funcionario): matriz inicial da especificacao, seccao 8.
--   Criar clientes e viaturas ......... sim
--   Lancar servicos e pagamentos ...... sim
--   Registar despesas ................. sim
--   Consultar historico operacional ... sim
--   Alterar conta autorizada e precos . nao
--   Estornar ou cancelar movimentos ... nao
--   Resumo global e exportar .......... nao por defeito
--   Gerir utilizadores ................ nao
-- O funcionario edita entradas em curso; a restricao "depois da entrega" e
-- aplicada no servico, alem da permissao manage:delivered.
INSERT INTO tb_permissions_roles (role_id, permission_id)
SELECT r.id, p.id FROM tb_roles r CROSS JOIN tb_permissions p
WHERE r.name = 'STAFF'
  AND p.name IN (
      'read:clients',   'create:clients',   'update:clients',
      'read:vehicles',  'create:vehicles',  'update:vehicles',
      'read:entries',   'create:entries',   'update:entries',
      'read:payments',  'create:payments',
      'read:expenses',  'create:expenses',
      'read:services',
      'read:statements'
  );

-- Campos de perfil de empresa para ERP (nao so contabilidade) + seed de moedas.

ALTER TABLE tb_business_settings
    ADD COLUMN IF NOT EXISTS website varchar(255),
    ADD COLUMN IF NOT EXISTS activity_sector varchar(150),
    ADD COLUMN IF NOT EXISTS default_tax_rate numeric(5, 2) NOT NULL DEFAULT 16;

-- Ordem fixa: id 1 = MZN, 2 = USD, 3 = EUR em instalacoes novas.
INSERT INTO tb_currencies (name, short_name, symbol)
SELECT v.name, v.short_name, v.symbol
FROM (VALUES
          ('Metical', 'MZN', 'MT'),
          ('Dólar americano', 'USD', 'US$'),
          ('Euro', 'EUR', '€')
     ) AS v(name, short_name, symbol)
WHERE NOT EXISTS (SELECT 1 FROM tb_currencies LIMIT 1);

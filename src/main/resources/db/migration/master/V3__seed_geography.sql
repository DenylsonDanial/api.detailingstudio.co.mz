-- Geografia de referencia: Mocambique e as suas provincias.

INSERT INTO tb_countries (description, slug, symbol) VALUES ('Mocambique', 'mocambique', 'MZ');

INSERT INTO tb_provinces (description, slug, country_id)
SELECT v.descricao, v.slug, c.id
FROM tb_countries c,
     (VALUES
         ('Maputo Cidade', 'maputo-cidade'),
         ('Maputo',        'maputo'),
         ('Gaza',          'gaza'),
         ('Inhambane',     'inhambane'),
         ('Sofala',        'sofala'),
         ('Manica',        'manica'),
         ('Tete',          'tete'),
         ('Zambezia',      'zambezia'),
         ('Nampula',       'nampula'),
         ('Cabo Delgado',  'cabo-delgado'),
         ('Niassa',        'niassa')
     ) AS v(descricao, slug)
WHERE c.slug = 'mocambique';

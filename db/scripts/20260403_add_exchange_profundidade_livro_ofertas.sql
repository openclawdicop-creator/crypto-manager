ALTER TABLE exchange
ADD COLUMN IF NOT EXISTS profundidade_livro_ofertas INTEGER DEFAULT 10;

UPDATE exchange
SET profundidade_livro_ofertas = 10
WHERE profundidade_livro_ofertas IS NULL
   OR profundidade_livro_ofertas <= 0;

ALTER TABLE exchange
ALTER COLUMN profundidade_livro_ofertas SET DEFAULT 10;

ALTER TABLE exchange
ALTER COLUMN profundidade_livro_ofertas SET NOT NULL;

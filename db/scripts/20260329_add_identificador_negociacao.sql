ALTER TABLE parametrizacao_consulta_preco
ADD COLUMN IF NOT EXISTS identificador_negociacao VARCHAR(100);

UPDATE parametrizacao_consulta_preco
SET identificador_negociacao = CASE
    WHEN id = 1 THEN 'BTCUSDT'
    WHEN id = 2 THEN 'ETHUSDT'
    WHEN id = 3 THEN 'BTCUSDC'
    WHEN id = 4 THEN 'ETHUSDT'
    WHEN id = 5 THEN 'BNBUSDT'
    ELSE identificador_negociacao
END
WHERE identificador_negociacao IS NULL;

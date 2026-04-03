ALTER TABLE ativo_financeiro_rede
    ADD COLUMN IF NOT EXISTS quantidade_casas_decimais INTEGER DEFAULT 6 NOT NULL;

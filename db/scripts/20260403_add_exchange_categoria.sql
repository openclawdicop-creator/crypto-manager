ALTER TABLE exchange
ADD COLUMN IF NOT EXISTS categoria VARCHAR(20) DEFAULT 'SPOT';

UPDATE exchange
SET categoria = 'SPOT'
WHERE categoria IS NULL
   OR categoria NOT IN ('SPOT', 'FUTURO');

ALTER TABLE exchange
ALTER COLUMN categoria SET DEFAULT 'SPOT';

ALTER TABLE exchange
ALTER COLUMN categoria SET NOT NULL;

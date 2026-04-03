-- Insert admin user with BCrypt hashed password (admin1234)
-- Hash gerado com BCrypt para: admin1234
INSERT INTO usuario (id, username, password, email, ativo) VALUES 
(1, 'admin', '$2a$10$MpgOWXjDvQ5aoyPWDa5Ls.4mQUDlUs7y1Nba1Tf1wC9E8PiWU2YTW', 'admin@cryptomanager.com', true);

-- Insert sample exchanges
INSERT INTO exchange (id, nome, descricao, tipo, tipo_api, categoria, token_api, url_api, profundidade_livro_ofertas, log_habilitado, usar_proxy) VALUES
(1, 'Binance', 'Maior exchange de criptomoedas do mundo', 'CENTRALIZADA', 'REST', 'SPOT', 'binance_api_token_123', 'https://api.binance.com', 10, true, false),
(2, 'Coinbase', 'Exchange americana de criptomoedas', 'CENTRALIZADA', 'REST', 'SPOT', 'coinbase_api_token_456', 'https://api.coinbase.com', 10, true, false),
(3, 'Uniswap', 'Exchange descentralizada na rede Ethereum', 'DESCENTRALIZADA', 'WEBSOCKET', 'SPOT', 'uniswap_api_token_789', 'https://api.uniswap.org', 10, false, false),
(4, 'MEXC', 'Exchange centralizada com API Spot v3 compativel para order book', 'CENTRALIZADA', 'REST', 'SPOT', 'mexc_api_token_321', 'https://api.mexc.com', 10, true, false);

-- Insert sample networks
INSERT INTO rede (id, nome, url_explorer) VALUES
(1, 'Ethereum', 'https://etherscan.io'),
(2, 'Bitcoin', 'https://blockchain.info'),
(3, 'Binance Smart Chain', 'https://bscscan.com'),
(4, 'Polygon', 'https://polygonscan.com');

-- Insert sample financial assets
INSERT INTO ativo_financeiro (id, nome, simbolo) VALUES
(1, 'Bitcoin', 'BTC'),
(2, 'Ethereum', 'ETH'),
(3, 'Tether', 'USDT'),
(4, 'Binance Coin', 'BNB'),
(5, 'USD Coin', 'USDC'),
(6, 'Cardano', 'ADA'),
(7, 'Solana', 'SOL'),
(8, 'Polygon', 'MATIC');

-- Insert sample asset network settings
INSERT INTO ativo_financeiro_rede (id, ativo_financeiro_id, rede_id, identificador, quantidade_casas_decimais) VALUES
(1, 1, 2, 'BTC', 8),
(2, 2, 1, 'ETH', 18),
(3, 3, 1, 'USDT', 6),
(4, 3, 3, 'USDT', 18),
(5, 4, 3, 'BNB', 18);

-- Insert sample price query configurations
INSERT INTO parametrizacao_consulta_preco (id, exchange_id, rede_id, ativo_desejado_id, ativo_pagamento_id, quantidade_pagamento, identificador_negociacao, ativa, log_habilitado) VALUES
(1, 1, 1, 1, 3, 1.0, 'BTCUSDT', true, true),
(2, 1, 1, 2, 3, 10.0, 'ETHUSDT', true, true),
(3, 2, 1, 1, 5, 0.5, 'BTCUSDC', true, false),
(4, 3, 1, 2, 3, 5.0, 'ETHUSDT', true, true),
(5, 1, 3, 4, 3, 2.0, 'BNBUSDT', false, true);

-- Insert sample price history
INSERT INTO historico_cotacao (id, parametrizacao_id, data_hora_consulta, cotacao_compra, cotacao_venda) VALUES
(1, 1, CURRENT_TIMESTAMP(), 42500.50, 42480.10),
(2, 1, CURRENT_TIMESTAMP(), 42650.75, 42610.20),
(3, 2, CURRENT_TIMESTAMP(), 2280.30, 2274.90),
(4, 3, CURRENT_TIMESTAMP(), 42480.00, 42440.55),
(5, 4, CURRENT_TIMESTAMP(), 2275.80, 2270.35);

-- Insert sample proxies
INSERT INTO proxy (id, nome, url, usuario, senha, token, porta) VALUES
(1, 'Proxy Europa 1', 'http://185.123.45.67:8080', 'user_proxy', 'pass123', 'tok_eur_001', 8080),
(2, 'Proxy EUA 2', 'http://64.233.191.255:3128', NULL, NULL, NULL, 3128),
(3, 'Proxy Brasil 3', 'https://200.155.10.2:443', 'admin_br', 'secret_br', 'tok_br_55', 443);

ALTER TABLE usuario ALTER COLUMN id RESTART WITH 2;
ALTER TABLE exchange ALTER COLUMN id RESTART WITH 5;
ALTER TABLE rede ALTER COLUMN id RESTART WITH 5;
ALTER TABLE ativo_financeiro ALTER COLUMN id RESTART WITH 9;
ALTER TABLE ativo_financeiro_rede ALTER COLUMN id RESTART WITH 6;
ALTER TABLE parametrizacao_consulta_preco ALTER COLUMN id RESTART WITH 6;
ALTER TABLE historico_cotacao ALTER COLUMN id RESTART WITH 6;
ALTER TABLE proxy ALTER COLUMN id RESTART WITH 4;

-- Insert default scheduling routine
INSERT INTO agendamento (id, nome, frequencia_segundos, ativo) VALUES
(1, 'Rotina Global de Consulta', 60, true);

ALTER TABLE agendamento ALTER COLUMN id RESTART WITH 2;

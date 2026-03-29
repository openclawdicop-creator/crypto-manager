-- Insert admin user with BCrypt hashed password (admin1234)
-- Hash gerado com BCrypt para: admin1234
INSERT INTO usuario (id, username, password, email, ativo) VALUES 
(1, 'admin', '$2a$10$MpgOWXjDvQ5aoyPWDa5Ls.4mQUDlUs7y1Nba1Tf1wC9E8PiWU2YTW', 'admin@cryptomanager.com', true);

-- Insert sample exchanges
INSERT INTO exchange (id, nome, descricao, tipo, tipo_api, token_api, url_api, log_habilitado) VALUES
(1, 'Binance', 'Maior exchange de criptomoedas do mundo', 'CENTRALIZADA', 'REST', 'binance_api_token_123', 'https://api.binance.com', true),
(2, 'Coinbase', 'Exchange americana de criptomoedas', 'CENTRALIZADA', 'REST', 'coinbase_api_token_456', 'https://api.coinbase.com', true),
(3, 'Uniswap', 'Exchange descentralizada na rede Ethereum', 'DESCENTRALIZADA', 'WEBSOCKET', 'uniswap_api_token_789', 'https://api.uniswap.org', false);

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

-- Insert sample price query configurations
INSERT INTO parametrizacao_consulta_preco (id, exchange_id, rede_id, ativo_desejado_id, ativo_pagamento_id, quantidade_compra, identificador_negociacao, ativa, log_habilitado) VALUES
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

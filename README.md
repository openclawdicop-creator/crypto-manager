# Crypto Manager - Sistema de Gerenciamento de Negociação de Criptomoedas

## Prompt Completo (v2.0)

Criar sistema web para gerenciamento de negociação de criptomoedas com Quarkus 3.17.2, Java 17, Hibernate Panache, banco H2, autenticação JWT e arquitetura em camadas.

### ARQUITETURA
Entity → Repository → Service → Resource (API) → Frontend

### ESTRUTURA
```
src/main/java/org/acme/
├── entity/ (6 entidades: Usuario, Exchange, Rede, AtivoFinanceiro, ParametrizacaoConsultaPreco, HistoricoCotacao + enums TipoExchange, TipoApi)
├── repository/ (6 repositories estendendo PanacheRepository)
├── service/ (6 services com regras de negócio)
└── resource/ (6 resources: API REST + AuthResource para login/logout)
```

### ENTIDADES

1. **Usuario**: id, username (único), password (BCrypt), email, ativo
2. **Exchange**: id, nome, descricao, tipo (CENTRALIZADA/DESCENTRALIZADA), tipoApi (REST/WEBSOCKET), tokenApi, urlApi, logHabilitado
3. **Rede**: id, nome, urlExplorer
4. **AtivoFinanceiro**: id, nome, simbolo (único)
5. **ParametrizacaoConsultaPreco**: id, exchange_id, rede_id, token_compra_id, token_venda_id, quantidade_compra, ativa, log_habilitado
6. **HistoricoCotacao**: id, parametrizacao_id, data_hora_consulta, cotacao

### AUTENTICAÇÃO JWT
- SmallRye JWT + Quarkus Security
- POST /api/auth/login (retorna JWT)
- POST /api/auth/logout
- Todos endpoints /api/* exigem Authorization: Bearer <token>
- Token sem expiração (31536000 segundos)
- Usuário inicial: admin / admin1234 (senha com hash BCrypt)

### PADRÃO DE RESPOSTA JSON
```json
{
  "error": false,
  "message": "...",
  "data": { ... }
}
```

### ENDPOINTS PARA CADA ENTIDADE
- GET /api/{entidade} - Listar (com filtros query params)
- GET /api/{entidade}/{id} - Buscar por ID
- POST /api/{entidade} - Cadastrar
- PUT /api/{entidade}/{id} - Atualizar
- DELETE /api/{entidade}/{id} - Excluir

### FRONTEND
- Qute templates (layout.html, login.html, index.html, exchanges.html, redes.html, ativos.html, parametrizacoes.html, historicos.html)
- HTMX para atualizações parciais
- Alpine.js para interatividade (modais)
- TailwindCSS via CDN
- Design glassmorphism (transparências, blur, gradientes)
- Mobile-first responsivo
- Página de login (sem cadastro)
- JWT armazenado no localStorage

### CONFIGURAÇÕES (application.properties)
```properties
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:cryptodb;DB_CLOSE_DELAY=-1
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.sql-load-script=import.sql
mp.jwt.verify.publickey.location=publicKey.pem
smallrye.jwt.sign.key.location=privateKey.pem
smallrye.jwt.new-token.lifetime=31536000
```

### IMPORT.SQL
- Criar usuário admin com senha hash BCrypt ($2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
- Inserir dados de exemplo para todas as entidades

## Tecnologias Utilizadas

- **Backend**: Quarkus 3.17.2, Java 17, Hibernate ORM with Panache
- **Banco de Dados**: H2 (in-memory)
- **Segurança**: SmallRye JWT, BCrypt
- **Frontend**: Qute Templates, HTMX, Alpine.js, TailwindCSS
- **Build**: Maven

## Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.8+

### Passos

1. Clone o projeto
2. Navegue até o diretório do projeto
3. Execute:
```bash
./mvnw quarkus:dev
```

4. Acesse: http://localhost:8080

### Credenciais de Acesso
- **Username**: admin
- **Senha**: admin1234

## Estrutura do Projeto

```
crypto-manager/
├── src/
│   ├── main/
│   │   ├── java/org/acme/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── resource/
│   │   └── resources/
│   │       ├── templates/
│   │       ├── application.properties
│   │       ├── import.sql
│   │       ├── publicKey.pem
│   │       └── privateKey.pem
│   └── test/
├── pom.xml
└── README.md
```

## Endpoints da API

### Autenticação
- POST /api/auth/login
- POST /api/auth/logout
- GET /api/auth/me

### Usuários
- GET /api/usuarios
- GET /api/usuarios/{id}
- POST /api/usuarios
- PUT /api/usuarios/{id}
- DELETE /api/usuarios/{id}

### Exchanges
- GET /api/exchanges
- GET /api/exchanges/{id}
- POST /api/exchanges
- PUT /api/exchanges/{id}
- DELETE /api/exchanges/{id}

### Redes
- GET /api/redes
- GET /api/redes/{id}
- POST /api/redes
- PUT /api/redes/{id}
- DELETE /api/redes/{id}

### Ativos Financeiros
- GET /api/ativos
- GET /api/ativos/{id}
- POST /api/ativos
- PUT /api/ativos/{id}
- DELETE /api/ativos/{id}

### Parametrizações
- GET /api/parametrizacoes
- GET /api/parametrizacoes/{id}
- GET /api/parametrizacoes/ativas
- POST /api/parametrizacoes
- PUT /api/parametrizacoes/{id}
- DELETE /api/parametrizacoes/{id}

### Históricos de Cotação
- GET /api/historicos
- GET /api/historicos/{id}
- GET /api/historicos/parametrizacao/{parametrizacaoId}
- POST /api/historicos
- DELETE /api/historicos/{id}

## Licença

MIT License

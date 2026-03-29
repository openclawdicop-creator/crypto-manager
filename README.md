# Crypto Manager

Aplicacao web para gerenciamento de ativos, exchanges, redes, parametrizacoes de consulta e historico de cotacoes de criptoativos. O backend foi construido com Quarkus 3 e o frontend atual e uma SPA Vue 3 servida junto da aplicacao via Quinoa.

## Visao geral

- Backend REST em Java 17 com Quarkus 3.17.2
- Persistencia com Hibernate ORM Panache
- Banco H2 em memoria, recriado a cada subida
- Autenticacao com JWT e senhas com BCrypt
- Frontend Vue 3 + Vue Router + Vite em `src/main/webui`
- Porta padrao da aplicacao: `9001`

## Funcionalidades

- Login com emissao de token JWT
- Dashboard com contadores e ultimas cotacoes registradas
- CRUD de ativos financeiros
- CRUD de exchanges
- CRUD de redes
- CRUD de parametrizacoes de consulta de preco
- Consulta e exclusao de historicos de cotacao
- API de usuarios disponivel no backend, sem tela dedicada no frontend

## Stack

### Backend

- Java 17
- Quarkus REST Jackson
- Quarkus Security
- SmallRye JWT
- Hibernate ORM with Panache
- H2
- jBCrypt

### Frontend

- Vue 3
- Vue Router 4
- Vite
- Quarkus Quinoa para integrar o build do frontend ao backend

## Arquitetura

O backend segue a divisao:

`entity -> repository -> service -> resource`

Principais modulos:

- `src/main/java/org/acme/entity`: entidades JPA e enums
- `src/main/java/org/acme/repository`: repositories Panache
- `src/main/java/org/acme/service`: regras de negocio e geracao de JWT
- `src/main/java/org/acme/resource`: endpoints REST e padrao de resposta
- `src/main/resources`: configuracoes, chaves JWT e carga inicial
- `src/main/webui`: SPA Vue 3

## Modelo de dados

Entidades principais:

- `Usuario`: username, password, email, ativo
- `Exchange`: nome, descricao, tipo, tipoApi, tokenApi, urlApi, logHabilitado
- `Rede`: nome, urlExplorer
- `AtivoFinanceiro`: nome, simbolo
- `ParametrizacaoConsultaPreco`: exchange, rede, tokenCompra, tokenVenda, quantidadeCompra, ativa, logHabilitado
- `HistoricoCotacao`: parametrizacao, dataHoraConsulta, cotacaoCompra, cotacaoVenda

Enums:

- `TipoExchange`: `CENTRALIZADA`, `DESCENTRALIZADA`
- `TipoApi`: `REST`, `WEBSOCKET`

## Dados iniciais

Ao iniciar a aplicacao, o banco e recriado com os dados de `src/main/resources/import.sql`.

Credenciais padrao:

- Usuario: `admin`
- Senha: `admin1234`

Carga inicial incluida:

- 1 usuario administrador
- 3 exchanges
- 4 redes
- 8 ativos financeiros
- 5 parametrizacoes
- 5 registros de historico

## Como executar

### Pre-requisitos

- Java 17+
- Maven 3.8+
- Node.js instalado no ambiente se o Quinoa precisar reconstruir o frontend

### Subir a aplicacao completa

No diretorio raiz do projeto:

```bash
mvn quarkus:dev
```

Depois acesse:

- Aplicacao: [http://localhost:9001](http://localhost:9001)
- API base: [http://localhost:9001/api](http://localhost:9001/api)

### Build do projeto

```bash
mvn clean package
```

### Frontend isolado

O frontend fica em `src/main/webui`, mas ele foi pensado para ser servido pelo Quarkus. Se voce rodar o Vite isoladamente com `npm run dev`, sera necessario configurar proxy para `/api`, porque hoje o projeto nao traz essa configuracao no `vite.config.js`.

Para gerar apenas o build do frontend:

```bash
cd src/main/webui
npm install
npm run build
```

## Fluxo de autenticacao

- `POST /api/auth/login` recebe `username` e `password`
- Em caso de sucesso retorna token JWT e dados basicos do usuario
- O frontend salva `token` e `user` em `localStorage`
- Endpoints protegidos exigem `Authorization: Bearer <token>`
- O token tem validade de 1 ano (`31536000` segundos)

Exemplo de login:

```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin1234"
}
```

Resposta de sucesso:

```json
{
  "error": false,
  "message": "Login realizado com sucesso",
  "data": {
    "token": "eyJ...",
    "username": "admin",
    "email": "admin@cryptomanager.com"
  }
}
```

## Padrao de resposta

As rotas REST retornam JSON no formato:

```json
{
  "error": false,
  "message": "Operacao realizada com sucesso",
  "data": {}
}
```

Em falhas de negocio, o backend normalmente retorna `error: true` com a mensagem correspondente. Excecoes nao tratadas passam pelo `GlobalExceptionMapper`.

## Endpoints principais

### Autenticacao

- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/me`
- `POST /api/auth/reset-admin`
- `GET /api/auth/check-admin`

Observacao: `reset-admin` e `check-admin` nao exigem autenticacao no estado atual do codigo.

### Usuarios

- `GET /api/usuarios`
- `GET /api/usuarios/{id}`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `DELETE /api/usuarios/{id}`

### Ativos

- `GET /api/ativos`
- `GET /api/ativos/{id}`
- `POST /api/ativos`
- `PUT /api/ativos/{id}`
- `DELETE /api/ativos/{id}`

### Exchanges

- `GET /api/exchanges`
- `GET /api/exchanges/{id}`
- `POST /api/exchanges`
- `PUT /api/exchanges/{id}`
- `DELETE /api/exchanges/{id}`

### Redes

- `GET /api/redes`
- `GET /api/redes/{id}`
- `POST /api/redes`
- `PUT /api/redes/{id}`
- `DELETE /api/redes/{id}`

### Parametrizacoes

- `GET /api/parametrizacoes`
- `GET /api/parametrizacoes/{id}`
- `GET /api/parametrizacoes/ativas`
- `POST /api/parametrizacoes`
- `PUT /api/parametrizacoes/{id}`
- `DELETE /api/parametrizacoes/{id}`

### Historicos

- `GET /api/historicos`
- `GET /api/historicos/{id}`
- `GET /api/historicos/parametrizacao/{parametrizacaoId}`
- `POST /api/historicos`
- `DELETE /api/historicos/{id}`

## Frontend atual

Rotas da SPA:

- `/login`
- `/dashboard`
- `/ativos`
- `/exchanges`
- `/redes`
- `/parametrizacoes`
- `/historicos`

Comportamento principal:

- guarda JWT em `localStorage`
- redireciona para `/login` ao receber `401`
- exibe dashboard com contadores e ultimas 10 cotacoes
- permite criar, editar e excluir ativos, exchanges, redes e parametrizacoes
- permite filtrar historicos por parametrizacao e excluir registros

## Estrutura do projeto

```text
crypto-manager/
|-- pom.xml
|-- README.md
|-- GenerateKeys.java
|-- src/
|   |-- main/
|   |   |-- java/org/acme/
|   |   |   |-- entity/
|   |   |   |-- repository/
|   |   |   |-- resource/
|   |   |   `-- service/
|   |   |-- resources/
|   |   |   |-- application.properties
|   |   |   |-- import.sql
|   |   |   |-- private_key.pem
|   |   |   `-- public_key.pem
|   |   `-- webui/
|   |       |-- package.json
|   |       |-- vite.config.js
|   |       `-- src/
|   `-- test/
`-- target/
```

## Observacoes importantes

- O banco H2 e em memoria e usa `drop-and-create`, entao todos os dados sao reinicializados a cada start.
- O frontend antigo descrito no README anterior com Qute, HTMX e Alpine.js nao corresponde mais ao estado atual do projeto.
- Nao ha testes automatizados em `src/test` no momento.
- Existe um utilitario `GenerateKeys.java` na raiz para gerar novo par de chaves JWT em `src/main/resources`.

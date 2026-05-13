# HelpDesk Queue Manager

API REST para gerenciamento de filas de atendimento, desenvolvida com Spring Boot 3 e Java 21.

O sistema distribui chamados automaticamente entre os balcões disponíveis, sempre priorizando o de menor carga. Quando todos atingem a capacidade máxima, o ticket é enfileirado e processado em background sem a necessidade de um broker externo.

---

## Funcionalidades

- Criação e gerenciamento de balcões de atendimento
- Ciclo de vida completo de chamados: `ABERTO` → `EM_ESPERA` → `EM_ATENDIMENTO` → `CONCLUIDO`
- Balanceamento de carga automático (least-loaded) entre balcões
- Fila de espera com `DelayQueue` nativa do Java — sem broker externo
- Promoção automática de tickets da fila a cada 30 segundos via scheduler
- HTTP `202 Accepted` para tickets enfileirados (semântica REST correta)
- Migrações de banco gerenciadas com Flyway
- Timestamps automáticos de criação e resolução de chamados

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.0 |
| PostgreSQL | via Docker Compose |
| Flyway | gerenciamento de migrations |
| Gradle | build tool |
| Lombok | redução de boilerplate |

---

## Pré-requisitos

- Java 21+
- Docker e Docker Compose

---

## Como executar

**1. Clone o repositório**
```bash
git clone https://github.com/seu-usuario/projeto-pratico.git
cd projeto-pratico
```

**2. Suba o banco de dados**
```bash
docker compose up -d
```

**3. Execute a aplicação**
```bash
./gradlew bootRun
```

A API estará disponível em `http://localhost:8080`.

---

## Endpoints

### Balcões — `/balcoes`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/balcoes` | Cria um novo balcão |
| `GET` | `/balcoes` | Lista todos os balcões |
| `GET` | `/balcoes/{id}` | Busca balcão por ID |
| `PUT` | `/balcoes/{id}` | Atualiza um balcão |
| `DELETE` | `/balcoes/{id}` | Remove um balcão |

### Chamados — `/chamados`

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/chamados` | Abre um chamado (pode retornar `202` se enfileirado) |
| `GET` | `/chamados` | Lista todos os chamados |
| `GET` | `/chamados/{id}` | Busca chamado por ID |
| `PUT` | `/chamados/{id}` | Atualiza status ou dados do chamado |
| `DELETE` | `/chamados/{id}` | Remove um chamado |

---

## Regras de negócio

- Cada balcão suporta no máximo **5 chamados ativos** simultaneamente.
- Novos chamados são atribuídos ao balcão com **menor carga** no momento.
- Se todos os balcões estiverem cheios, o chamado entra na **fila de espera** com uma janela de **3 minutos**.
- O scheduler verifica a fila a cada **30 segundos** e promove chamados assim que houver capacidade disponível.

---

## Testes

### Unitários (sem banco)

```bash
./gradlew test --tests "com.example.Projeto.pratico.service.*"
```

Cobre a máquina de estados (`MaquinaDeEstadosChamadoTest`) e o validador de capacidade (`ValidadorChamadoTest`).

### Todos os testes (requer banco)

**1. Inicie o Docker**
```bash
colima start          # se estiver usando Colima no macOS
```

**2. Suba o banco**
```bash
docker compose up -d
```

**3. Execute os testes**
```bash
./gradlew test
```

**4. Veja o relatório**
```bash
open build/reports/tests/test/index.html
```

**5. Derrube o banco ao terminar**
```bash
docker compose down
```

---

## Estrutura do projeto

```
src/main/java/com/example/Projeto/pratico/
├── config/         # Configurações de segurança e scheduler
├── controller/     # Endpoints REST
├── service/        # Regras de negócio
├── scheduler/      # Processamento em background da fila
├── queue/          # Elemento da fila de espera (DelayQueue)
├── model/          # Entidades JPA
├── dto/            # Objetos de request/response
├── repository/     # Acesso a dados (Spring Data JPA)
├── exception/      # Exceções customizadas e handler global
└── enums/          # StatusChamado, TipoChamado
```

# api.detailingstudio.co.mz

Backend do sistema de gestao do Detailing Studio. Java 17 + Spring Boot 3 +
PostgreSQL, construido sobre o nucleo da API Everlee Grace (autenticacao com
refresh rotativo, utilizadores, perfis e permissoes, multi-empresa, auditoria e
exportacao PDF/Excel).

Especificacao funcional: `../Detailing_Studio_Especificacao_Developer.docx`.

## Portas em desenvolvimento

Escolhidas para correr ao lado da Everlee Grace sem colisoes:

| Servico | Detailing Studio | Everlee Grace |
| --- | --- | --- |
| PostgreSQL (host) | **5442** | 5432 |
| API | **8090** | 8080 |
| Frontend | **8095** | 8085 |
| Contentores | `ds-db`, `ds-api` | `gestao-*` |
| Base master | `ds_master` | `eg_master` |

## Arrancar em local

Precisas de JDK 17 e Docker Desktop.

```bash
cp .env.example .env          # preencher DB_PASSWORD, JWT_SECRET e ADMIN_BOOTSTRAP_*
docker compose up -d db       # PostgreSQL em localhost:5442
./mvnw spring-boot:run        # API em http://localhost:8090
```

No Windows: `mvnw.cmd spring-boot:run`. As variaveis do `.env` tem de estar no
ambiente (o IntelliJ le-as com o plugin EnvFile; na consola, carrega-as antes).

Tudo em contentores: `docker compose --profile full up -d --build`.

### Primeiro arranque

1. `MasterFlywayConfig` corre `db/migration/master`: identidade, perfis
   (SA, Gestor, Funcionario), permissoes e geografia.
2. `AdminBootstrapRunner` cria o super administrador a partir de
   `ADMIN_BOOTSTRAP_*`, se ainda nao existir.
3. Entra com essa conta e cria a empresa "Detailing Studio" em
   `POST /companies/setup`. Isso cria a base de dados do tenant e corre
   `db/migration/tenant`.
4. Remove as variaveis `ADMIN_BOOTSTRAP_*` do ambiente.

## Comandos

| Comando | O que faz |
| --- | --- |
| `./mvnw spring-boot:run` | Arranca em modo dev |
| `./mvnw clean package` | Compila e produz o jar em `target/` |
| `./mvnw test` | Testes |
| `./mvnw -DskipTests clean package` | Build rapido para a imagem Docker |

## Producao (Coolify, VPS Amaterasu)

- API: `https://api.detailingstudio.co.mz`, build pelo `Dockerfile`, porta 8090.
- PostgreSQL 16 gerido pelo Coolify, com backups agendados.
- Variaveis de ambiente definidas no Coolify (ver `.env.example`).

## Estrutura

```
src/main/java/mz/co/amaterasu/gestao/
├── base/        datasources, multi-tenancy, seguranca, Flyway, bootstrap, auth
├── user/        identidade e RBAC (User, Role, Permission) + @MasterDB
├── document/    exportacao PDF (iText) e Excel (POI) a partir de ReportData
├── exceptions/  ApiRequestException e o handler global
└── util/        TenantContext, mapeamento, datas, dinheiro

src/main/resources/
├── db/migration/master/   identidade, perfis e permissoes
├── db/migration/tenant/   dados de negocio de cada empresa
├── templates/mail/
└── application*.properties
```

Os modulos de dominio (clientes, viaturas, catalogo, entradas, pagamentos,
despesas, relatorios) entram como `mz.co.amaterasu.gestao.<modulo>`, seguindo o
`AGENTS.md`.

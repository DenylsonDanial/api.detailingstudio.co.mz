# AGENTS.md — api.detailingstudio.co.mz

Regras para agentes de codigo a trabalhar neste projecto. A especificacao
funcional esta em `../Detailing_Studio_Especificacao_Developer.docx`.
Esta API reaproveita o nucleo da Everlee Grace; as regras abaixo sao as mesmas.

## Antes de comecar

- Package raiz: `mz.co.amaterasu.gestao`. Modulos novos entram como
  `mz.co.amaterasu.gestao.<modulo>` com `entity`, `repository`, `service`,
  `service/impl`, `dto`, `resource`.
- Copia a estrutura de `user/` (User, Role, Permission) como molde. Nao inventes
  uma organizacao diferente.

## Obrigatorio

- **Todo o endpoint protegido leva `@PreAuthorize`**. A permissao segue
  `<accao>:<recurso>` e tem de existir no seed
  `db/migration/master/V2__seed_rbac.sql` **e** no enum `Authorities` do
  frontend (`app.detailingstudio.co.mz`). Um endpoint sem `@PreAuthorize` e um bug de seguranca, nao uma
  omissao de estilo.
- **Schema so por Flyway.** `ddl-auto=none`. Alteracoes ao master vao para
  `db/migration/master/V{n}__descricao.sql`; alteracoes ao tenant vao para
  `db/migration/tenant/V{n}__descricao.sql`. Nunca edites uma migracao ja
  aplicada — cria a seguinte.
- **Nomes de rota conforme a convencao**: `list`, `findById/{id}`, `search`,
  `insert`, `update/{id}`, `deleteById/{id}`.
- **Paginacao com `Pageable`** do Spring Data em todas as listagens.
- **Erros de negocio com `ApiRequestException`** (unchecked). O
  `ApiExceptionHandler` traduz para 400 com mensagem legivel.
- **Tabelas novas do tenant levam o trigger `track_changes()`** na mesma
  migracao, para ficarem auditadas.
- **Segredos so por variavel de ambiente**, com entrada correspondente no
  `.env.example` sem valor real.
- **Toda a exportacao passa por `ReportData`** e pelo `StrategyFactory`. Nao
  escrevas codigo iText ou POI num servico de negocio.

## Proibido

- Acrescentar prefixos a `SYSTEM_BASE_PATHS` em `JwtAuthenticationFilter` para
  dados de negocio. Esses vao ao tenant por omissao, que e o correcto.
- Voltar a por `spring.jpa.hibernate.ddl-auto=update`.
- Interpolar valores vindos do cliente em SQL nativo. O unico caso de DDL
  dinamico (`CREATE DATABASE` no provisionamento) valida o nome com um padrao
  restritivo antes de o usar; mantem essa validacao.
- Devolver a excepcao ou o stack trace no corpo da resposta.
- Guardar refresh tokens ou tokens de reposicao em claro na base de dados: sao
  guardados com hash.
- Confiar no frontend para restringir acessos.
- Criar ficheiros de documentacao novos sem que tenham sido pedidos.

## Multi-tenancy — o que e preciso saber

- Dados de negocio vivem na base de dados do tenant e nao precisam de nenhuma
  anotacao: o encaminhamento e automatico pelo prefixo do URI.
- Se um servico precisa mesmo de ler o master no meio de um pedido de tenant,
  anota o metodo com `@MasterDB`. Usa isto com parcimonia.
- `TenantContext` e um `ThreadLocal`. Se abrires uma thread nova (`@Async`,
  `CompletableFuture`), propaga o valor a mao ou o pedido vai a base de dados
  errada.

## Estilo

- Construtores por `@RequiredArgsConstructor` com campos `final`. Nada de
  `@Autowired` em campos.
- Servicos expostos por interface + `impl`, como no resto do projecto.
- Comentarios so quando explicam uma decisao que o codigo nao mostra.
- Mensagens de erro voltadas ao utilizador em portugues.

## Regras financeiras do Detailing Studio

- **Dinheiro em `BigDecimal` com duas casas** (`Money.of`), `numeric(18,2)` na
  base de dados. Nunca `double`.
- **Nenhum saldo editavel.** O estado de pagamento de uma entrada e o saldo do
  cliente sao calculados a partir das linhas, dos pagamentos e das afectacoes.
- **Pagamento e afectacoes gravam-se na mesma transaccao**, com bloqueio das
  entradas afectadas (`SELECT ... FOR UPDATE`) e nova verificacao do saldo, para
  impedir sobrepagamento por pedidos simultaneos. Usar chave de idempotencia
  para o duplo clique.
- **Movimentos financeiros nao se apagam.** Correccoes sao estornos com motivo e
  ligacao ao original.
- **Cada entrada guarda os valores do momento** (cliente, matricula, nome do
  servico, quantidade, preco, subtotal). Mudar o catalogo nao altera o passado.
- **Data do movimento separada da data de criacao**; datas locais em
  `Africa/Maputo`.

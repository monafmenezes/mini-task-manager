# Mini Task Manager

Sistema de gestão de tarefas em equipe, desenvolvido como desafio técnico para a Pacto Mais.

> Escopo obrigatório e a maioria dos diferenciais estão prontos — ver checklist em [Status](#status) para o que falta e o porquê.

🎥 [Vídeo do sistema funcionando](docs/demo.webm) — login, filtros, criação de tarefa com
sugestão de IA e troca de status, de ponta a ponta.

## Arquitetura (visão geral)

O sistema é dividido em dois serviços independentes, para separar a responsabilidade de autenticação da responsabilidade de gestão de tarefas:

- **auth-service** — cadastro de usuários, login e emissão de JWT.
- **tasks-service** — CRUD de tarefas, times, filtros e paginação. Valida o JWT localmente (sem chamar o auth-service a cada requisição).
- **web** — front-end em React que consome os dois serviços.

### Por que separar auth de tasks

Autenticação e gestão de tarefas têm ciclos de vida e motivos de mudança diferentes: o
`auth-service` muda quando a política de segurança muda; o `tasks-service` muda quando as
regras de negócio do produto mudam. Separar os dois permite deployar/escalar cada um de forma
independente e limita o raio de impacto de uma falha.

O trade-off: mais peças de infraestrutura (2 bancos, 2 pipelines de deploy) e a necessidade de
decidir como os serviços compartilham dado sem virar um "distributed monolith" (serviços
fisicamente separados, mas conversando em tempo real pra tudo, perdendo a vantagem de estarem
separados). Duas decisões tratam disso:

- **Sem chamada síncrona entre os serviços.** O `tasks-service` valida o JWT localmente (mesmo
  segredo/chave pública do `auth-service`), sem bater no `auth-service` a cada requisição.
- **Sem API Gateway (por enquanto).** Com só 2 serviços, um gateway dedicado custaria mais
  tempo de setup/infra do que o benefício de centralizar rotas justificaria. O front guarda duas
  URLs base (uma por serviço). Se o sistema crescesse pra mais serviços, um gateway (ex.: Spring
  Cloud Gateway, ou Azure API Management) seria o próximo passo natural.
- **Responsável da tarefa por denormalização.** O front busca os membros do time no
  `auth-service` e envia `responsavelId` + `responsavelNome` ao criar/editar uma tarefa; o
  `tasks-service` grava esse retrato em vez de consultar o `auth-service` a cada leitura.

## Como rodar localmente

### Opção 1 — Docker Compose (recomendado)

Pré-requisito: Docker.

```bash
docker compose up --build
```

Sobe os 2 bancos Postgres, o Redis, o `auth-service` (porta 8081), o `tasks-service` (porta
8082) e o front (porta 5173, servido por nginx). Acesse **http://localhost:5173**.

As chaves RSA usadas pelo JWT (seção de segurança abaixo) precisam existir antes de subir:

```bash
mkdir -p apps/auth-service/keys apps/tasks-service/keys
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out apps/auth-service/keys/private.pem
openssl rsa -pubout -in apps/auth-service/keys/private.pem -out apps/auth-service/keys/public.pem
cp apps/auth-service/keys/public.pem apps/tasks-service/keys/public.pem
```

Pra sugestão de prioridade via IA funcionar (opcional — o resto do sistema roda sem isso), copie
`.env.example` para `.env` na raiz e preencha `OPENAI_API_KEY` com uma chave válida da OpenAI:

```bash
cp .env.example .env
```

### Opção 2 — rodando cada peça manualmente (útil em desenvolvimento)

1. Suba um Postgres para cada serviço (portas 5432 e 5433) e um Redis (porta 6379) — via Docker
   (`docker run`) ou local.
2. Gere as chaves RSA (comando acima).
3. Abra `apps/auth-service` e `apps/tasks-service` como projetos Maven na sua IDE e rode as
   classes `*Application`. Variáveis de ambiente têm defaults sensatos para desenvolvimento
   local (ver `application.yml` de cada serviço); pra sugestão via IA funcionar, exporte
   `OPENAI_API_KEY` no ambiente do `tasks-service`.
4. No front (`apps/web`), copie `.env.example` para `.env`, rode `pnpm install` (na raiz do
   monorepo) e depois `pnpm dev` dentro de `apps/web`.

## Cache (Redis)

O `tasks-service` cacheia a listagem paginada de tarefas (`GET /tasks`) no Redis, TTL de 5
minutos. Qualquer escrita (criar, editar, mudar status, excluir) invalida o **cache inteiro**
(`allEntries = true`) em vez de tentar invalidar só a entrada afetada. Trade-off consciente: com
poucos filtros possíveis, o custo de recalcular tudo é baixo, e evita o risco de bug sutil de uma
invalidação parcial deixar uma combinação de filtro desatualizada.

## Autenticação

O `auth-service` assina os tokens JWT com **RS256** (chave privada). O `tasks-service` valida
com a chave pública correspondente, sem chamar o `auth-service` — ver seção de arquitetura
acima. Por isso as chaves em `apps/*/keys/` precisam existir antes de qualquer serviço subir
(elas não são versionadas, por serem material sensível).

## Observabilidade

Os dois serviços expõem `spring-boot-starter-actuator`: `/actuator/health` (com `db` e, no
`tasks-service`, `redis` — o Actuator detecta sozinho o que existe no classpath de cada um),
`/actuator/metrics` e `/actuator/prometheus`. `/actuator/**` é público (sem JWT), do mesmo jeito
que qualquer health check de orquestrador de container precisa ser. Log estruturado em JSON no
formato **ECS** (Elastic Common Schema), nativo do Spring Boot desde a série 3.4
(`logging.structured.format.console: ecs`) — sem dependência extra.

## CI

`.github/workflows/ci.yml` roda 3 jobs em paralelo a cada push/PR: `auth-service` e
`tasks-service` (Postgres — e Redis no caso do tasks-service — como `services:` do job, chave
RSA gerada na hora e descartada, `mvn test`) e `web` (`pnpm install --frozen-lockfile`, lint,
build).

## Deploy (Azure) — como eu faria

Não implementei o deploy de verdade neste desafio (prazo), mas aqui está a decisão de
infraestrutura que eu tomaria, para deixar claro que não é uma lacuna por falta de saber o quê
fazer.

**Serviço de compute: Azure Container Apps, não AKS.** Pro tamanho desse sistema (2 APIs +
front), um cluster Kubernetes seria overhead operacional sem benefício correspondente — alguém
precisaria administrar o control plane, node pools, upgrades. Container Apps dá o essencial que
esse projeto precisa (scale-to-zero, revisões, ingress HTTPS gerenciado) sem esse custo, e é o
free tier que eu tenho disponível no Azure.

**Recursos Terraform, em `infra/terraform/` (hoje vazio, propositalmente):**

| Recurso | Papel |
|---|---|
| `azurerm_resource_group` | agrupa tudo, um por ambiente |
| `azurerm_container_registry` | guarda as imagens Docker que o CI builda |
| `azurerm_container_app_environment` | ambiente compartilhado pelos 2 serviços |
| `azurerm_container_app` (×2) | auth-service e tasks-service, cada um com sua config de scaling/revisão |
| `azurerm_static_web_app` | front — arquivos estáticos do `vite build`, mais barato e mais simples que rodar nginx num Container App só pra servir HTML/JS/CSS |
| `azurerm_postgresql_flexible_server` (×2 databases) | um server, dois bancos — não precisa de duas instâncias pra esse tamanho |
| `azurerm_redis_cache` | cache do tasks-service |
| `azurerm_key_vault` | chave privada RSA, credenciais de banco, `OPENAI_API_KEY` |

**Segredos nunca em variável de ambiente plana.** As chaves RSA e credenciais iriam pro Key
Vault; os `azurerm_container_app` referenciam o segredo via `secret { key_vault_secret_id }`, não
como valor literal no `.tf` nem no state — o mesmo princípio que já vale localmente (chaves
gitignored, montadas como volume `:ro`), só que a versão gerenciada em nuvem.

**Pipeline:** um job novo no `ci.yml` existente, disparado só em push pra `main`, com
`needs: [auth-service, tasks-service, web]` — só builda/deploya imagem depois que os testes de
todos os serviços passarem. `azure/login` com um Service Principal via OIDC (sem secret de longa
duração no GitHub), depois `terraform apply` (ou `az containerapp update` direto, mais simples se
o Terraform ficar só pra provisionar a infra base uma vez).

**O que eu faria diferente num sistema de produção real** (fora do escopo de um desafio técnico):
múltiplas réplicas com health check de `readiness` (já preparado — ver seção Observabilidade)
plugado no load balancer do Container Apps, backup automático do Postgres, e Azure Application
Insights consumindo o `/actuator/prometheus` que já existe, em vez de só expor a métrica sem
alguém coletando.

## Estrutura do repositório

```
apps/
  auth-service/   # Java 17 + Spring Boot
  tasks-service/  # Java 17 + Spring Boot
  web/            # React + Vite + shadcn/ui
infra/
  terraform/      # vazio de propósito — ver seção "Deploy (Azure)" acima
docs/
  demo.webm       # vídeo do sistema funcionando de ponta a ponta
```

## Status

- [x] auth-service
- [x] tasks-service
- [x] testes automatizados (auth-service e tasks-service)
- [x] front-end
- [x] Docker Compose
- [x] cache (Redis)
- [x] sugestão de prioridade via IA
- [x] observabilidade (Actuator + log estruturado)
- [x] CI (GitHub Actions)
- [ ] Terraform + deploy — documentado, não implementado (ver seção "Deploy (Azure)" acima)

# Mini Task Manager

Sistema de gestão de tarefas em equipe, desenvolvido como desafio técnico para a Pacto Mais.

> Projeto em construção. Este README vai crescendo junto com cada etapa; a versão final vai reunir instruções de execução completas e as decisões de arquitetura.

## Arquitetura (visão geral)

O sistema é dividido em dois serviços independentes, para separar a responsabilidade de autenticação da responsabilidade de gestão de tarefas:

- **auth-service** — cadastro de usuários, login e emissão de JWT.
- **tasks-service** — CRUD de tarefas, times, filtros e paginação. Valida o JWT localmente (sem chamar o auth-service a cada requisição).
- **web** — front-end em React que consome os dois serviços.

O motivo dessa separação e os trade-offs envolvidos estão detalhados mais abaixo (seção em construção).

## Estrutura do repositório

```
apps/
  auth-service/   # Java 17 + Spring Boot
  tasks-service/  # Java 17 + Spring Boot
  web/            # React + Vite + shadcn/ui
infra/
  terraform/      # Infraestrutura como código (Azure)
docs/
```

## Status

- [ ] auth-service
- [ ] tasks-service
- [ ] cache (Redis)
- [ ] sugestão de prioridade via IA
- [ ] testes automatizados
- [ ] front-end
- [ ] Docker Compose
- [ ] observabilidade
- [ ] CI
- [ ] Terraform + deploy

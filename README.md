# PetVitaliz API

API REST da PetVitaliz, uma clínica veterinária com planos de assinatura. Ela atende o site em Angular e o app mobile em Expo, e foi migrada de Express + Prisma para Spring Boot mantendo as mesmas URLs, cookies e formatos de JSON.

## Tecnologias

- Java 21 e Spring Boot 3.5
- Spring Web, Spring Data JPA (Hibernate) e Spring Security
- MySQL (Aiven)
- JWT (jjwt) em cookie HttpOnly ou no cabeçalho `Authorization`
- Cloudinary para imagens e Gmail SMTP para e-mails
- springdoc-openapi (Swagger)
- JUnit 5, Mockito e H2 nos testes
- Docker para o deploy na Render

## Estrutura

```
src/main/java/com/petvitaliz
├── config        segurança, CORS, Swagger, relógio e propriedades
├── controller    rotas HTTP
├── dto           objetos de entrada e saída (JSON)
├── entity        entidades JPA e conversores de enum
├── enums         enums do domínio
├── exception     erros da API e tratamento global
├── repository    repositórios Spring Data
├── security      JWT, cookies e filtro de autenticação
├── service       regras de negócio
├── util          datas, horários e textos
└── web           rate limit e idempotência
```

## Como rodar localmente

Pré-requisitos: JDK 21, Maven (ou o do IntelliJ) e um banco MySQL com o schema do projeto.

1. Copie `.env.example` para `.env` e preencha as variáveis (tabela abaixo).
2. Rode a classe `PetvitalizApplication` ou `mvn spring-boot:run`.
3. Teste em `http://localhost:3000/health`. A resposta esperada é `{"API":"Rodando","DB":"ON"}`.
4. A documentação fica em `http://localhost:3000/docs`.

O `.env` é lido automaticamente e não vai para o Git.

## Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DB_URL` | sim | `jdbc:mysql://HOST:PORTA/BANCO?sslMode=REQUIRED` |
| `DB_USER` / `DB_PASSWORD` | sim | Usuário e senha do MySQL |
| `JWT_SECRET` | sim | Chave dos tokens, com 32 caracteres ou mais |
| `EMAIL_USER` / `EMAIL_PASS` | sim | Conta Gmail e senha de app |
| `CLOUDINARY_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | para upload | Credenciais do Cloudinary |
| `FRONTEND_URL` | em produção | Origens liberadas no CORS, separadas por vírgula |
| `COOKIE_SECURE` | não | `true` em produção, `false` em `localhost` (padrão `true`) |
| `COOKIE_SAMESITE` | não | `None` em produção, `Lax` em `localhost` (padrão `None`) |
| `PORT` | não | Porta HTTP (padrão `3000`) |
| `APP_TIMEZONE` | não | Fuso das regras de horário (padrão `America/Sao_Paulo`) |
| `RATE_LIMIT_ENABLED` | não | Liga ou desliga o rate limit (padrão `true`) |
| `RATE_LIMIT_GENERAL` | não | Requisições por janela, no geral (padrão `300`) |
| `RATE_LIMIT_AUTH` | não | Requisições por janela no login e cadastro (padrão `20`) |
| `RATE_LIMIT_WINDOW` | não | Tamanho da janela (padrão `1m`) |
| `RATE_LIMIT_TRUSTED_PROXIES` | não | Proxies à frente da API; use `1` na Render (padrão `0`) |
| `IDEMPOTENCY_ENABLED` | não | Liga ou desliga a idempotência (padrão `true`) |
| `IDEMPOTENCY_TTL` | não | Tempo que uma resposta fica guardada (padrão `24h`) |

## Autenticação

Existem quatro tipos de sessão, cada uma com seu cookie:

| Perfil | Login | Cookie | Duração |
|---|---|---|---|
| Usuário | `POST /user/login` | `token` | 3 dias |
| Administrador | `POST /user/login/adm` | `token_adm` | 3 dias |
| Funcionário | `POST /user/login/funcionario` | `token_funcionario` | 1 hora |
| Recuperação de senha | `POST /user/login/esqueci-a-senha-confirmar` | `token_reset` | 9 minutos |

- **Site:** o navegador guarda o cookie sozinho, desde que as requisições usem `withCredentials: true`.
- **App mobile:** envie `X-Client-Type: mobile` no login para receber o `token` no corpo da resposta. Depois mande `Authorization: Bearer <token>` nas próximas requisições.

Erros seguem o formato `{"mensagem": "..."}`. Uma requisição sem login devolve `401`; um token expirado, inválido ou de outro perfil devolve `403`.

## Rotas

### Públicas
| Método | Rota | Descrição |
|---|---|---|
| GET | `/health` | Estado da API e do banco |
| GET | `/` | Home com os planos |
| GET | `/servicos`, `/contato`, `/servicos-de-emergencia` | Páginas informativas |
| GET | `/adm/listar/produtos` | Lista de planos |
| POST | `/user/cadastro` | Cadastro de usuário |
| POST | `/user/login/esqueci-a-senha` | Envia o código de recuperação por e-mail |
| PUT | `/user/login/alterar-senha` | Nova senha (exige `token_reset`) |

### Usuário
| Método | Rota | Descrição |
|---|---|---|
| GET | `/user/home`, `/user/servicos`, `/user/servicos-de-emergencia` | Páginas logadas |
| GET | `/user/buscar/perfil` | Dados do perfil |
| POST | `/user/contato` | Envia mensagem de contato |
| GET/POST | `/user/logout` | Encerra a sessão |
| GET | `/user/listar/pet` | Lista os pets |
| POST | `/user/listar/pet/cadastar` | Cadastra pet (JSON ou multipart com `image`) |
| PUT | `/user/listar/pet/editar/{id}` | Edita pet |
| DELETE | `/user/listar/pet/delete/{id}` | Exclui pet |
| GET | `/user/planos` | Plano atual |
| POST | `/user/planos/pagamento` | Assina um plano |
| DELETE | `/user/planos/cancelar` | Cancela o plano |
| POST | `/agendamento` | Agenda consulta |
| GET | `/consultas` | Lista as consultas |
| DELETE | `/user/agendamento/cancelar/{id}` | Cancela consulta |

### Funcionário
| Método | Rota | Descrição |
|---|---|---|
| GET | `/funcionario` | Resumo e consultas do dia |
| GET | `/funcionario/consultas` | Todas as consultas do funcionário |
| GET | `/funcionario/consultas/{id}` | Detalhes de uma consulta |
| PUT | `/funcionario/consultas/atualizar/{id}` | Muda o status (`em_andamento` ou `finalizado`) |
| POST | `/funcionario/bater-ponto` | Restaura a carga diária (8 consultas) |
| GET | `/funcionario/perfil-dados` | Dados do perfil |
| PUT | `/funcionario/perfil-dados/atualizar` | Atualiza o perfil |
| GET | `/funcionario/clientes` | Clientes e seus pets |
| GET | `/funcionario/pets` | Pacientes |
| GET/POST | `/funcionario/logout` | Encerra a sessão |

### Administrador
| Método | Rota | Descrição |
|---|---|---|
| GET | `/adm` | Home do administrador |
| GET | `/adm/consultas` | Consultas do dia |
| PUT | `/adm/consultas/atualizar/{id}` | Muda o status de uma consulta |
| DELETE | `/adm/consultas/cancelar/{id}` | Cancela uma consulta |
| POST | `/adm/agendamento` | Agenda consulta para um cliente |
| GET | `/adm/listar/clientes` | Clientes e pets |
| DELETE | `/adm/listar/clientes/excluir/{petId}` | Exclui um pet |
| POST | `/adm/upload` | Envia imagem ao Cloudinary |
| GET, POST, PUT, DELETE | `/adm/listar/adm[/cadastrar, /{id}, /editar/{id}, /excluir/{id}]` | CRUD de administradores |
| GET, POST, PUT, DELETE | `/adm/listar/funcionario[/cadastrar, /{id}, /editar/{id}, /excluir/{id}]` | CRUD de funcionários |
| GET, POST, PUT, DELETE | `/adm/listar/produtos[/cadastrar, /{id}, /editar/{id}, /excluir/{id}]` | CRUD de planos |
| DELETE | `/adm/listar/produtos/assinaturas/cancelar` | Cancela todos os planos contratados (libera a exclusão de planos) |
| GET/POST | `/adm/logout` | Encerra a sessão |

Exemplos prontos para o HTTP Client do IntelliJ estão em `requests/user.http`, `requests/employee.http` e `requests/admin.http`.

## Regras de agendamento

- Serviços disponíveis: `veterinario` e `tosador`.
- Cada consulta dura 1 hora, e não é possível agendar em horário que já passou (fuso `America/Sao_Paulo`).
- O pet precisa pertencer ao usuário e não pode ter outra consulta no mesmo horário.
- O profissional é sorteado entre os livres naquele horário. Cada um atende até 8 consultas por dia.
- Uma consulta só pode ser iniciada a partir do horário marcado e finalizada até 10 minutos depois do fim.
- Consultas finalizadas não podem ser canceladas. Ao cancelar, a carga do profissional é devolvida.

## Rate limit

O limite é contado por IP. Por padrão são 300 requisições por minuto no geral e 20 por minuto nas rotas de login e cadastro.

- Ao passar do limite, a API responde `429` com o cabeçalho `Retry-After`.
- Toda resposta traz `X-RateLimit-Limit` e `X-RateLimit-Remaining`.

## Idempotência

Em `POST`, `PUT` e `DELETE` o cliente pode enviar o cabeçalho `Idempotency-Key`, com até 128 caracteres e um valor novo por operação. Se a mesma chave chegar de novo, a API devolve a resposta guardada, com `Idempotency-Replayed: true`, sem executar a operação outra vez.

- A mesma chave com outro corpo devolve `422`.
- A mesma chave enquanto a primeira requisição ainda processa devolve `409`.
- Só respostas de sucesso (2xx) são guardadas, por 24 horas.
- As rotas de login não usam idempotência.

O app mobile já gera a chave sozinho. Os contadores do rate limit e as chaves ficam em memória, então valem para uma instância e zeram ao reiniciar.

## Testes

```
mvn test
```

Os testes unitários cobrem regras de agendamento, JWT, upload, mapeamento de JSON, rate limit e idempotência. Um teste de integração sobe a API inteira com um banco H2 em memória e percorre os fluxos de usuário, funcionário e administrador. Nenhum teste usa o banco configurado no `.env`.

## Deploy na Render (com Aiven)

1. Suba o projeto para o GitHub.
2. Na Render, crie um **Web Service** com o runtime **Docker**. Ele usa o `Dockerfile` da raiz.
3. Em **Health Check Path**, coloque `/health`.
4. Cadastre as variáveis de ambiente. Em produção use `COOKIE_SECURE=true`, `COOKIE_SAMESITE=None` e `RATE_LIMIT_TRUSTED_PROXIES=1`.
5. Coloque em `FRONTEND_URL` a URL do site na Vercel.
6. No Aiven, deixe o acesso liberado para `0.0.0.0/0`, porque a Render não tem IP fixo no plano gratuito.
7. Se o banco for compartilhado com outro serviço, limite as conexões com `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=5`.

A API não altera a estrutura do banco (`ddl-auto: none`), então o schema precisa existir antes do deploy.

## Integração com o site e o app

- **Site (Angular):** ajuste `apiUrl` em `src/app/environments/environment.prod.ts` para a URL da Render.
- **App (Expo):** defina `EXPO_PUBLIC_API_URL` no `.env` do app. Em desenvolvimento ele usa sozinho o IP do computador que roda o Expo, na porta 3000.

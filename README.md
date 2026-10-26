# petvitaliz-spring

Migracao da API `petvitaliz-service` (Express + Prisma) para Spring Boot 3.5 / Java 21 / Maven / MySQL.

## Rodar
1. JDK 21 + MySQL com o banco do projeto antigo.
2. Copie `.env.example` para `.env` e preencha (o `JWT_SECRET` precisa ter 32+ caracteres).
   Localmente deixe `COOKIE_SECURE=false` e `COOKIE_SAMESITE=Lax`.
3. Execute `PetvitalizApplication` e abra `http://localhost:3000/health`.

## Etapas
- [x] 1. Projeto, entidades, repositories, /health
- [x] 2. Seguranca (JWT em cookie, login, reset de senha, CORS)
- [x] 3a. Usuario: home, servicos, contato, perfil, planos
- [x] 3b. Pets: listar, cadastrar, editar, excluir (upload Cloudinary)
- [x] 4. Agendamento e consultas do usuario
- [ ] 5. Funcionario e administrador
- [ ] 6. springdoc, Dockerfile

## Testar
Abra `requests/auth.http` no IntelliJ (HTTP Client) e rode as requisicoes de cima para baixo.

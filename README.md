# petvitaliz-spring

Migracao da API `petvitaliz-service` (Express + Prisma) para Spring Boot 3.5 / Java 21 / Maven / MySQL.

## Rodar
1. JDK 21 + MySQL com o banco do projeto antigo.
2. Em `Run > Edit Configurations > Environment variables` (IntelliJ), defina `DB_URL`, `DB_USER`, `DB_PASSWORD`
   (veja `.env.example`).
3. Execute `PetvitalizApplication` e abra `http://localhost:3000/health`.

## Etapas
- [x] 1. Projeto, entidades, repositories, /health
- [ ] 2. Seguranca (JWT em cookie, login, reset de senha)
- [ ] 3. Usuario e pets (upload Cloudinary)
- [ ] 4. Agendamento e consultas
- [ ] 5. Funcionario e administrador
- [ ] 6. E-mail, springdoc, Dockerfile

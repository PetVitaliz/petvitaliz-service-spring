# petvitaliz-spring

API REST da PetVitaliz em Spring Boot 3.5 / Java 21 / Maven / MySQL, migrada de Express + Prisma.
As URLs, os cookies e o formato dos JSON sao os mesmos da API antiga.

## Rodar localmente
1. JDK 21 e um banco MySQL com o schema do projeto.
2. Copie `.env.example` para `.env` e preencha. O `JWT_SECRET` precisa ter 32+ caracteres.
3. Execute `PetvitalizApplication` e abra `http://localhost:3000/health`.
4. Documentacao (Swagger): `http://localhost:3000/docs`.

## Testes
`mvn test` roda os testes unitarios e um teste de integracao que sobe a API inteira com um banco H2 em memoria.
Nenhum teste usa o banco configurado no `.env`.

## Rate limit
Limite por IP em janelas de 1 minuto: 300 requisicoes no geral e 20 nas rotas de login e cadastro.
Ao passar do limite a API responde `429` com o cabecalho `Retry-After`.
Os cabecalhos `X-RateLimit-Limit` e `X-RateLimit-Remaining` vao em todas as respostas.
Ajuste com `RATE_LIMIT_GENERAL`, `RATE_LIMIT_AUTH` e `RATE_LIMIT_WINDOW`.

## Idempotencia
Em `POST`, `PUT` e `DELETE` o cliente pode enviar o cabecalho `Idempotency-Key` (ate 128 caracteres, um valor novo por operacao).
Se a mesma chave chegar de novo, a API devolve a resposta guardada, com `Idempotency-Replayed: true`, sem executar a operacao outra vez.
- mesma chave com outro corpo: `422`
- mesma chave enquanto a primeira ainda processa: `409`
- so respostas 2xx sao guardadas (24h por padrao, `IDEMPOTENCY_TTL`)
- as rotas de login nao usam idempotencia

Os contadores de rate limit e as chaves de idempotencia ficam em memoria, entao valem para uma instancia e zeram ao reiniciar.

## Deploy na Render (Docker)
1. Crie um Web Service apontando para o repositorio e escolha o runtime Docker.
2. Cadastre as variaveis do `.env.example` no painel. Em producao use `COOKIE_SECURE=true` e `COOKIE_SAMESITE=None`.
3. `FRONTEND_URL` aceita varias origens separadas por virgula (ex.: a URL da Vercel).
4. Use `RATE_LIMIT_TRUSTED_PROXIES=1` para o limite considerar o IP do cliente e nao o do proxy da Render.
5. Health check: `/health`.

## Requisicoes de exemplo
A pasta `requests/` tem arquivos `.http` para o HTTP Client do IntelliJ.

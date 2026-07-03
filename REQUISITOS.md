# Sistema de Questionários Online — Checklist de Requisitos

> Documento de referência para acompanhar o cumprimento dos requisitos do trabalho.
> Legenda: ✅ OK · ⚠️ Parcial / precisa ajuste · ❌ Faltando / quebrado

---

## 3. Requisitos Funcionais

| ID | Requisito | Status | Observação |
|----|-----------|--------|------------|
| RF01 | Cadastro de Professores (nome, email, senha) | ✅ | `Professor` + `ProfessorController`. Senha é criptografada com BCrypt. |
| RF02 | Cadastro de Alunos (nome, email, matrícula) | ✅ | `Aluno` + `AlunoController`. |
| RF03 | Autenticação do Professor (login email/senha, JWT) | ✅ | `AuthController.login` valida email+senha (BCrypt) contra o banco e emite JWT. `JwtAuthenticationFilter` valida o token em cada requisição. |
| RF04 | Cadastro de Questionários (título, descrição, validade token, perguntas) | ⚠️ | Entidade completa, mas o endpoint exige autenticação e é inalcançável (ver RF03/RNF04). O `professor` dono nunca é associado ao questionário criado. |
| RF05 | Cadastro de Perguntas (enunciado, **tipo**, alternativas, correta, pontuação) | ✅ | Campo `tipo` (enum `TipoPergunta`) adicionado. `POST /questionarios/{id}/perguntas` persiste de fato (com alternativas) via `PerguntaRepository`. |
| RF06 | Cadastro de Alternativas (texto, correta) | ✅ | `Alternativa` OK. |
| RF07 | Associação de Questionário ao Aluno | ✅ | `POST /questionarios/{idQ}/enviar/{idAluno}`. |
| RF08 | Geração de Token (id aluno, id questionário, expiração) | ✅ | `TokenAcesso` com UUID único. |
| RF09 | Envio de E-mail (link, token, validade) | ⚠️ | `EmailService` implementado; falha de SMTP agora é **não-fatal** (loga o link e não derruba a geração do token). Para envio real, configurar SMTP em `application.properties`. O endpoint de envio retorna o token gerado (útil sem SMTP). |
| RF10 | Validação do Token (existência, expiração, integridade, aluno correto) | ⚠️ | Valida existência, expiração e uso único. **Não valida associação ao aluno correto** (endpoint público só com token). |
| RF11 | Resposta do Questionário enquanto token válido | ✅ | `POST /responder/{token}`. |
| RF12 | Correção Automática (nota, % acertos) | ⚠️ | Funciona, mas o percentual usa só as alternativas enviadas como denominador (não o total de perguntas); nota não tem limite. |
| RF13 | Exibição do Resultado (nota, acertos, erros, %) | ✅ | `ResultadoDTO`. |
| RF14 | Visualização das Respostas | ✅ | Entidade `RespostaAluno` persistida na correção; `GET /respostas/{token}` retorna as respostas reais (pergunta, alternativa, correta). |
| RF15 | Estatísticas do Questionário | ✅ | `GET /questionarios/{id}/estatisticas` calcula acertos/erros geral e **por pergunta** a partir das respostas persistidas. |
| RF16 | Documentação Swagger / OpenAPI 3 | ✅ | `springdoc-openapi` configurado. |
| RF17 | Suporte a HATEOAS | ✅ | `Questionario`: `self`, `perguntas`, `estatisticas`. Resultado da correção: `respostas`, `estatisticas`. `Aluno` e `Professor`: `self` (com GET por id). Recursão JSON bidirecional corrigida com `@JsonIgnore`. |

## 4. Requisitos Não Funcionais

| ID | Requisito | Status | Observação |
|----|-----------|--------|------------|
| RNF01 | Java 17+ e Spring Boot | ✅ | Java 21, Spring Boot 3.3.0. |
| RNF02 | Banco relacional (PostgreSQL/MySQL) | ✅ | Migrado para **MySQL** (`mysql-connector-j`), schema `questionarios_db` criado automaticamente. Validado em runtime: 7 tabelas criadas e dados persistidos. H2 mantido apenas para testes. Credenciais em `application.properties` (override por `DB_USER`/`DB_PASSWORD`/`DB_URL`). |
| RNF03 | API REST em JSON | ✅ | |
| RNF04 | Endpoints protegidos por JWT | ✅ | `JwtAuthenticationFilter` (`OncePerRequestFilter`) valida o `Authorization: Bearer <token>` e popula o `SecurityContext`. Endpoints protegidos acessíveis com token válido. |
| RNF05 | Expiração do token configurável em minutos | ✅ | `tempoValidadeTokenMinutos`. |
| RNF06 | Documentação Swagger/OpenAPI | ✅ | |
| RNF07 | Testes via Postman | ➖ | Externo ao código. |
| RNF08 | Arquitetura em camadas | ⚠️ | Controller/Service/Repository/DTO/Entity presentes, mas Aluno/Professor não têm camada Service (controller acessa repositório direto). |

## 5. Regras de Negócio

| ID | Regra | Status | Observação |
|----|-------|--------|------------|
| RN01 | Só professores autenticados criam questionários | ✅ | Endpoint exige JWT válido (`anyRequest().authenticated()` + filtro JWT). |
| RN02 | Só alunos cadastrados recebem questionários | ✅ | `findById(...).orElseThrow()`. |
| RN03 | Aluno não responde após expiração | ✅ | Validado. |
| RN04 | Questionário respondido só uma vez | ✅ | Flag `utilizado`. |
| RN05 | Cada pergunta com ≥1 alternativa correta | ✅ | Validado em `validarERelacionarPergunta` (na criação do questionário e ao adicionar pergunta); viola → `400 Bad Request`. |
| RN06 | Nota calculada automaticamente | ✅ | |

---

## Prioridades de correção (resumo)

1. ~~**Autenticação JWT (RF03/RNF04/RN01)**~~ — ✅ FEITO: login valida email+senha, `JwtAuthenticationFilter` valida o token.
2. ~~**Persistir respostas + `RespostaAluno` (RF14)**~~ — ✅ FEITO: entidade/repositório criados e salvos na correção.
3. ~~**Estatísticas reais (RF15)**~~ — ✅ FEITO: cálculo geral e por pergunta a partir das respostas.
4. ~~**Salvar perguntas de fato (RF05)** e adicionar campo **tipo**~~ — ✅ FEITO.
5. ~~**Validar RN05**~~ — ✅ FEITO (400 Bad Request quando não há alternativa correta).
6. ~~**HATEOAS (RF17)**~~ — ✅ FEITO (links em questionário, resultado, aluno, professor).
7. ~~**Banco (RNF02)**~~ — ✅ FEITO: migrado para MySQL (usuário `root`, schema criado automaticamente), validado em runtime. Senha fica **só via variável de ambiente `DB_PASSWORD`** (padrão no arquivo é o placeholder `changeme`).
8. ~~**Frontend (ponto extra)**~~ — ✅ FEITO: interface web (professor + aluno) servida como estático pelo próprio Spring Boot.
9. **E-mail real (RF09)** — configurar SMTP em `application.properties` (opcional; hoje loga o link).

## Frontend (ponto extra)

Interface web em HTML/CSS/JavaScript puro, servida pelo backend em `src/main/resources/static`
(mesma origem, sem CORS). Acesse em **http://localhost:8080** com a aplicação rodando.

- **Painel do professor (`index.html`):** login/cadastro (JWT salvo no navegador), criação de
  questionário com construtor dinâmico de perguntas/alternativas, cadastro e listagem de alunos,
  envio (gera link com botão copiar) e estatísticas com barras por pergunta.
- **Página do aluno (`responder.html?token=...`):** carrega o questionário pelo token (sem revelar a
  resposta correta), responde (radio/checkbox conforme o tipo), vê a nota e o detalhe das respostas.
- **Apoio no backend:** endpoint público `GET /api/responder/{token}` (perguntas sem gabarito),
  `GET /api/questionarios` e `GET /api/alunos` (listagens), e liberação dos estáticos no SecurityConfig.

### Correções colaterais aplicadas (bugs pré-existentes)
- `application.properties` com encoding corrompido quebrava o `mvn compile` → reescrito em ASCII.
- `pom.xml` sem `spring-boot-starter-test` → o teste gerado não compilava → dependência adicionada.
- Recursão infinita na serialização JSON de `Questionario/Pergunta/Alternativa` → `@JsonIgnore` nas back-references.
- Back-references (`pergunta.questionario`, `alternativa.pergunta`) não eram setadas → FKs iam nulos no cascade → corrigido.
- Hash da senha do professor era retornado nas respostas → ocultado (cópia sem senha).
- Divisão por zero no percentual quando não há respostas → tratado.
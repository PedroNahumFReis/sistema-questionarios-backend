# Sistema de Questionários Online — Backend

API REST em **Spring Boot** para professores criarem questionários e disponibilizarem a alunos via
link com token temporário enviado por e-mail. Inclui autenticação JWT, correção automática,
estatísticas, documentação Swagger e navegação HATEOAS.

> Projeto da disciplina de Backend. A verificação de cada requisito (RF/RN/RNF) está em [`REQUISITOS.md`](REQUISITOS.md).

---

## Tecnologias

- Java 21 · Spring Boot 3.3
- Spring Web · Spring Data JPA · Spring Security (JWT) · Spring HATEOAS · Spring Mail
- MySQL (produção) · H2 (apenas testes)
- Swagger UI / OpenAPI 3 (springdoc)
- Maven (com wrapper `mvnw`)

---

## Pré-requisitos

1. **JDK 21** instalado (`java -version` deve mostrar 21).
2. **MySQL** rodando localmente na porta padrão **3306**.
   - Não é preciso criar o banco à mão: a aplicação cria o schema `questionarios_db` sozinha
     (`createDatabaseIfNotExist=true`).
3. Não precisa instalar o Maven — o projeto usa o wrapper (`./mvnw` no Git Bash ou `mvnw.cmd` no PowerShell).

---

## Configuração do banco

As credenciais ficam em `src/main/resources/application.properties`, mas o recomendado é
**não editar o arquivo** e passar a senha por variável de ambiente `DB_PASSWORD`.

Variáveis disponíveis (com os padrões entre parênteses):

| Variável      | Padrão                                                        | O que é              |
|---------------|--------------------------------------------------------------|----------------------|
| `DB_URL`      | `jdbc:mysql://localhost:3306/questionarios_db?...`           | URL de conexão       |
| `DB_USER`     | `root`                                                        | Usuário do MySQL     |
| `DB_PASSWORD` | `changeme` (placeholder — **defina a sua**)                  | Senha do MySQL       |

---

## Como rodar

No **PowerShell** (Windows), a partir da pasta do projeto:

```powershell
$env:DB_PASSWORD = "SUA_SENHA_MYSQL"
.\mvnw.cmd spring-boot:run
```

Ou no **Git Bash / Linux / macOS**:

```bash
DB_PASSWORD="SUA_SENHA_MYSQL" ./mvnw spring-boot:run
```

Para não repetir a senha toda vez, defina-a de forma permanente (abra um **novo** terminal depois):

```powershell
setx DB_PASSWORD "SUA_SENHA_MYSQL"
```

A aplicação sobe em **http://localhost:8080**. Quando ver no console
`Started BackEndTrab2Application`, está pronta.

> Se aparecer `Access denied for user 'root'`, a senha em `DB_PASSWORD` está errada.

---

## Frontend (interface web — ponto extra)

Com a aplicação no ar, abra no navegador:

- **Painel do professor:** http://localhost:8080/ (ou `/index.html`)
  Faça login/cadastro, crie questionários (com construtor de perguntas), cadastre alunos,
  envie o questionário (gera um link para copiar) e veja as estatísticas.
- **Página do aluno:** aberta pelo link gerado no envio — `http://localhost:8080/responder.html?token=SEU_TOKEN`
  O aluno responde e vê a nota e o detalhe das respostas.

A interface é servida pelo próprio Spring Boot (arquivos em `src/main/resources/static`), então
não há nada extra para instalar ou subir.

## Documentação da API (Swagger)

Com a aplicação no ar, acesse:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI (JSON):** http://localhost:8080/api-docs

---

## Rotas de acesso (segurança)

- **Públicas (sem token):** `POST /api/auth/login`, `POST /api/professores` (cadastro),
  `POST /api/responder/{token}`, `GET /api/respostas/{token}`, Swagger.
- **Protegidas (exigem JWT):** todas as demais — ex.: criar questionário, cadastrar aluno,
  enviar questionário, ver estatísticas.

Para chamar uma rota protegida, envie o cabeçalho:

```
Authorization: Bearer <token-recebido-no-login>
```

---

## Fluxo de uso (passo a passo)

Sequência típica para testar no Postman ou Swagger:

1. **Cadastrar professor** — `POST /api/professores`
   ```json
   { "nome": "Ana", "email": "ana@x.com", "senha": "123" }
   ```
2. **Login** — `POST /api/auth/login` → copie o `token` da resposta.
   ```json
   { "email": "ana@x.com", "senha": "123" }
   ```
3. A partir daqui, use o header `Authorization: Bearer <token>` nas rotas protegidas.
4. **Criar questionário (com perguntas)** — `POST /api/questionarios`
   ```json
   {
     "titulo": "Java Básico",
     "descricao": "Avaliação inicial",
     "tempoValidadeTokenMinutos": 60,
     "perguntas": [
       {
         "enunciado": "Quanto é 2 + 2?",
         "tipo": "UNICA_ESCOLHA",
         "pontuacao": 5.0,
         "alternativas": [
           { "texto": "3", "correta": false },
           { "texto": "4", "correta": true }
         ]
       }
     ]
   }
   ```
   > Regra RN05: cada pergunta precisa de ao menos uma alternativa `correta: true`, senão retorna **400**.
5. **Cadastrar aluno** — `POST /api/alunos`
   ```json
   { "nome": "João", "email": "joao@x.com", "matricula": "2024001" }
   ```
6. **Enviar questionário ao aluno** — `POST /api/questionarios/{idQuestionario}/enviar/{idAluno}`
   - Gera o token de acesso e "envia" o e-mail. A resposta traz o `token` (útil porque o SMTP é fictício).
7. **Aluno responde** — `POST /api/responder/{token}` (público)
   ```json
   { "idsAlternativasSelecionadas": [2] }
   ```
   - Retorna nota, acertos, erros, percentual e links para respostas e estatísticas.
8. **Ver respostas do aluno** — `GET /api/respostas/{token}` (RF14)
9. **Ver estatísticas** — `GET /api/questionarios/{idQuestionario}/estatisticas` (RF15, protegida)

---

## Testes

Os testes usam **H2 em memória**, então rodam sem MySQL ligado:

```powershell
.\mvnw.cmd test
```

---

## Observações

- **E-mail (RF09):** por padrão o SMTP é fictício (`localhost:2525`); o envio real falha de forma
  silenciosa e o link é registrado no log. Para envio real, ajuste as propriedades `spring.mail.*`
  em `application.properties` com um SMTP verdadeiro (ex.: Gmail com senha de app).
- **Segurança da senha do banco:** o valor padrão `changeme` é só um placeholder; use `DB_PASSWORD`
  para não versionar a senha real.

# Secure To-Do API
API de Gerenciamento de Tarefas com foco em Segurança Ofensiva e Defensiva.
---

Este projeto evoluiu de um desafio técnico para um ambiente de treinamento. Ele simula vulnerabilidades críticas do OWASP API Security Top 10 (2023) e demonstra mitigações práticas em um ambiente Spring Boot de alta performance.

---
### Tecnologias e Hardening

- **Java 21 & Spring Boot 3.5.8**
- **Security:** Spring Security, JWT (JSON Web Tokens), BCrypt, RBAC(Role-Based Access Control)
- **Infra:** Docker (User non-root), PostgreSQL 15
- **Auditoria:** Hibernate Envers (Versionamento histórico de entidades)
- **Upload:** Apache Tika 3.2.2 (Análise de Magic Bytes e Detecção de Mime-Type)
- **Rate Limiting:** Bucket4j (Proteção contra Brute-Forcing/DDoS nível de aplicação)
- **Documentação:** OpenAPI 3 (Swagger UI)
---

### OWASP API Security Top 10: Implementações Práticas

Abaixo estão as vulnerabilidades simuladas e as defesas implementadas no projeto:

| Risco  | Nome                 | Implementação / Defesa                                                       |
|--------|----------------------|------------------------------------------------------------------------------|
| API01  | BOLA                 | Validação de propriedade (IDOR) em nível de serviço para cada UUID.          |
| API02  | Broken Auth          | Reset de senha com bloqueio de e-mail após 5 tentativas (Lockout).           |
| API03  | Object Property      | Uso estrito de DTO Records para impedir Mass Assignment.                     |
| API04  | Resource Consumption | Paginação obrigatória em listagens e limite de tamanho em uploads.           |
| API05  | BFLA                 | Proteção de endpoints administrativos via @PreAuthorize("hasRole('ADMIN')"). |
| API06  | Business Flow        | Rate Limiting no fluxo de "Adicionar Colaborador" para impedir Spam.         |
| API07  | SSRF                 | Allowlist de domínios e bloqueio de IPs internos na importação de tasks.     |
| API08  | Security Misconf.    | Handler Global para ocultar Stack Traces e headers de segurança (CORS/CSP).  |
| API09  | Inventory Mgmt       | Documentação Swagger versionada e desativação de endpoints de /debug.        |
| API10  | Unsafe Consumption   | Timeouts e sanitização de HTML em dados vindos de APIs externas.             |
---

### Diferenciais de Segurança Implementados

**1. Upload Blindado (Anti-Spoofing)**

Diferente de validações comuns que checam apenas a extensão, este sistema implementa uma **Tripla Checagem:**
1. **Whitelist de Extensão:** Apenas `.pdf`, `.jpg`, `.png`.
2. **Análise de Magic Bytes (Apache Tika):** Lê o cabeçalho binário do arquivo para garantir que um `.pdf` é realmente PDF.
3. **Cross-Check de Consistência:** Verifica se a extensão condiz com o conteúdo real.
    - _Resultado:_ Bloqueia **MIME Spoofing** e **Polyglot Files**  (ex: `virus.exe` renomeado para `imagem.pdf`).

**2. Proteção de Fluxo (Anti-Spam)**

No recurso de **Adicionar Colaborador**, utilizamos o algoritmo **Token Bucket**.
- Efeito: Impede que um atacante automatize a adição de milhares de pessoas a uma tarefa para causar incômodo ou spam

**3. Mitigação de SSRF (Server-Side Request Forgery)**

Ao importar tarefas via URL, a API valida se o destino não é o próprio `localhost`, o IP do Docker Host ou a rede interna do container.

---

## Como Rodar o Projeto.
A aplicação está totalmente "dockerizada". Você não precisa ter o Java ou Maven instalados na máquina, apenas o Docker .

**Passo Único (Docker Compose)**
Na raiz do projeto, execute:

```bash
docker-compose up --build
```
O docker irá:
1. Baixar a imagem do maven com Java 21.
2. Compilar o projeto e gerar o `.jar`
3. Subir o banco de dados PostgreSQL.
4. Subir a API Spring Boot (assim que o banco estiver pronto).

A api estará disponível em http://localhost:8080
Acesse a documentação interativa: http://localhost:8080/swagger-ui/index.html

---

## Guia de Testes Ofensivos (PoC)
1. **BFLA (API05)**: Tente acessar `DELETE /admin/cleanup` com um token de usuário comum.
2. **SSRF (API07)**: No `POST /tasks/import`, tente passar `http://db:5432` e veja a proteção agir.
3. **Broken Auth (API02)**: Erre o código de reset 5 vezes e veja o e-mail ser bloqueado temporariamente.
4. **Mass Assignment (API03)**: Tente enviar `"role": "ADMIN"` no PUT /profile e verifique que o campo é ignorado.

## Roteiro de Testes

##### Cenário 1: Teste o Upload Malicioso (Spoofing)
Objetivo: Tentar enganar o servidor enviando um arquivo falso.
1. Crie um arquivo de texto simples (ex: `virus.txt`).
2. Renomeie ele para `virus.pdf`.
3. Tente fazer o upload no endpoint: `POST /tasks/{id}/upload`.
4. **Resultado Esperado:** A API retornará erro (403/500) com a mensagem de que o conteúdo (text/plain) não condiz com a extensão.

#### Cenário 2: Teste de Rate Limit
Objetivo: Simular um ataque de força bruta.
1. Tente fazer login (POST /auth/login) mais de 5 vezes consecutivas rapidamente.
2. Resultado Esperado: Na 6ª tentativa, a API retornará 429 Too Many Requests.

#### Cenário 3: Auditoria
Objetivo: Verificar o rastro de alterações.
1. Crie, Edite e depois Conclua uma tarefa.
2. Acesse o banco de dados (docker exec -it todo_db psql -U admin -d todo_db).
3. Consulte: SELECT * FROM tb_tasks_aud;.
4. Resultado Esperado: Haverá 3 revisões (ADD, MOD, MOD) documentando todo o ciclo de vida do dado.

#### Cenário 4: Teste de IDOR (Insecure Direct Object References)
Objetivo: Tentar deletar ou alterar uma tarefa de outro usuário.
1. Crie dois usuários distintos: usuario_vitima e usuario_atacante.
2. Logue com usuario_vitima, crie uma tarefa e copie o ID (UUID) dela.
3. Logue com usuario_atacante e pegue o Token dele.
4. Usando o Token do atacante, tente deletar a tarefa da vítima: DELETE /tasks/{UUID_DA_VITIMA}.
5. Resultado Esperado: A API retornará 403 Forbidden com a mensagem "Você não tem permissão para alterar esta tarefa". Isso prova que o sistema valida a posse do recurso, e não apenas o login.

---

## Estrutura do Projeto

```plaintext
src/main/java/br/com/gabwsv/secure_todo
├── config       # Configurações (OpenAPI/Swagger, Security)
├── controller   # Camada REST (HTTP handlers documentados)
├── dto          # Objetos de Transferência (Records com validação)
├── enums        # Domínios fixos (UserRole, TaskPriority)
├── exception    # Tratamento global de erros (GlobalExceptionHandler)
├── model        # Entidades JPA (Mapeamento do Banco)
├── repository   # Interfaces de acesso a dados
├── security     # Configuração JWT, Filtros e Rate Limit
└── service      # Regras de Negócio e Lógica Forense (Tika, IDOR)
```

# Secure To-Do API
API de Gerenciamento de Tarefas com foco em Segurança de Aplicação e Auditoria.
---

Este projeto foi desenvolvido como um desáfio técnico, simulando um ambiente onde a integridade dos dados, a rastreabilidade e a segurança ofensiva/defensiva são prioritárias. A aplicação implementa o conceito de "**Security by Design**", protegendo contra as principais vulnerabilidades do OWASP Top 10.

---
### Tecnologias Utilizadas

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.5.8
- **Segurança:** Spring Security, JWT (JSON Web Tokens), BCrypt
- **Banco de Dados:** PostgreSQL 15
- **Auditoria:** Hibernate Envers (Versionamento histórico de entidades)
- **Upload:** Apache Tika 3.2.2 (Análise de Magic Bytes e Detecção de Mime-Type)
- **Rate Limiting:** Bucket4j (Proteção contra Brute-Forcing/DDoS nível de aplicação)
- **Documentação:** OpenAPI 3 (Swagger UI)
- **Infraestrutura:** Docker & Docker-Compose
---

### Diferenciais de Segurança Implementados

**1. Upload Blindado (Anti-Spoofing)**

Diferente de validações comuns que checam apenas a extensão, este sistema implementa uma **Tripla Checagem:**
1. **Whitelist de Extensão:** Apenas `.pdf`, `.jpg`, `.png`.
2. **Análise de Magic Bytes (Apache Tika):** Lê o cabeçalho binário do arquivo para garantir que um `.pdf` é realmente PDF.
3. **Cross-Check de Consistência:** Verifica se a extensão condiz com o conteúdo real.
    - _Resultado:_ Bloqueia **MIME Spoofing** e **Polyglot Files**  (ex: `virus.exe` renomeado para `imagem.pdf`).

**2. Auditoria de Dados (Hibernate Envers)**

Todas as operações críticas (Criação, Edição, Deleção de Tarefas e Usuários) são auditadas automaticamente em tabelas espelho (`_AUD`).
- **Rastreabilidade:** O sistema sabe QUEM alterou, O QUE foi alterado e QUANDO (Timestamp), garantindo não-repudio.

**3. Proteção contra IDOR (Insecure Direct Object References)**

O sistema valida a propriedade do recurso em nivel de serviço.
- Mesmo que um usuário autenticado descubra o UUID da tarefa de outro usuário, ele receberá `403 Forbidden` ao tentar acessá-la ou modificá-la.

**4. Rate Limiting (Token Bucket)**

Implementação do algoritmo Token Bucket para proteger a rota de Login.
- **Regra:** 5 tentativas por minuto por IP.
- **Efeito:** Mítiga ataques de força bruta (Brute Force).

**5. Sanitização de Inputs**
- **DTOs Estritos:** Uso de `Records` e validação rigorosa (`@Pattern`, `@Size`) para impedir Mass Assignment e ataques de Injeção.
- **Tratamento de Exceções:** GlobalExceptionHandler configurado para não vazar Stack Traces (Information Leakage).

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

--- 

## Documentação Interativa (Swagger UI)
A documentação completa dos endpoints está disponível e configurada para suportar autenticação JWT.

1. Acesse: http://localhost:8080/swagger-ui/index.html
2. **Criação do Usuário (Passo Obrigatório):**
   - Como o banco inicia vazio, vá no endpoint `POST /auth/register`.
   - Crie um usuário (ex: `admin`) com uma senha forte. (Requisito obrigatório)
3. **Autenticação:**
   - Vá no endpoint `POST /auth/login`.
   - Faça login com o usuário criado e copie o Token gerado (sem as aspas).
   - Clique no botão **Authorize** (Cadeado) no topo da página.
   - Cole o token e confirme.
4. Agora você pode testar as rotas protegidas (Tasks, Upload, etc).

---

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

# 🌱 Pé de Quintal — Sistema Web (MVP)

> _"Nada do seu quintal se perde."_

Aplicação web para **troca e doação** de frutas, hortaliças, temperos e mudas cultivados em casa.
Spring Boot + Thymeleaf + Spring Data JPA + Spring Security + Supabase (PostgreSQL).

**Fluxo principal:** Cadastro → Publicar item → Outro usuário envia proposta → Dono **aceita ou rejeita** →
Contato liberado para combinar a retirada → Confirmação da entrega.

---

## 1. O que foi construído

| Tela | Rota | O que faz |
|---|---|---|
| W1 — Entrar | `GET /login` | Login por e-mail e senha (sessão + BCrypt) |
| W1 — Criar conta | `GET/POST /cadastro` | Nome, e-mail, telefone, senha, cidade/UF |
| W2 — Lista de itens | `GET /itens?busca=&categoria=&cidade=` | Itens **ativos e dentro do prazo**, com busca e filtros |
| Detalhe + proposta | `GET /itens/{id}` · `POST /itens/{id}/propostas` | Mostra o item e envia a proposta na própria tela |
| W3 — Publicar | `GET /itens/novo` · `POST /itens` | Formulário único com 1 foto opcional |
| Excluir anúncio | `POST /itens/{id}/excluir` | Só o dono, e só enquanto **ATIVO** |
| W4 — Minhas propostas | `GET /propostas` | Abas Recebidas/Enviadas + tabela "Meus anúncios" |
| Aceitar / Rejeitar | `POST /propostas/{id}/aceitar` · `/rejeitar` | Só o dono do anúncio |
| Confirmar entrega | `POST /propostas/{id}/concluir` | Qualquer uma das duas partes; gera a `Transacao` |

### Regras de negócio (todas na camada **Service**)

- **Aceitar** é uma transação atômica (`@Transactional`): a proposta vira `ACEITA`, o anúncio vira
  `RESERVADO` e **as demais propostas pendentes do item são rejeitadas automaticamente**.
- O **telefone** só aparece para quem teve a proposta aceita (e, para o dono, o telefone de quem foi aceito).
- Não é possível propor no próprio anúncio, nem mandar duas propostas pendentes para o mesmo item.
- Em anúncios de **troca**, "o que você oferece" é obrigatório; em **doação**, é opcional.
- Itens com `disponivelAte` vencido somem da lista (filtro na consulta, sem job agendado).
- `@Version` no `Anuncio` (*optimistic locking*) impede que dois "Aceitar" simultâneos reservem o mesmo item.
- **Confirmar entrega** muda o anúncio para `CONCLUIDO` e grava a `Transacao` com o peso em kg
  (quando a unidade é `kg` ou `g`).

### Máquina de estados

```
Anúncio:   ATIVO ──(aceitar proposta)──> RESERVADO ──(confirmar entrega)──> CONCLUIDO
Proposta:  PENDENTE ──> ACEITA   |   PENDENTE ──> REJEITADA
```

---

## 2. Etapa 1: Criar o banco no Supabase

1. Crie um **New project** no Supabase (região *South America – São Paulo*) e **anote a senha do banco**.
2. Abra o **SQL Editor** e execute o script [`database/01_schema.sql`](database/01_schema.sql).
   Ele cria as 5 tabelas (`usuario`, `categoria`, `anuncio`, `proposta`, `transacao`) e o seed das
   categorias (Hortaliça, Fruta, Tempero, Muda).
3. Confira no **Table Editor** se as tabelas apareceram e se `categoria` tem 4 linhas.

### Pegar os dados de conexão

**Connect → Session pooler → JDBC.** Anote host, porta, usuário (`postgres.xxxxxxxx`) e senha.

> _Por que o pooler? A conexão direta usa IPv6, que muitas redes não têm. O pooler funciona em IPv4._

---

## 3. Etapa 2: Configurar a conexão

Copie `.env.example` para **`.env`** (na mesma pasta do `pom.xml`) e preencha:

```properties
DB_URL=jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.SEU_ID_DO_PROJETO
DB_PASSWORD=sua_senha
APP_DADOS_EXEMPLO=true
```

O `application.properties` lê o `.env` com `spring.config.import=optional:file:.env[.properties]`
e usa `spring.jpa.hibernate.ddl-auto=validate`: o Hibernate **não cria tabelas**, só confere se as
entidades batem com o script SQL.

`APP_DADOS_EXEMPLO=true` cria, **na primeira execução** (banco sem usuários), 3 usuários e 5 anúncios:

| E-mail | Senha | Cidade |
|---|---|---|
| maria@pedequintal.com | 123456 | Sete Lagoas/MG |
| joao@pedequintal.com | 123456 | Sete Lagoas/MG |
| ana@pedequintal.com | 123456 | Belo Horizonte/MG |

---

## 4. Etapa 3: Executar

Pré-requisitos: **JDK 21**, **Maven 3.9+** (ou a IDE: IntelliJ/Eclipse/VS Code importam o `pom.xml`).

```bash
mvn spring-boot:run
```

Ou rode a classe `PeDeQuintalApplication` pela IDE. Abra **http://localhost:8080**.

Rodar os testes (JUnit 5 + Mockito, sem precisar do banco):

```bash
mvn test
```

---

## 5. Roteiro de teste manual (fluxo completo)

1. Entre como **ana@pedequintal.com** → abra "Limão taiti" → ofereça "cenoura 1 kg" → **Enviar proposta**.
2. Saia e entre como **maria@pedequintal.com** → envie outra proposta para o mesmo limão.
3. Saia e entre como **joao@pedequintal.com** → **Minhas propostas** → Recebidas (2) → **Aceitar** a da Ana.
   - O limão fica **Reservado**, a proposta da Maria vira **Rejeitada** e o telefone da Ana aparece.
4. Entre como **ana** → Minhas propostas → Enviadas: o telefone do João aparece → **Confirmar entrega**.
5. O anúncio fica **Concluído** e a linha é gravada em `transacao` (confira no Supabase, `peso_kg = 3`).
6. Publique um item novo com foto e depois **exclua** pela tabela "Meus anúncios".

---

## 6. Estrutura do projeto

```
src/main/java/br/edu/aula/pedequintal/
  PeDeQuintalApplication.java
  config/       SecurityConfig, WebConfig (uploads), GlobalControllerAdvice, DadosExemploLoader
  controller/   HomeController, AuthController, AnuncioController, PropostaController
  dto/          CadastroForm, AnuncioForm, PropostaForm   (Bean Validation)
  model/        Usuario, Categoria, Anuncio, Proposta, Transacao + enums
  repository/   interfaces JpaRepository (query methods + 2 @Query)
  service/      UsuarioService, AnuncioService, PropostaService, FotoService, UsuarioDetailsService
src/main/resources/
  templates/    login, cadastro, itens/lista, itens/novo, itens/detalhe, propostas, error, fragments/layout
  static/css/   style.css  (verde #2E7D32 · laranja #F57C00)
  application.properties
database/01_schema.sql
src/test/java/...  PropostaServiceTest, AnuncioServiceTest
```

Camadas: **Controller → Service → Repository → Banco**. Os controllers são "cascas finas":
recebem o formulário, chamam o service e fazem **Post/Redirect/Get** com flash messages.

---

## 7. Decisões de projeto

- **Spring Boot 3.5.x** (estável, Java 21). Para usar 4.x, basta trocar a versão do `parent` no `pom.xml`.
- **DTOs nos formulários** (`CadastroForm`, `AnuncioForm`) para não expor campos como `senhaHash`.
- **Filtros com `""` e `0` em vez de `null`** na `@Query`: o PostgreSQL não descobre o tipo de um
  parâmetro nulo em `(:x IS NULL OR ...)`.
- **`JOIN FETCH`** na lista de itens: anunciante e categoria vêm no mesmo SELECT (menos idas ao Supabase).
- **Foto em pasta local** (`uploads/`), servida em `/uploads/**`. Em deploy com disco efêmero
  (Render/Railway) as fotos se perdem ao reiniciar — limitação aceita no MVP.
- **CSRF ligado**: o `th:action` do Thymeleaf injeta o token em todos os formulários automaticamente.

### Fora do escopo (evolução v2)

Aplicativo React Native / API REST, chat, pagamento, avaliação/reputação, geolocalização,
notificações, painel administrativo, relatórios, moderação, múltiplas fotos, WhatsApp e Supabase Storage.

---

## 8. Erros comuns

- **Timeout / Connection refused:** use o *Session pooler* e confira host, porta e usuário.
- **password authentication failed:** senha errada ou `.env` fora da pasta do `pom.xml`.
- **Schema-validation: missing table / wrong column type:** o `01_schema.sql` não foi executado
  (ou foi alterado). A mensagem diz qual tabela/coluna não bateu.
- **Login sempre falha:** usuários criados direto no SQL precisam de senha em **BCrypt**; cadastre pela tela.
- **"Este anúncio foi alterado por outra ação ao mesmo tempo":** é o `@Version` funcionando
  (dois aceites simultâneos). Recarregue a página.

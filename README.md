# 🌱 Pé de Quintal

> _Nada do seu quintal se perde._

Plataforma web para **troca e doação** de frutas, hortaliças, temperos e mudas cultivados em casa.
Desenvolvi o projeto como trabalho acadêmico para conectar vizinhos que produzem mais do que consomem
e, com isso, reduzir o desperdício de alimentos.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-MVC-005F0F)
![PostgreSQL](https://img.shields.io/badge/Supabase-PostgreSQL-3ECF8E)
![Status](https://img.shields.io/badge/status-MVP-F57C00)

---

## 📌 Sumário

- [Sobre o projeto](#-sobre-o-projeto)
- [Funcionalidades](#-funcionalidades)
- [Regras de negócio](#-regras-de-negócio)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Modelo de dados](#-modelo-de-dados)
- [Como executar](#-como-executar)
- [Testes](#-testes)
- [Rotas](#-rotas)
- [Decisões técnicas](#-decisões-técnicas)
- [Próximos passos](#-próximos-passos)
- [Autor](#-autor)

---

## 💡 Sobre o projeto

Muita gente tem um pé de limão, uma horta pequena ou vasos de temperos que produzem mais do que a
família consegue consumir, e o excedente acaba estragando. O **Pé de Quintal** resolve isso de forma simples:

1. A pessoa **publica** o que tem sobrando.
2. Outra pessoa **envia uma proposta** (pedido de doação ou oferta de troca).
3. O anunciante **aceita ou rejeita**.
4. Com a proposta aceita, o **contato é liberado** para combinarem a retirada.
5. Depois da entrega, qualquer um dos dois **confirma a conclusão**.

O escopo é um **MVP web**: fiz só o necessário para validar o fluxo principal, de ponta a ponta.

---

## ✅ Funcionalidades

- Cadastro e login de usuários (senha criptografada com BCrypt)
- Listagem dos anúncios disponíveis
- Busca por texto e filtros por **categoria** e **cidade**
- Página de detalhes do anúncio
- Publicação de anúncio com **uma foto opcional**
- Envio de proposta direto na página do item
- Tela **Minhas propostas** com as abas *Recebidas* e *Enviadas*
- Aceite e rejeição de propostas recebidas
- Confirmação da entrega
- Exclusão do próprio anúncio (enquanto ele estiver ativo)

### Telas

| Tela | Descrição |
|---|---|
| **Entrar / Criar conta** | Login por e-mail e senha; cadastro com nome, e-mail, telefone, cidade e UF |
| **Itens disponíveis** | Grade de cards com busca, filtros e selo de *Doação* ou *Troca* |
| **Detalhe do item** | Informações completas e formulário de proposta |
| **Publicar item** | Formulário único com foto, categoria, quantidade, modalidade e prazo |
| **Minhas propostas** | Propostas recebidas e enviadas, e tabela com os meus anúncios |

> 💡 Sugestão: adicione aqui prints das telas, por exemplo `![Lista de itens](docs/lista.png)`.

---

## 📐 Regras de negócio

Toda a regra de negócio fica na camada **Service**. Os controllers só recebem a requisição e redirecionam.

**Ciclo de vida**

```
Anúncio:   ATIVO ──(proposta aceita)──> RESERVADO ──(entrega confirmada)──> CONCLUIDO
Proposta:  PENDENTE ──> ACEITA
           PENDENTE ──> REJEITADA
```

**Regras implementadas**

- O **aceite** é atômico (`@Transactional`): a proposta vira `ACEITA`, o anúncio vira `RESERVADO`
  e **as outras propostas pendentes do mesmo item são rejeitadas automaticamente**.
- O **telefone** só aparece depois que a proposta é aceita, para preservar a privacidade (LGPD).
- Ninguém pode enviar proposta para o próprio anúncio.
- Cada usuário pode ter só **uma proposta pendente** por item.
- Em anúncios de **troca**, é obrigatório dizer o que está sendo oferecido; em **doação**, é opcional.
- Anúncios com o prazo vencido saem da listagem automaticamente.
- Só o dono responde às propostas, e só pode excluir o anúncio enquanto ele estiver **ATIVO**.
- A confirmação da entrega gera um registro em `transacao`, com o peso em kg quando a unidade é `kg` ou `g`.
- **Concorrência:** usei *optimistic locking* (`@Version`) para impedir que dois aceites simultâneos
  reservem o mesmo item.

---

## 🛠 Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Build | Maven |
| Framework | Spring Boot 3.5 (Web / MVC) |
| Views | Thymeleaf + HTML + CSS |
| Persistência | Spring Data JPA + Hibernate |
| Segurança | Spring Security (sessão + BCrypt + CSRF) |
| Validação | Bean Validation |
| Banco de dados | PostgreSQL no Supabase |
| Testes | JUnit 5 + Mockito + AssertJ |

---

## 🏗 Arquitetura

O projeto segue o padrão **MVC em camadas**:

```
Navegador ──> Controller ──> Service ──> Repository ──> PostgreSQL (Supabase)
                  │
                  └──> Thymeleaf (HTML renderizado no servidor)
```

```
src/main/java/br/edu/aula/pedequintal/
├── PeDeQuintalApplication.java
├── config/        # Segurança, uploads, dados de exemplo e tratamento global de erros
├── controller/    # Auth, Anúncio, Proposta e Home
├── dto/           # Formulários com Bean Validation
├── model/         # Entidades JPA e enums de status
├── repository/    # Interfaces Spring Data JPA
└── service/       # Regras de negócio

src/main/resources/
├── templates/     # Páginas Thymeleaf e fragmentos (layout, header, mensagens)
├── static/css/    # Identidade visual
└── application.properties

database/01_schema.sql   # Script de criação do banco
```

Usei o padrão **Post/Redirect/Get** em todos os formulários, com *flash messages* para os avisos
de sucesso e de erro. Assim, apertar F5 depois de um envio não repete a ação.

---

## 🗃 Modelo de dados

```
usuario 1 ──── N anuncio N ──── 1 categoria
   │                │
   │ 1              │ 1
   └──── N proposta N
                │ 1
                └──── 1 transacao
```

| Tabela | Descrição |
|---|---|
| `usuario` | Dados de cadastro (senha guardada como hash BCrypt) |
| `categoria` | Hortaliça, Fruta, Tempero e Muda |
| `anuncio` | Item publicado: quantidade, unidade, modalidade, status, prazo e foto |
| `proposta` | Pedido ou oferta de troca feito por outro usuário |
| `transacao` | Registro da entrega concluída |

---

## 🚀 Como executar

### Pré-requisitos

- JDK 21
- Maven 3.9+ (ou uma IDE como IntelliJ, Eclipse ou VS Code)
- Uma conta no [Supabase](https://supabase.com)

### 1. Clonar o repositório

```bash
git clone https://github.com/SEU_USUARIO/pe-de-quintal.git
cd pe-de-quintal
```

### 2. Criar o banco

No **SQL Editor** do Supabase, execute o script `database/01_schema.sql`.
Ele cria as tabelas e já cadastra as categorias.

### 3. Configurar as credenciais

Na raiz do projeto, onde fica o `pom.xml`, crie um arquivo `.env` usando o `.env.example` como base:

```properties
PDQ_DB_USERNAME=postgres.ID_DO_SEU_PROJETO
PDQ_DB_PASSWORD=sua_senha_do_banco
APP_DADOS_EXEMPLO=true
```

No `application.properties`, ajuste a URL do *Session pooler* do seu projeto
(*Connect → Session pooler*):

```properties
spring.datasource.url=jdbc:postgresql://SEU_HOST.pooler.supabase.com:5432/postgres?sslmode=require
```

> ⚠️ O `.env` está no `.gitignore` e **nunca deve ser enviado para o repositório**.
> No pooler, o usuário precisa estar no formato `postgres.ID_DO_PROJETO`.

### 4. Rodar

```bash
mvn spring-boot:run
```

Depois, acesse **http://localhost:8080**.

### Usuários de exemplo

Com `APP_DADOS_EXEMPLO=true`, a primeira execução cria alguns usuários e anúncios para teste:

| E-mail | Senha |
|---|---|
| maria@pedequintal.com | 123456 |
| joao@pedequintal.com | 123456 |
| ana@pedequintal.com | 123456 |

---

## 🧪 Testes

Os testes unitários cobrem as principais regras de negócio sem precisar do banco:

```bash
mvn test
```

**Cenários cobertos**

- Aceite: muda os status corretamente e rejeita as demais propostas
- Bloqueio de aceite e rejeição por quem não é o dono
- Bloqueio de proposta no próprio anúncio, em item reservado e de proposta duplicada
- Item oferecido obrigatório em anúncios de troca
- Confirmação de entrega e cálculo do peso
- Exclusão permitida só para o dono e só com o anúncio ativo

### Roteiro de teste manual

1. Entre como **ana** e envie uma proposta para "Limão taiti".
2. Entre como **maria** e envie outra proposta para o mesmo item.
3. Entre como **joao**, abra *Minhas propostas* e **aceite** a proposta da Ana.
   O item fica *Reservado*, a proposta da Maria é rejeitada e o contato é liberado.
4. Entre como **ana** e clique em **Confirmar entrega**. O anúncio fica *Concluído*.

---

## 🔗 Rotas

| Método | Rota | Ação |
|---|---|---|
| GET | `/login` | Tela de login |
| GET / POST | `/cadastro` | Criar conta |
| GET | `/itens` | Listagem com `?busca=&categoria=&cidade=` |
| GET | `/itens/{id}` | Detalhe do item |
| GET | `/itens/novo` | Formulário de publicação |
| POST | `/itens` | Publicar item |
| POST | `/itens/{id}/propostas` | Enviar proposta |
| POST | `/itens/{id}/excluir` | Excluir o próprio anúncio |
| GET | `/propostas` | Minhas propostas (`?aba=enviadas`) |
| POST | `/propostas/{id}/aceitar` | Aceitar proposta |
| POST | `/propostas/{id}/rejeitar` | Rejeitar proposta |
| POST | `/propostas/{id}/concluir` | Confirmar entrega |

---

## 🧠 Decisões técnicas

- **Formulários com DTOs** (`CadastroForm`, `AnuncioForm`, `PropostaForm`): assim, campos sensíveis
  como `senhaHash` nunca ficam expostos no formulário.
- **`ddl-auto=validate`**: o banco é criado só pelo script SQL. O Hibernate apenas confere se as
  entidades batem com as tabelas.
- **`JOIN FETCH` na listagem**: anunciante e categoria vêm na mesma consulta, o que reduz as idas ao banco.
- **Filtros opcionais com `""` e `0`** na `@Query`: evita um problema do PostgreSQL com parâmetros nulos.
- **Foto salva em pasta local** (`uploads/`): uma solução simples, adequada ao escopo do MVP.
- **CSRF ativo**: o Thymeleaf inclui o token em todos os formulários automaticamente.

---

## 🔭 Próximos passos

- Aplicativo mobile (React Native) consumindo uma API REST
- Chat interno entre os usuários
- Avaliações e reputação
- Busca por proximidade (geolocalização)
- Notificações de novas propostas
- Várias fotos por anúncio, com armazenamento em nuvem
- Painel com o impacto gerado (kg de alimentos que deixaram de ser desperdiçados)

---

## 👤 Autores

**Lucas Gabriel Valadares**
- LinkedIn:https://www.linkedin.com/in/lucas-valadares-1528b2249/
- GitHub: https://github.com/Lucasvalada12

**Marcus Vinícius Fernandes Lima**
- LinkedIn:https://www.linkedin.com/in/marcus-vfl
- GitHub: https://github.com/xMarKkk

**João Pedro Evangelista da Silva**
- LinkedIn:https://www.linkedin.com/in/joaopedroevangelistadasilva
- GitHub: https://github.com/joaopedasilva

**Marcus Vinícius Marques França Smith**
- LinkedIn:https://www.linkedin.com/in/marcus-smith
- GitHub:https://github.com/marcusfrancasmith

**Elerson Silva Evangelista**
- LinkedIn:https://www.linkedin.com/in/elerson-evangelista-97b43628a
- GitHub: https://github.com/elersons

**Vinicius Kilesse Azevedo**
- LinkedIn:https://www.linkedin.com/in/viniciuskl
- GitHub: https://github.com/kilesse
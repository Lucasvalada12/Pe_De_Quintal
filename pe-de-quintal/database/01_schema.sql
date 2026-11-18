-- =============================================================
-- Pé de Quintal — script do banco (PostgreSQL / Supabase)
-- Execute no SQL Editor do Supabase ANTES de subir a aplicação.
-- A aplicação usa ddl-auto=validate: ela NÃO cria tabelas,
-- apenas confere se as entidades batem com este script.
-- =============================================================

DROP TABLE IF EXISTS transacao;
DROP TABLE IF EXISTS proposta;
DROP TABLE IF EXISTS anuncio;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS usuario;

CREATE TABLE usuario (
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    senha_hash  VARCHAR(100) NOT NULL,
    telefone    VARCHAR(20)  NOT NULL,
    cidade      VARCHAR(100) NOT NULL,
    uf          VARCHAR(2)   NOT NULL
);

CREATE TABLE categoria (
    id    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE anuncio (
    id              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    titulo          VARCHAR(100)  NOT NULL,
    descricao       VARCHAR(500),
    quantidade      NUMERIC(10,2) NOT NULL,
    unidade         VARCHAR(20)   NOT NULL,
    modalidade      VARCHAR(10)   NOT NULL,   -- DOACAO | TROCA
    deseja_receber  VARCHAR(200),
    status          VARCHAR(15)   NOT NULL,   -- ATIVO | RESERVADO | CONCLUIDO
    foto_url        VARCHAR(255),
    local_retirada  VARCHAR(150)  NOT NULL,
    disponivel_ate  DATE          NOT NULL,
    data_criacao    TIMESTAMP     NOT NULL,
    versao          INT           NOT NULL DEFAULT 0,  -- optimistic locking
    usuario_id      INT           NOT NULL,
    categoria_id    INT           NOT NULL,
    CONSTRAINT fk_anuncio_usuario   FOREIGN KEY (usuario_id)   REFERENCES usuario (id),
    CONSTRAINT fk_anuncio_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT ck_anuncio_modalidade CHECK (modalidade IN ('DOACAO', 'TROCA')),
    CONSTRAINT ck_anuncio_status     CHECK (status IN ('ATIVO', 'RESERVADO', 'CONCLUIDO'))
);

CREATE TABLE proposta (
    id              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_oferecido  VARCHAR(200),
    mensagem        VARCHAR(500),
    status          VARCHAR(15) NOT NULL,     -- PENDENTE | ACEITA | REJEITADA
    data_criacao    TIMESTAMP   NOT NULL,
    data_resposta   TIMESTAMP,
    anuncio_id      INT NOT NULL,
    proponente_id   INT NOT NULL,
    CONSTRAINT fk_proposta_anuncio    FOREIGN KEY (anuncio_id)    REFERENCES anuncio (id),
    CONSTRAINT fk_proposta_proponente FOREIGN KEY (proponente_id) REFERENCES usuario (id),
    CONSTRAINT ck_proposta_status CHECK (status IN ('PENDENTE', 'ACEITA', 'REJEITADA'))
);

CREATE TABLE transacao (
    id              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data_conclusao  TIMESTAMP NOT NULL,
    peso_kg         NUMERIC(10,3),
    proposta_id     INT NOT NULL UNIQUE,      -- 1:1 com proposta
    CONSTRAINT fk_transacao_proposta FOREIGN KEY (proposta_id) REFERENCES proposta (id)
);

CREATE INDEX idx_anuncio_status   ON anuncio (status);
CREATE INDEX idx_proposta_anuncio ON proposta (anuncio_id);

-- Seed das categorias (obrigatório)
INSERT INTO categoria (nome) VALUES ('Hortaliça'), ('Fruta'), ('Tempero'), ('Muda');

-- Cria o banco (se ainda não existir)
CREATE DATABASE IF NOT EXISTS senaiapp
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE senaiapp;

-- Tabela de clientes (exemplo inicial para a tela ClienteView)
CREATE TABLE IF NOT EXISTS clientes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome  VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  senha VARCHAR(255) NOT NULL,
  criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE senaiapp;

CREATE TABLE IF NOT EXISTS auditoria (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  momento     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  usuario_id  INT NULL,
  usuario_email VARCHAR(160) NULL,
  operacao    VARCHAR(120) NOT NULL,
  sql_texto   TEXT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

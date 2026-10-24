-- Actividad: Desarrollo de Software Seguro
-- Gestión de funcionarios - Institución Universitaria Digital de Antioquia
-- Motor: PostgreSQL
-- Script 1 de 3: creación de la estructura de la base de datos

-- Orden de ejecución:
--   1. crear_bd.sql    (este archivo)
--   2. poblar_bd.sql   (catálogos y datos de ejemplo)
--   3. usuario_app.sql (rol de la aplicación)
--
-- Antes, desde la base "postgres":
-- CREATE DATABASE recursos_humanos;
-- Luego conectarse a recursos_humanos y ejecutar este script.

-- Limpieza para poder re-ejecutar el script (orden inverso a la creación)
DROP TABLE IF EXISTS informacion_academica;
DROP TABLE IF EXISTS miembro_familiar;
DROP TABLE IF EXISTS funcionario;
DROP TABLE IF EXISTS nivel_estudio;
DROP TABLE IF EXISTS rol_familiar;
DROP TABLE IF EXISTS sexo;
DROP TABLE IF EXISTS estado_civil;
DROP TABLE IF EXISTS tipo_identificacion;
DROP FUNCTION IF EXISTS fn_actualizar_fecha_modificacion CASCADE;

-- 1. CATÁLOGOS (no dependen de ninguna otra tabla)
-- Se modelan como tablas, no como CHECK enumerado, para
-- poder agregar nuevos valores sin alterar la estructura.

CREATE TABLE tipo_identificacion (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE estado_civil (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE sexo (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE rol_familiar (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE nivel_estudio (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);


-- 2. FUNCIONARIO (Esta es la tabla principal del CRUD)
-- Se implemento RESTRICT en los catálogos para que no se pueda borrar un catálogo que esté 
-- siendo usado por un funcionario.
-- integracion de fecha_creacion / fecha_modificacion esto permite la trazabilidad de cambios
-- buena práctica en sistemas de RR.HH.

CREATE TABLE funcionario (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_identificacion_id INT NOT NULL
        REFERENCES tipo_identificacion(id) ON DELETE RESTRICT,
    numero_identificacion  VARCHAR(20)  NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    estado_civil_id INT NOT NULL
        REFERENCES estado_civil(id) ON DELETE RESTRICT,
    sexo_id INT NOT NULL
        REFERENCES sexo(id) ON DELETE RESTRICT,
    direccion VARCHAR(255) NOT NULL,
    telefono VARCHAR(20)  NOT NULL,
    fecha_nacimiento DATE NOT NULL
        CHECK (fecha_nacimiento <= CURRENT_DATE - INTERVAL '18 years'),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_funcionario_doc
        UNIQUE (tipo_identificacion_id, numero_identificacion)
);

-- 3. TABLAS HIJAS DE FUNCIONARIO
-- funcionario_id usa CASCADE: al borrar un funcionario se borran también sus familiares y estudios.

CREATE TABLE miembro_familiar (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    funcionario_id INT NOT NULL
        REFERENCES funcionario(id) ON DELETE CASCADE,
    rol_familiar_id INT NOT NULL
        REFERENCES rol_familiar(id) ON DELETE RESTRICT,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    tipo_identificacion_id INT NOT NULL
        REFERENCES tipo_identificacion(id) ON DELETE RESTRICT,
    numero_identificacion VARCHAR(20) NOT NULL,
    fecha_nacimiento DATE NOT NULL
        CHECK (fecha_nacimiento <= CURRENT_DATE),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_miembro_doc
        UNIQUE (funcionario_id, tipo_identificacion_id, numero_identificacion)
);

CREATE TABLE informacion_academica (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    funcionario_id INT NOT NULL
        REFERENCES funcionario(id) ON DELETE CASCADE,
    universidad VARCHAR(150) NOT NULL,
    nivel_estudio_id INT NOT NULL
        REFERENCES nivel_estudio(id) ON DELETE RESTRICT,
    titulo VARCHAR(150) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. ÍNDICES sobre las llaves foráneas más consultadas
-- (PostgreSQL no los crea automáticamente en las FK)

CREATE INDEX idx_miembro_funcionario   ON miembro_familiar(funcionario_id);
CREATE INDEX idx_academica_funcionario ON informacion_academica(funcionario_id);

-- 5. FUNCIÓN Y TRIGGERS PARA AUDITORÍA DE FECHA
-- Esto hace que mantiene actualizado automáticamente fecha_modificacion al ejecutar UPDATE.

CREATE OR REPLACE FUNCTION fn_actualizar_fecha_modificacion()
RETURNS TRIGGER
LANGUAGE plpgsql AS $$
BEGIN
    NEW.fecha_modificacion := CURRENT_TIMESTAMP;
    RETURN NEW;
END; $$;

CREATE TRIGGER trg_funcionario_fecha_mod
BEFORE UPDATE ON funcionario
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_miembro_familiar_fecha_mod
BEFORE UPDATE ON miembro_familiar
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

CREATE TRIGGER trg_informacion_academica_fecha_mod
BEFORE UPDATE ON informacion_academica
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_fecha_modificacion();

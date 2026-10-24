-- EA1: Desarrollo de Software Seguro
-- Script 3 de 3: usuario de la aplicación (mínimo privilegio)
-- Ejecutar como postgres, conectado a recursos_humanos,
-- DESPUÉS de crear_bd.sql y poblar_bd.sql.

-- La aplicación Java debe conectarse con este rol, nunca
-- como "postgres". Si la app es comprometida, el atacante
-- solo tendrá los permisos de este usuario.
--
-- IMPORTANTE: cambia la contraseña antes de usarlo y no la
-- subas a GitHub. La real va en un archivo de configuración
-- incluido en el .gitignore.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'app_rrhh') THEN
        CREATE ROLE app_rrhh LOGIN PASSWORD 'cambia_esta_clave';
    END IF;
END $$;

GRANT CONNECT ON DATABASE recursos_humanos TO app_rrhh;
GRANT USAGE ON SCHEMA public TO app_rrhh;

-- Catálogos: solo lectura (la app no los modifica)
GRANT SELECT ON tipo_identificacion, estado_civil, sexo,
                rol_familiar, nivel_estudio TO app_rrhh;

-- Tablas del CRUD: lectura y escritura
GRANT SELECT, INSERT, UPDATE, DELETE
    ON funcionario, miembro_familiar, informacion_academica TO app_rrhh;

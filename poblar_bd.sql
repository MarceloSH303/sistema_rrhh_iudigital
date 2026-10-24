-- Actividad: Desarrollo de Software Seguro
-- Script 2 de 3: poblado inicial de la base de datos
-- Ejecutar DESPUÉS de crear_bd.sql, conectado a recursos_humanos.
-- Todos los datos de personas son ficticios.

-- Permite volver a ejecutar el script sin duplicar datos
TRUNCATE TABLE informacion_academica, miembro_familiar, funcionario,
               nivel_estudio, rol_familiar, sexo, estado_civil,
               tipo_identificacion
RESTART IDENTITY CASCADE;

-- 1. CATÁLOGOS (necesarios para que la aplicación inicie)

INSERT INTO tipo_identificacion (nombre) VALUES
    ('Cédula de Ciudadanía'),
    ('Cédula de Extranjería'),
    ('Pasaporte'),
    ('Tarjeta de Identidad');

INSERT INTO estado_civil (nombre) VALUES
    ('Soltero/a'),
    ('Casado/a'),
    ('Unión Libre'),
    ('Divorciado/a'),
    ('Viudo/a');

INSERT INTO sexo (nombre) VALUES
    ('Masculino'),
    ('Femenino'),
    ('Otro');

INSERT INTO rol_familiar (nombre) VALUES
    ('Hijo/a'),
    ('Cónyuge/Compañero/a'),
    ('Padre'),
    ('Madre');

INSERT INTO nivel_estudio (nombre) VALUES
    ('Bachiller'),
    ('Técnico'),
    ('Tecnólogo'),
    ('Pregrado'),
    ('Especialización'),
    ('Maestría'),
    ('Doctorado');

-- 2. FUNCIONARIOS DE EJEMPLO
-- Los ids de los catálogos se buscan por nombre, así el
-- script no depende del orden en que se insertaron.

INSERT INTO funcionario
    (tipo_identificacion_id, numero_identificacion, nombres, apellidos,
     estado_civil_id, sexo_id, direccion, telefono, fecha_nacimiento)
VALUES
    ((SELECT id FROM tipo_identificacion WHERE nombre = 'Cédula de Ciudadanía'),
     '1017234501', 'Carlos Andrés', 'Ramírez Gómez',
     (SELECT id FROM estado_civil WHERE nombre = 'Casado/a'),
     (SELECT id FROM sexo WHERE nombre = 'Masculino'),
     'Calle 45 # 80-12, Medellín', '3004567890', '1985-03-14'),

    ((SELECT id FROM tipo_identificacion WHERE nombre = 'Cédula de Ciudadanía'),
     '43875612', 'María Fernanda', 'López Restrepo',
     (SELECT id FROM estado_civil WHERE nombre = 'Casado/a'),
     (SELECT id FROM sexo WHERE nombre = 'Femenino'),
     'Calle 45 # 80-12, Medellín', '3112345678', '1987-09-02'),

    ((SELECT id FROM tipo_identificacion WHERE nombre = 'Cédula de Ciudadanía'),
     '1036789045', 'Juan Camilo', 'Muñoz Torres',
     (SELECT id FROM estado_civil WHERE nombre = 'Soltero/a'),
     (SELECT id FROM sexo WHERE nombre = 'Masculino'),
     'Carrera 70 # 32-18, Envigado', '3209876543', '1995-11-23');

-- 3. GRUPO FAMILIAR
-- Carlos y María Fernanda son pareja y comparten un hijo:
-- el mismo niño aparece en el grupo familiar de ambos, lo
-- que permite el UNIQUE (funcionario_id, tipo, número). 

INSERT INTO miembro_familiar
    (funcionario_id, rol_familiar_id, nombres, apellidos,
     tipo_identificacion_id, numero_identificacion, fecha_nacimiento)
VALUES
    ((SELECT id FROM funcionario WHERE numero_identificacion = '1017234501'),
     (SELECT id FROM rol_familiar WHERE nombre = 'Cónyuge/Compañero/a'),
     'María Fernanda', 'López Restrepo',
     (SELECT id FROM tipo_identificacion WHERE nombre = 'Cédula de Ciudadanía'),
     '43875612', '1987-09-02'),

    ((SELECT id FROM funcionario WHERE numero_identificacion = '1017234501'),
     (SELECT id FROM rol_familiar WHERE nombre = 'Hijo/a'),
     'Sofía', 'Ramírez López',
     (SELECT id FROM tipo_identificacion WHERE nombre = 'Tarjeta de Identidad'),
     '1100234567', '2014-06-10'),

    ((SELECT id FROM funcionario WHERE numero_identificacion = '43875612'),
     (SELECT id FROM rol_familiar WHERE nombre = 'Cónyuge/Compañero/a'),
     'Carlos Andrés', 'Ramírez Gómez',
     (SELECT id FROM tipo_identificacion WHERE nombre = 'Cédula de Ciudadanía'),
     '1017234501', '1985-03-14'),

    ((SELECT id FROM funcionario WHERE numero_identificacion = '43875612'),
     (SELECT id FROM rol_familiar WHERE nombre = 'Hijo/a'),
     'Sofía', 'Ramírez López',
     (SELECT id FROM tipo_identificacion WHERE nombre = 'Tarjeta de Identidad'),
     '1100234567', '2014-06-10'),

    ((SELECT id FROM funcionario WHERE numero_identificacion = '1036789045'),
     (SELECT id FROM rol_familiar WHERE nombre = 'Madre'),
     'Luz Marina', 'Torres Vélez',
     (SELECT id FROM tipo_identificacion WHERE nombre = 'Cédula de Ciudadanía'),
     '32456789', '1965-01-30');

-- 4. INFORMACIÓN ACADÉMICA

INSERT INTO informacion_academica
    (funcionario_id, universidad, nivel_estudio_id, titulo)
VALUES
    ((SELECT id FROM funcionario WHERE numero_identificacion = '1017234501'),
     'Universidad de Antioquia',
     (SELECT id FROM nivel_estudio WHERE nombre = 'Pregrado'),
     'Ingeniero de Sistemas'),

    ((SELECT id FROM funcionario WHERE numero_identificacion = '1017234501'),
     'Universidad EAFIT',
     (SELECT id FROM nivel_estudio WHERE nombre = 'Especialización'),
     'Especialista en Gerencia de Proyectos'),

    ((SELECT id FROM funcionario WHERE numero_identificacion = '43875612'),
     'Universidad Nacional de Colombia',
     (SELECT id FROM nivel_estudio WHERE nombre = 'Maestría'),
     'Magíster en Administración'),

    ((SELECT id FROM funcionario WHERE numero_identificacion = '1036789045'),
     'SENA',
     (SELECT id FROM nivel_estudio WHERE nombre = 'Tecnólogo'),
     'Tecnólogo en Análisis y Desarrollo de Software');

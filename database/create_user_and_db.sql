-- Entrar en psql como administrador
    --
--  Crear usuario
CREATE USER responsable_carbon_audit WITH PASSWORD '1234';

-- Crear la base de datos para el programa
CREATE DATABASE carbon_audit_test
WITH OWNER responsable_carbon_audit --equivale a que el usuario la hubiera creado
ENCODING 'UTF8'
LC_COLLATE='es_ES.UTF-8'
LC_CTYPE='es_ES.UTF-8'
TEMPLATE template0;

-- Conceder todos los privilegios sobre la base recién creada
GRANT ALL PRIVILEGES ON DATABASE carbon_audit_test TO responsable_carbon_audit;
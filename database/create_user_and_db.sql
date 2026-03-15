-- Entrar en psql como administrador 
--  Crear usuario
CREATE USER carbon_audit_responsable WITH PASSWORD '';

-- Crear la base de datos para el programa
CREATE DATABASE carbon_audit
WITH OWNER carbon_audit_responsable
ENCODING 'UTF8'
LC_COLLATE='es_ES.UTF-8'
LC_CTYPE='es_ES.UTF-8'
TEMPLATE template0;

-- Conceder todos los privilegios sobre la base recién creada
GRANT ALL PRIVILEGES ON DATABASE carbon_audit TO carbon_audit_responsable;
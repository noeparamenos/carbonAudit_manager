-- Eliminar tablas si existen (orden correcto por FK)
DROP TABLE IF EXISTS COMMUTING_EMPLEADO CASCADE;
DROP TABLE IF EXISTS CONSUMO_MENSUAL CASCADE;
DROP TABLE IF EXISTS RESPONSABLE CASCADE;
DROP TABLE IF EXISTS EMPLEADO CASCADE;
DROP TABLE IF EXISTS DEPARTAMENTO CASCADE;
DROP TABLE IF EXISTS FACTORES_EMISION CASCADE;
DROP TABLE IF EXISTS EMPRESA CASCADE;
DROP TABLE IF EXISTS DIRECCION CASCADE;

-- Creación de Tablas

CREATE TABLE DIRECCION
(
    id_direccion  SERIAL PRIMARY KEY,
    calle         VARCHAR(200) NOT NULL,
    ciudad        VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(10)  NOT NULL,
    provincia     VARCHAR(100),
    latitud       DECIMAL(10, 8),
    longitud      DECIMAL(11, 8)
);


CREATE TABLE EMPRESA
(
    id_empresa    SERIAL PRIMARY KEY,
    nombre_social VARCHAR(100) UNIQUE NOT NULL,
    cif           VARCHAR(15) UNIQUE  NOT NULL,
    telefono      VARCHAR(20),
    email         VARCHAR(100),
    sector        VARCHAR(50),
    id_direccion  INT REFERENCES DIRECCION (id_direccion)
);


CREATE TABLE DEPARTAMENTO
(
    id_departamento  SERIAL PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    descripcion      TEXT,
    incluir_alcance3 BOOLEAN DEFAULT TRUE,
    id_direccion     INT          NOT NULL REFERENCES DIRECCION (id_direccion),
    id_empresa       INT          NOT NULL REFERENCES EMPRESA (id_empresa),
    CONSTRAINT depto_unique_empresa_nombre UNIQUE (id_empresa, nombre)
);

CREATE TABLE FACTORES_EMISION
(
    id_factor    SERIAL PRIMARY KEY,
    nombre       VARCHAR(100)   NOT NULL,
    unidad       VARCHAR(20)    NOT NULL,
    valor_factor DECIMAL(10, 5) NOT NULL,
    alcance      INT            NOT NULL CHECK (alcance IN (1, 2, 3)),
    CONSTRAINT factor_unique_nombre_unidad UNIQUE (nombre, unidad)
);

CREATE TABLE EMPLEADO
(
    id_empleado       SERIAL PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    distancia_trabajo DECIMAL(6, 2),
    medio_transporte  INT          NOT NULL REFERENCES FACTORES_EMISION (id_factor),
    dias_presenciales INT          NOT NULL DEFAULT 20,
    id_direccion      INT          NOT NULL REFERENCES DIRECCION (id_direccion),
    id_dept           INT          NOT NULL REFERENCES DEPARTAMENTO (id_departamento)
);


CREATE TABLE RESPONSABLE
(
    id_asignacion SERIAL PRIMARY KEY,
    fecha_inicio  DATE NOT NULL,
    fecha_fin     DATE,
    id_dept       INT  NOT NULL REFERENCES DEPARTAMENTO (id_departamento),
    id_empleado   INT  NOT NULL REFERENCES EMPLEADO (id_empleado),
    CONSTRAINT responsable_unico_por_dept UNIQUE (id_dept, fecha_fin)
);


CREATE TABLE CONSUMO_MENSUAL
(
    id_consumo SERIAL PRIMARY KEY,
    cantidad   DECIMAL(12, 2) NOT NULL,
    mes        INT            NOT NULL CHECK (mes BETWEEN 1 AND 12),
    anio       INT            NOT NULL CHECK (anio >= 1950),
    id_dept    INT            NOT NULL REFERENCES DEPARTAMENTO (id_departamento),
    id_factor  INT            NOT NULL REFERENCES FACTORES_EMISION (id_factor),
    CONSTRAINT consumo_unico UNIQUE (id_dept, id_factor, mes, anio)
);


CREATE TABLE COMMUTING_EMPLEADO
(
    id_empleado           INT           NOT NULL REFERENCES EMPLEADO (id_empleado),
    id_factor             INT           NOT NULL REFERENCES FACTORES_EMISION (id_factor),
    distancia_diaria_km   DECIMAL(6, 2) NOT NULL,
    dias_presenciales_mes INT           NOT NULL,
    mes                   INT           NOT NULL CHECK (mes BETWEEN 1 AND 12),
    anio                  INT           NOT NULL CHECK (anio >= 1950),
    PRIMARY KEY (id_empleado, mes, anio)
);

-- ÍNDICES
-- para mejorar consultas frecuentes
CREATE INDEX idx_consumo_anio_mes ON CONSUMO_MENSUAL (anio, mes);
CREATE INDEX idx_consumo_id_dept ON CONSUMO_MENSUAL (id_dept);

CREATE INDEX idx_commuting_id_empleado ON COMMUTING_EMPLEADO (id_empleado);
CREATE INDEX idx_commuting_anio_mes ON COMMUTING_EMPLEADO (anio, mes);
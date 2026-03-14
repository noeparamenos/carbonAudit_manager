-- 1. Creación de la base de datos (Ejecutar por separado si es necesario)
-- CREATE DATABASE carbon_audit_db;

-- 2. Tabla Maestra de Factores de Emisión 
-- Contiene los coeficientes oficiales (Alcances 1, 2 y 3)
CREATE TABLE factores_emision (
    id_factor SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    unidad VARCHAR(20) NOT NULL, -- Ej: kWh, Litros
    valor_factor DECIMAL(10,5) NOT NULL, -- kgCO2e por unidad
    alcance INT NOT NULL CHECK (alcance IN (1, 2, 3)) -- GHG Protocol
);

-- 3. Entidad Principal: Empresa
CREATE TABLE empresa (
    id_empresa SERIAL PRIMARY KEY,
    nombre_social VARCHAR(100) NOT NULL,
    cif VARCHAR(15) UNIQUE NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    email VARCHAR(100),
    sector VARCHAR(50)
);

-- 4. Segmentación por Departamentos (RF-01)
CREATE TABLE departamento (
    id_departamento SERIAL PRIMARY KEY,
    id_empresa INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    direccion_completa VARCHAR(255),
    incluir_alcance3 BOOLEAN DEFAULT TRUE, -- Las leyes actuales no demandan incluir el alcance3 (esta previsto un cambio proximo)
    CONSTRAINT fk_empresa FOREIGN KEY (id_empresa) 
        REFERENCES empresa(id_empresa) ON DELETE CASCADE
);

-- 5. Gestión de Empleados
CREATE TABLE empleados (
    id_empleado SERIAL PRIMARY KEY,
    id_dept INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    direccion_residencia VARCHAR(255),
    distancia_trabajo_km DECIMAL(6,2),
    CONSTRAINT fk_departamento FOREIGN KEY (id_dept) 
        REFERENCES departamento(id_departamento) ON DELETE CASCADE
);

-- 6. Trazabilidad de Responsables (Cumplimiento CSRD)
CREATE TABLE responsables (
    id_asignacion SERIAL PRIMARY KEY,
    id_dept INT NOT NULL,
    id_empleado INT NOT NULL,
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin DATE,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_responsable_dept FOREIGN KEY (id_dept) 
        REFERENCES departamento(id_departamento),
    CONSTRAINT fk_responsable_emp FOREIGN KEY (id_empleado) 
        REFERENCES empleados(id_empleado)
);

-- 7. Registro de Consumos Mensuales (Core del cálculo)
CREATE TABLE consumos_mensuales (
    id_consumo SERIAL PRIMARY KEY,
    id_dept INT NOT NULL,
    id_factor INT NOT NULL,
    cantidad DECIMAL(12,2) NOT NULL,
    mes INT NOT NULL CHECK (mes BETWEEN 1 AND 12),
    anio INT NOT NULL,
    CONSTRAINT fk_consumo_dept FOREIGN KEY (id_dept) 
        REFERENCES departamento(id_departamento),
    CONSTRAINT fk_consumo_factor FOREIGN KEY (id_factor) 
        REFERENCES factores_emision(id_factor)
);

-- 8. Cálculo de Commuting (Alcance 3)
CREATE TABLE commuting_empleados (
    id_transporte SERIAL PRIMARY KEY,
    id_empleado INT NOT NULL,
    id_factor INT NOT NULL, -- Vincula al factor de transporte (ej. coche diésel)
    distancia_diaria_km DECIMAL(6,2) NOT NULL,
    dias_presenciales_mes INT NOT NULL DEFAULT 20,
    CONSTRAINT fk_commuting_emp FOREIGN KEY (id_empleado) 
        REFERENCES empleados(id_empleado) ON DELETE CASCADE,
    CONSTRAINT fk_commuting_factor FOREIGN KEY (id_factor) 
        REFERENCES factores_emision(id_factor)
);
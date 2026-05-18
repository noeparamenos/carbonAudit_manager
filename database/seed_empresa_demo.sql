-- =============================================================================
-- seed_empresa_demo.sql
-- Datos de demostración: empresa, departamentos, empleados, responsables
-- y consumos mensuales de todo el año 2024.
--
-- Empresa ficticia: Talleres Renovables Ibérica, S.L.
--   · Departamento "Operaciones"   — 4 empleados, taller y logística en Alcobendas
--   · Departamento "Administración" — 3 empleados, oficinas en Madrid
--
-- Prerequisito: seed_factores_emision.sql debe haberse ejecutado antes.
--
-- Ejecución: psql -U carbonaudit_user -d carbonaudit_db -f seed_empresa_demo.sql
-- Nota: el script aborta sin cambios si la empresa ya existe (CIF duplicado).
-- =============================================================================

DO $$
DECLARE
    -- Factores de emisión (cargados del seed de factores)
    v_f_elect        INT;   -- Electricidad red española (kWh, alcance 2)
    v_f_gas          INT;   -- Gas natural             (m³,  alcance 1)
    v_f_diesel_veh   INT;   -- Diésel (vehículo)       (litros, alcance 1)
    v_f_coche_gas    INT;   -- Coche gasolina          (km, alcance 3, commuting)
    v_f_coche_dies   INT;   -- Coche diésel            (km, alcance 3, commuting)
    v_f_coche_hibr   INT;   -- Coche híbrido           (km, alcance 3, commuting)
    v_f_tren         INT;   -- Tren / metro            (km, alcance 3, commuting)
    v_f_autobus      INT;   -- Autobús urbano          (km, alcance 3, commuting)
    v_f_bici         INT;   -- Bicicleta / a pie       (km, alcance 3, commuting)

    -- Direcciones
    v_dir_empresa    INT;
    v_dir_dept_ops   INT;
    v_dir_dept_adm   INT;
    -- Empleados Operaciones
    v_dir_e1_ops     INT;   -- Luis García
    v_dir_e2_ops     INT;   -- María Rodríguez
    v_dir_e3_ops     INT;   -- Carlos Pérez
    v_dir_e4_ops     INT;   -- Ana Jiménez
    -- Empleados Administración
    v_dir_e1_adm     INT;   -- Elena Castro
    v_dir_e2_adm     INT;   -- Javier Moreno
    v_dir_e3_adm     INT;   -- Sofía Fernández

    -- Entidades principales
    v_empresa        INT;
    v_dept_ops       INT;
    v_dept_adm       INT;

    -- IDs de empleados
    v_emp1_ops       INT;   -- Luis García    (responsable Operaciones)
    v_emp2_ops       INT;   -- María Rodríguez
    v_emp3_ops       INT;   -- Carlos Pérez
    v_emp4_ops       INT;   -- Ana Jiménez
    v_emp1_adm       INT;   -- Elena Castro   (responsable Administración)
    v_emp2_adm       INT;   -- Javier Moreno
    v_emp3_adm       INT;   -- Sofía Fernández

BEGIN

    -- ── Guardia: abortar si la empresa ya existe ──────────────────────────────
    IF EXISTS (SELECT 1 FROM EMPRESA WHERE cif = 'B12345678') THEN
        RAISE NOTICE 'El seed ya fue ejecutado (CIF B12345678 ya existe). Sin cambios.';
        RETURN;
    END IF;

    -- ── Resolución de factores de emisión ─────────────────────────────────────
    SELECT id_factor INTO v_f_elect      FROM FACTOR_EMISION WHERE nombre = 'Electricidad red española';
    SELECT id_factor INTO v_f_gas        FROM FACTOR_EMISION WHERE nombre = 'Gas natural';
    SELECT id_factor INTO v_f_diesel_veh FROM FACTOR_EMISION WHERE nombre = 'Diésel (vehículo)';
    SELECT id_factor INTO v_f_coche_gas  FROM FACTOR_EMISION WHERE nombre = 'Coche gasolina';
    SELECT id_factor INTO v_f_coche_dies FROM FACTOR_EMISION WHERE nombre = 'Coche diésel';
    SELECT id_factor INTO v_f_coche_hibr FROM FACTOR_EMISION WHERE nombre = 'Coche híbrido';
    SELECT id_factor INTO v_f_tren       FROM FACTOR_EMISION WHERE nombre = 'Tren / metro';
    SELECT id_factor INTO v_f_autobus    FROM FACTOR_EMISION WHERE nombre = 'Autobús urbano';
    SELECT id_factor INTO v_f_bici       FROM FACTOR_EMISION WHERE nombre = 'Bicicleta / a pie';

    IF v_f_elect IS NULL THEN
        RAISE EXCEPTION 'Factores de emisión no encontrados. Ejecuta seed_factores_emision.sql primero.';
    END IF;

    -- ══════════════════════════════════════════════════════════════════════════
    -- EMPRESA
    -- ══════════════════════════════════════════════════════════════════════════

    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Gran Vía', 45, 'Madrid', '28013', 'Madrid')
    RETURNING id_direccion INTO v_dir_empresa;

    INSERT INTO EMPRESA (nombre_social, cif, telefono, email, sector, id_direccion)
    VALUES ('Talleres Renovables Ibérica, S.L.', 'B12345678',
            '915550100', 'info@trisl.es', 'Energía', v_dir_empresa)
    RETURNING id_empresa INTO v_empresa;

    -- ══════════════════════════════════════════════════════════════════════════
    -- DEPARTAMENTO: Operaciones
    -- Taller de instalación y flota de vehículos. Alcance 3 habilitado.
    -- ══════════════════════════════════════════════════════════════════════════

    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Calle Industria', 12, 'Alcobendas', '28100', 'Madrid')
    RETURNING id_direccion INTO v_dir_dept_ops;

    INSERT INTO DEPARTAMENTO (nombre, descripcion, incluir_alcance3, id_direccion, id_empresa)
    VALUES ('Operaciones', 'Taller, instalaciones y logística', TRUE, v_dir_dept_ops, v_empresa)
    RETURNING id_departamento INTO v_dept_ops;

    -- ── Empleados Operaciones ─────────────────────────────────────────────────

    -- Luis García Martínez · Coche gasolina · 14,8 km · 20 días/mes
    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Calle Alcalá', 120, 'Madrid', '28009', 'Madrid')
    RETURNING id_direccion INTO v_dir_e1_ops;

    INSERT INTO EMPLEADO (nombre, fecha_alta, distancia_trabajo, medio_transporte, dias_presenciales, id_direccion, id_dept)
    VALUES ('Luis García Martínez', '2022-03-01', 14.80, v_f_coche_gas, 20, v_dir_e1_ops, v_dept_ops)
    RETURNING id_empleado INTO v_emp1_ops;

    -- María Rodríguez López · Tren / metro · 23,5 km · 20 días/mes
    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Calle Fuencarral', 78, 'Madrid', '28004', 'Madrid')
    RETURNING id_direccion INTO v_dir_e2_ops;

    INSERT INTO EMPLEADO (nombre, fecha_alta, distancia_trabajo, medio_transporte, dias_presenciales, id_direccion, id_dept)
    VALUES ('María Rodríguez López', '2021-09-15', 23.50, v_f_tren, 20, v_dir_e2_ops, v_dept_ops)
    RETURNING id_empleado INTO v_emp2_ops;

    -- Carlos Pérez Sánchez · Coche diésel · 6,3 km · 22 días/mes
    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Avenida de la Constitución', 5, 'Alcobendas', '28100', 'Madrid')
    RETURNING id_direccion INTO v_dir_e3_ops;

    INSERT INTO EMPLEADO (nombre, fecha_alta, distancia_trabajo, medio_transporte, dias_presenciales, id_direccion, id_dept)
    VALUES ('Carlos Pérez Sánchez', '2020-06-01', 6.30, v_f_coche_dies, 22, v_dir_e3_ops, v_dept_ops)
    RETURNING id_empleado INTO v_emp3_ops;

    -- Ana Jiménez Torres · Bicicleta / a pie · 2,1 km · 20 días/mes
    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Calle Mayor', 3, 'Alcobendas', '28100', 'Madrid')
    RETURNING id_direccion INTO v_dir_e4_ops;

    INSERT INTO EMPLEADO (nombre, fecha_alta, distancia_trabajo, medio_transporte, dias_presenciales, id_direccion, id_dept)
    VALUES ('Ana Jiménez Torres', '2023-01-10', 2.10, v_f_bici, 20, v_dir_e4_ops, v_dept_ops)
    RETURNING id_empleado INTO v_emp4_ops;

    -- ══════════════════════════════════════════════════════════════════════════
    -- DEPARTAMENTO: Administración
    -- Gestión, contabilidad y RRHH. Alcance 3 habilitado.
    -- ══════════════════════════════════════════════════════════════════════════

    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Gran Vía', 45, 'Madrid', '28013', 'Madrid')
    RETURNING id_direccion INTO v_dir_dept_adm;

    INSERT INTO DEPARTAMENTO (nombre, descripcion, incluir_alcance3, id_direccion, id_empresa)
    VALUES ('Administración', 'Gestión, contabilidad y RRHH', TRUE, v_dir_dept_adm, v_empresa)
    RETURNING id_departamento INTO v_dept_adm;

    -- ── Empleados Administración ──────────────────────────────────────────────

    -- Elena Castro Ruiz · Tren / metro · 17,2 km · 20 días/mes
    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Calle Serrano', 210, 'Madrid', '28016', 'Madrid')
    RETURNING id_direccion INTO v_dir_e1_adm;

    INSERT INTO EMPLEADO (nombre, fecha_alta, distancia_trabajo, medio_transporte, dias_presenciales, id_direccion, id_dept)
    VALUES ('Elena Castro Ruiz', '2019-04-01', 17.20, v_f_tren, 20, v_dir_e1_adm, v_dept_adm)
    RETURNING id_empleado INTO v_emp1_adm;

    -- Javier Moreno Díaz · Coche híbrido · 19,8 km · 18 días/mes
    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Avenida de América', 55, 'Madrid', '28028', 'Madrid')
    RETURNING id_direccion INTO v_dir_e2_adm;

    INSERT INTO EMPLEADO (nombre, fecha_alta, distancia_trabajo, medio_transporte, dias_presenciales, id_direccion, id_dept)
    VALUES ('Javier Moreno Díaz', '2021-02-15', 19.80, v_f_coche_hibr, 18, v_dir_e2_adm, v_dept_adm)
    RETURNING id_empleado INTO v_emp2_adm;

    -- Sofía Fernández Gil · Autobús urbano · 11,5 km · 20 días/mes
    INSERT INTO DIRECCION (calle, numero, ciudad, codigo_postal, provincia)
    VALUES ('Calle Princesa', 33, 'Madrid', '28008', 'Madrid')
    RETURNING id_direccion INTO v_dir_e3_adm;

    INSERT INTO EMPLEADO (nombre, fecha_alta, distancia_trabajo, medio_transporte, dias_presenciales, id_direccion, id_dept)
    VALUES ('Sofía Fernández Gil', '2022-11-01', 11.50, v_f_autobus, 20, v_dir_e3_adm, v_dept_adm)
    RETURNING id_empleado INTO v_emp3_adm;

    -- ══════════════════════════════════════════════════════════════════════════
    -- RESPONSABLES (mandatos activos desde 01/01/2024)
    -- ══════════════════════════════════════════════════════════════════════════

    INSERT INTO RESPONSABLE (fecha_inicio, fecha_fin, id_dept, id_empleado)
    VALUES ('2024-01-01', NULL, v_dept_ops, v_emp1_ops);   -- Luis García → Operaciones

    INSERT INTO RESPONSABLE (fecha_inicio, fecha_fin, id_dept, id_empleado)
    VALUES ('2024-01-01', NULL, v_dept_adm, v_emp1_adm);   -- Elena Castro → Administración

    -- ══════════════════════════════════════════════════════════════════════════
    -- CONSUMOS MENSUALES 2024
    -- ══════════════════════════════════════════════════════════════════════════
    --
    -- Operaciones:
    --   · Electricidad  — taller + oficinas del polígono (patrón estacional)
    --   · Gas natural   — calefacción taller           (pico invierno)
    --   · Diésel vehículo — flota de furgonetas        (baja en agosto)
    --
    -- Administración:
    --   · Electricidad  — oficinas Madrid              (patrón estacional suavizado)
    --   · Gas natural   — calefacción oficinas         (solo meses fríos)
    --

    -- ── Operaciones · Electricidad (kWh) ──────────────────────────────────────
    INSERT INTO CONSUMO_MENSUAL (cantidad, mes, anio, id_dept, id_factor) VALUES
        (2200.00,  1, 2024, v_dept_ops, v_f_elect),
        (2100.00,  2, 2024, v_dept_ops, v_f_elect),
        (1800.00,  3, 2024, v_dept_ops, v_f_elect),
        (1600.00,  4, 2024, v_dept_ops, v_f_elect),
        (1500.00,  5, 2024, v_dept_ops, v_f_elect),
        (1800.00,  6, 2024, v_dept_ops, v_f_elect),
        (2100.00,  7, 2024, v_dept_ops, v_f_elect),
        (1200.00,  8, 2024, v_dept_ops, v_f_elect),
        (1600.00,  9, 2024, v_dept_ops, v_f_elect),
        (1700.00, 10, 2024, v_dept_ops, v_f_elect),
        (2000.00, 11, 2024, v_dept_ops, v_f_elect),
        (2300.00, 12, 2024, v_dept_ops, v_f_elect);

    -- ── Operaciones · Gas natural (m³) ────────────────────────────────────────
    INSERT INTO CONSUMO_MENSUAL (cantidad, mes, anio, id_dept, id_factor) VALUES
        (750.00,  1, 2024, v_dept_ops, v_f_gas),
        (680.00,  2, 2024, v_dept_ops, v_f_gas),
        (450.00,  3, 2024, v_dept_ops, v_f_gas),
        (200.00,  4, 2024, v_dept_ops, v_f_gas),
        ( 80.00,  5, 2024, v_dept_ops, v_f_gas),
        ( 40.00,  6, 2024, v_dept_ops, v_f_gas),
        ( 30.00,  7, 2024, v_dept_ops, v_f_gas),
        ( 20.00,  8, 2024, v_dept_ops, v_f_gas),
        ( 60.00,  9, 2024, v_dept_ops, v_f_gas),
        (200.00, 10, 2024, v_dept_ops, v_f_gas),
        (500.00, 11, 2024, v_dept_ops, v_f_gas),
        (720.00, 12, 2024, v_dept_ops, v_f_gas);

    -- ── Operaciones · Diésel vehículo — flota furgonetas (litros) ────────────
    INSERT INTO CONSUMO_MENSUAL (cantidad, mes, anio, id_dept, id_factor) VALUES
        (380.00,  1, 2024, v_dept_ops, v_f_diesel_veh),
        (360.00,  2, 2024, v_dept_ops, v_f_diesel_veh),
        (420.00,  3, 2024, v_dept_ops, v_f_diesel_veh),
        (450.00,  4, 2024, v_dept_ops, v_f_diesel_veh),
        (480.00,  5, 2024, v_dept_ops, v_f_diesel_veh),
        (460.00,  6, 2024, v_dept_ops, v_f_diesel_veh),
        (350.00,  7, 2024, v_dept_ops, v_f_diesel_veh),
        (200.00,  8, 2024, v_dept_ops, v_f_diesel_veh),
        (430.00,  9, 2024, v_dept_ops, v_f_diesel_veh),
        (460.00, 10, 2024, v_dept_ops, v_f_diesel_veh),
        (400.00, 11, 2024, v_dept_ops, v_f_diesel_veh),
        (340.00, 12, 2024, v_dept_ops, v_f_diesel_veh);

    -- ── Administración · Electricidad (kWh) ───────────────────────────────────
    INSERT INTO CONSUMO_MENSUAL (cantidad, mes, anio, id_dept, id_factor) VALUES
        (1200.00,  1, 2024, v_dept_adm, v_f_elect),
        (1150.00,  2, 2024, v_dept_adm, v_f_elect),
        ( 950.00,  3, 2024, v_dept_adm, v_f_elect),
        ( 850.00,  4, 2024, v_dept_adm, v_f_elect),
        ( 800.00,  5, 2024, v_dept_adm, v_f_elect),
        ( 950.00,  6, 2024, v_dept_adm, v_f_elect),
        (1100.00,  7, 2024, v_dept_adm, v_f_elect),
        ( 500.00,  8, 2024, v_dept_adm, v_f_elect),
        ( 850.00,  9, 2024, v_dept_adm, v_f_elect),
        ( 900.00, 10, 2024, v_dept_adm, v_f_elect),
        (1050.00, 11, 2024, v_dept_adm, v_f_elect),
        (1200.00, 12, 2024, v_dept_adm, v_f_elect);

    -- ── Administración · Gas natural — calefacción (m³) ──────────────────────
    --    Sin consumo en junio, julio y agosto (oficinas cerradas o sin calefacción)
    INSERT INTO CONSUMO_MENSUAL (cantidad, mes, anio, id_dept, id_factor) VALUES
        (380.00,  1, 2024, v_dept_adm, v_f_gas),
        (340.00,  2, 2024, v_dept_adm, v_f_gas),
        (220.00,  3, 2024, v_dept_adm, v_f_gas),
        ( 80.00,  4, 2024, v_dept_adm, v_f_gas),
        ( 30.00,  5, 2024, v_dept_adm, v_f_gas),
        ( 30.00,  9, 2024, v_dept_adm, v_f_gas),
        (100.00, 10, 2024, v_dept_adm, v_f_gas),
        (260.00, 11, 2024, v_dept_adm, v_f_gas),
        (360.00, 12, 2024, v_dept_adm, v_f_gas);

    RAISE NOTICE 'Seed completado: empresa "Talleres Renovables Ibérica, S.L." creada con 2 departamentos, 7 empleados y consumos 2024.';

END $$;
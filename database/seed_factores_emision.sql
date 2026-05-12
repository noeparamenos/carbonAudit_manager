-- =============================================================================
-- seed_factores_emision.sql
-- Datos de referencia iniciales para la tabla FACTOR_EMISION.
--
-- Fuentes:
--   · IDAE (Instituto para la Diversificación y Ahorro de la Energía) - 2023
--   · MITECO (Ministerio para la Transición Ecológica) - factores GHG España
--   · DEFRA (UK) - factores de transporte por carretera
--
-- Unidades:
--   · Alcance 1 y 2: valor_factor = kg CO₂e emitidos por 1 unidad del recurso
--   · Alcance 3:     valor_factor = kg CO₂e emitidos por 1 km recorrido
--
-- Ejecución: psql -U carbonaudit_user -d carbonaudit_db -f seed_factores_emision.sql
-- =============================================================================

-- Evitar duplicados si se re-ejecuta el script
INSERT INTO FACTOR_EMISION (nombre, unidad, valor_factor, alcance) VALUES

-- ── ALCANCE 1 · Combustión directa ──────────────────────────────────────────
-- Fuente: MITECO - Factores de emisión 2023
    ('Gas natural',               'm³',     2.04000, 1),
    ('Gasóleo calefacción (C)',   'litros', 2.68400, 1),
    ('GLP (propano/butano)',      'litros', 1.74900, 1),
    ('Gasolina (vehículo)',       'litros', 2.27200, 1),
    ('Diésel (vehículo)',         'litros', 2.63900, 1),
    ('Carbón',                    'kg',     2.34000, 1),

-- ── ALCANCE 2 · Energía indirecta ───────────────────────────────────────────
-- Fuente: IDAE - Factor de emisión electricidad red española 2023
    ('Electricidad red española', 'kWh',    0.18500, 2),
    ('Electricidad renovable 0',  'kWh',    0.00000, 2),

-- ── ALCANCE 3 · Transporte y movilidad (commuting empleados) ────────────────
-- Fuente: DEFRA 2023 - passenger transport per km
    ('Coche gasolina',            'km',     0.19200, 3),
    ('Coche diésel',              'km',     0.17100, 3),
    ('Coche híbrido',             'km',     0.10900, 3),
    ('Coche eléctrico',           'km',     0.05300, 3),
    ('Moto / ciclomotor',         'km',     0.11300, 3),
    ('Autobús urbano',            'km',     0.08900, 3),
    ('Tren / metro',              'km',     0.04100, 3),
    ('Bicicleta / a pie',         'km',     0.00000, 3),
    ('Teletrabajo',               'km',     0.00000, 3)

ON CONFLICT (nombre, unidad) DO NOTHING;
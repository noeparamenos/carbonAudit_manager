# CarbonAudit Manager – Arquitectura del Sistema

## Objetivo del sistema
CarbonAudit Manager es una aplicación de escritorio diseñada para centralizar
el cálculo de la huella de carbono en PYMES.

## Stack Tecnológico

- Lenguaje: Java 21
- Base de datos: PostgreSQL
- Arquitectura: DAO
- Build: Maven

## Capas de la aplicación

### 1. Capa de Persistencia
Implementada mediante el patrón DAO para acceder a PostgreSQL.

### 2. Capa de Negocio
Contiene la lógica de cálculo de emisiones:

Emisiones = Cantidad * Factor_emision

### 3. Capa de Presentación
Interfaz de escritorio donde los usuarios introducen consumos,
gestionan departamentos y consultan el dashboard.

## Modelo de emisiones

El sistema calcula emisiones según el estándar:

- Scope 1 → emisiones directas
- Scope 2 → electricidad comprada
- Scope 3 → emisiones indirectas
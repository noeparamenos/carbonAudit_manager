# Roadmap de Desarrollo: CarbonAudit Manager

Este documento detalla la hoja de ruta para el desarrollo de la aplicación.

## Fase 1: Cimentación y Modelo de Datos (Marzo)
**Enfoque**: Diseño relacional y estructuras Java (POO)

* **Diseño del Modelo Entidad-Relación (E-R):**
    * Crear tablas en **PostgreSQL** para: `Empresa`, `Departamentos` (RF-01), `FactoresEmision` (RF-05) y `Consumos` (RF-02).
    * Establecer relaciones de integridad referencial entre consumos y departamentos.
* **Configuración del Entorno de Desarrollo:**
    * Inicializar proyecto con **Maven** en **IntelliJ IDEA**.
    * Configurar dependencias: Driver JDBC de PostgreSQL y módulos de **JavaFX 21**.
* **Mapeo de Objetos (Backend):**
    * Crear clases POJO en **Java 21** que reflejen la estructura de la base de datos.
    * Configurar el sistema de control de versiones con **Git**.

---

## Fase 2: Capa de Persistencia y Lógica de Negocio (Abril)
***Enfoque**: Implementación del motor de cálculo y arquitectura DAO.

* **Arquitectura de Datos (DAO):**
    * Implementar el patrón **Data Access Object** para separar la lógica de negocio del acceso a la base de datos.
    * Establecer la conexión mediante Hibernate para la persistencia de datos.
* **Desarrollo del Motor de Cálculo (Core):**
    * Programar la lógica para automatizar el cálculo de $kgCO_2e$ basado en factores oficiales (RF-03).
    * Desarrollar el módulo de **Commuting** para registrar desplazamientos de empleados (RF-04).
* **Validación de Datos:**
    * Implementar pruebas para asegurar la precisión en los cálculos de transporte por distancia y tipo de combustible (RNF-07).

---

## Fase 3: Interfaz de Usuario y Dashboard (Mayo)
**Enfoque**: Creación de la UI con JavaFX y visualización de resultados.

* **Diseño Frontend:**
    * Diseñar pantallas (`.fxml`) utilizando **Scene Builder**.
    * Crear formularios de entrada de datos para consumos de energía y desplazamientos.
* **Visualización de Impacto:**
    * Desarrollar un **Dashboard** gráfico que muestre el desglose de emisiones por departamentos (RF-06).
    * Asegurar la visualización dinámica de los datos procesados en el Backend[cite: 15].
* **Funcionalidades de Reporte:**
    * Implementar la generación de resúmenes de datos para facilitar la toma de decisiones y el cumplimiento legal (RF-07).

---

## Fase 4: Pruebas Finales y Documentación (Junio)
*Enfoque: Refinado del software y preparación del proyecto final.*

* **QA y Debugging:**
    * Pruebas de compatibilidad en el sistema operativo Linux.
    * Optimización de consultas SQL en PostgreSQL.
* **Documentación Final:**
    * Finalizar la memoria del proyecto detallando la arquitectura y el cumplimiento de requerimientos.
# Especificación de Requisitos: CarbonAudit Manager

## 1. Introducción
**CarbonAudit Manager** es una aplicación de escritorio diseñada para centralizar y automatizar el cálculo de la huella de carbono en PYMES. 
El sistema permite a las organizaciones cumplir con las normativas de sostenibilidad europeas mediante el seguimiento detallado de focos de emisión.

## 2. Requerimientos Funcionales (RF)

### 2.1. Gestión Organizacional
* **RF-01 (Jerarquía de Departamentos):** El sistema debe permitir la creación, edición y eliminación de múltiples departamentos dentro de una misma empresa.
* **RF-02 (Asignación de Consumos):** Cada registro de consumo de energía (electricidad, gas, combustibles) debe estar vinculado a un departamento específico para permitir la segmentación del impacto.

### 2.2. Cálculo de Huella de Carbono (Core)
* **RF-03 (Motor de Cálculo):** Automatización del cálculo de kgCO2e basándose en factores de emisión oficiales.
* **RF-04 (Módulo de Transporte/Commuting):** Registro de desplazamientos de empleados al centro de trabajo, permitiendo seleccionar diversos métodos de transporte.
* **RF-05 (Base de Factores de Emisión):** Gestión de una tabla maestra de factores de emisión actualizables según el marco legal vigente.

### 2.3. Visualización y Reportes
* **RF-06 (Dashboard de Impacto):** Visualización gráfica de la huella total y desglose por departamentos mediante JavaFX.
* **RF-07 (Toma de Decisiones):** Generación de resúmenes de datos que faciliten el establecimiento de objetivos de reducción de emisiones.

## 3. Requerimientos No Funcionales (RNF)

### 3.1. Arquitectura y Desarrollo
* **RNF-01 (Patrón de Diseño):** Implementación obligatoria de la arquitectura **DAO (Data Access Object)** para la separación de la lógica de negocio y el acceso a datos.
* **RNF-02 (Persistencia):** Uso del SGBD **PostgreSQL** para garantizar la integridad referencial de los datos de consumo y departamentos.
* **RNF-03 (Tecnologías Core):** Desarrollo basado en **Java 21 (JDK)** y gestión de dependencias con **Maven**[cite: 15, 41].

### 3.2. Interfaz y Experiencia (UI/UX)
* **RNF-04 (Frontend):** Interfaz construida con **JavaFX 21 y FXML**, diseñada mediante **Scene Builder**.
* **RNF-05 (Entorno):** Compatibilidad para diferentes SO.

### 3.3. Metodología de Emisiones (Estándares)
* **RNF-06 (Modelo de Alcances):** Clasificación interna de consumos según el GHG Protocol (Alcances 1, 2 y 3).
* **RNF-07 (Precisión de Transporte):** El sistema debe soportar cálculos de transporte por distancia (km) y tipo de combustible para mayor precisión en el Alcance 3.

## 4. Stack Tecnológico
* **Lenguaje:** Java 21[cite: 15].
* **Base de Datos:** PostgreSQL[cite: 15].
* **IDE:** IntelliJ IDEA.
* **Control de Versiones:** Git. 
# Roadmap de Desarrollo: CarbonAudit Manager

Este documento detalla la hoja de ruta para el desarrollo de la aplicación.

## Fase 1: Modelo de Datos 
Diseño relacional y estructuras Java (POO)

- **Diseño del Modelo Entidad-Relación (E-R):**
  - [x] Creacion del diagrama E-R
  - [x] Establecer relaciones de integridad referencial entre las tablas
  - [x] Crear tablas en **PostgreSQL**
- **Configuración del Entorno de Desarrollo:**
  - [x] Inicializar proyecto con **Maven** en **IntelliJ IDEA**
  - [x] Configurar el sistema de control de versiones con **Git**.
  - [x] Crear las clases POJO
  - [x] Configurar dependencias: Driver JDBC de PostgreSQL y módulos de **JavaFX 21**.
- **Mapeo de Objetos (Backend):**
  - [x] Crear clases POJO  que reflejen la estructura de la base de datos. 

---

## Fase 2: Capa de Persistencia
Implementación del motor de cálculo y arquitectura DAO.

- **Arquitectura de Datos (DAO):**
  - [x] Implementar el patrón **Data Access Object** para separar la lógica de negocio del acceso a la base de datos.
    - [x] Crear una interfaz que sirva de patrón a seguir e indique que operaciones deben realizarse en la BD por cada una de las clases
    - [x] Implementar una clase DAO por cada una de las clases modelo con los métodos que realicen las operaciones deseadas en la bd
    - [X] Implementar comprobaciones de integridad en cada uno de los métodos para evitar fallos en las operacciones de la BD (sirve de última barrera)
  - [x] Comprobar el acceso a la BD (operaciones CRUD) desde la aplicación mediante una clase Test

---

## Fase 3: Lógica de Negocio
Desarrollo del Motor de Cálculo (Service) y la lógica de negocio
  - [x] Implementar una Interfaz que defina los metodos que deben tener los servicios de geolocalización 
    - (permite que se cambie el servicio en un futuro)
  - [x] Programar el uso de una **API externa OpenSource**  
    - [x] Crear una cuenta en **openrouteservice.org** y generar un Token (API Key)
    - [x] Crear el archivo .env para guardar la API key e implementar el uso de la libreria Dotenv
    - [x] Implementar la gestión de **peticiones y respuestas de la API** (usando GSON)
    - [x] Implementar un método para guardar la geolocalización de la dirección
    - [x] Implementar un método para calcular las distancias por carretera
    - [x] Implementar una clase test para comprobar el funcionamiento de la API
  - [ ] Programar el uso de la **API externa de Google**
    - [ ] Crear cuenta en la API de Google
    - [ ] Implementar los metodos para calcular la geolocalización de la dirección y el calculo de distancias
    - [ ] Implementar una clase test para comprobar el funcionamiento de la API
  - [ ] Implementar La lógica del calculo del Commuting
    - [x] Implementar metodo que calcule la **distancia al trabajo** del empelado usando el servicio externo
    - [x] Implementar un método que calcule el **impacto mensual del commuting** de un empleado 
    - [x] Implementar pruebas para asegurar la precisión en los cálculos de transporte por distancia y tipo de combustible.
  - [ ] Implementar la lógica de cálculo del cálculo de la huella 
    - [ ] Implementar un método que calcule la huella total mensual de un departamento basado en los factores de emision asignados.

---

## Fase 3: Interfaz de Usuario y Dashboard 
Creación de la UI con JavaFX y visualización de resultados.
  - [ ] Diseño Frontend
    - [ ] Diseñar pantallas (`.fxml`) utilizando **Scene Builder**.
    - [ ] Crear formularios de entrada de datos para consumos de energía y desplazamientos.
  - [ ] Visualización de Impacto:**
    - [ ] Desarrollar un **Dashboard** gráfico que muestre el desglose de emisiones por departamentos
    - [ ] Asegurar la visualización dinámica de los datos procesados en el Backend.
    - [ ] Implementar la generación de resúmenes de datos para facilitar la toma de decisiones.

---

## Fase 4: Pruebas Finales y Documentación
Refinado del software y preparación del proyecto final.
  - [ ] Pruebas de compatibilidad en el sistema operativo Linux.
  - [ ] Optimización de consultas SQL en PostgreSQL.
  - [ ] Finalizar la memoria del proyecto detallando la arquitectura y el cumplimiento de requerimientos.
  - [ ] Empaquetado para distribución 
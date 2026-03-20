# Roadmap de Desarrollo: CarbonAudit Manager

Este documento detalla la hoja de ruta para el desarrollo de la aplicación.

## Fase 1: Cimentación y Modelo de Datos (Marzo)
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
  - [x] Crear las clases DAO de acceso a la BD   
  - [x] Comprobar el acceso a la BD (operaciones CRUD) desde la aplicación

---

## Fase 2: Capa de Persistencia y Lógica de Negocio (Abril)
Implementación del motor de cálculo y arquitectura DAO.

- **Arquitectura de Datos (DAO):**
  - [x] Implementar el patrón **Data Access Object** para separar la lógica de negocio del acceso a la base de datos.
- **Desarrollo del Motor de Cálculo (Core):**
  - [ ] Programar el uso de una API externa de mapas para calcular distancias   
    - [ ] Crear una cuenta en openrouteservice.org y generar un Token (API Key)
    - [ ] Implementar una clase para guardar la geolocalización de la direccion
    - [ ] Implementar una clase para calcular las distancias por carretera


Tu HuellaCarbonoService transformará esos metros a kilómetros y aplicará el factor de emisión.
  - [] Programar la lógica para automatizar el cálculo de $kgCO_2e$ basado en factores oficiales.
  - [ ] Desarrollar el módulo de **Commuting** para registrar desplazamientos de empleados.
- **Validación de Datos:**
  - [ ] Implementar pruebas para asegurar la precisión en los cálculos de transporte por distancia y tipo de combustible.

2. Organización de Clases y Responsabilidades
   A. La Interfaz de Fachada (IServicioGeografico)
   Definiremos un contrato claro. El resto de tu aplicación no debe saber que usas ORS; solo debe saber que hay un servicio que "entiende" de mapas.

Método completarCoordenadas(Direccion d): Recibe el objeto Direccion, extrae la calle, ciudad y código postal, llama a la API y actualiza los campos latitud y longitud del mismo objeto.

Método calcularDistancia(Direccion origen, Direccion destino): Devuelve un BigDecimal con los kilómetros por carretera.

B. La Implementación Técnica (ServicioGeograficoORS)
Esta clase es la única que conoce la existencia de la API Key y los endpoints de OpenRouteService.

Carga de Configuración: Utilizará la librería Dotenv para leer la clave del archivo .env que planeaste.

Cliente HTTP: Usará el HttpClient de Java 21 para enviar las peticiones.

Parseo JSON: Usará Gson para extraer las coordenadas del JSON de respuesta y mapearlas a tus campos DECIMAL.

C. Orquestación en el HuellaCarbonoService
Esta es la clase "maestra". Su función es coordinar a los DAOs y al servicio geográfico:

Para el Registro: Cuando se crea un Empleado o una Empresa, este servicio llama a IServicioGeografico para asegurar que la Direccion tenga coordenadas antes de que el DireccionDAO la guarde en la base de datos.

Para el Cálculo de Emisiones: Para calcular el commuting, el servicio pedirá la distancia entre la dirección del empleado y la del departamento, y luego multiplicará esos km por el valor_factor de la tabla FACTORES_EMISION.

3. El Flujo de Datos (Workflow)
   El planteamiento de ejecución debe seguir este orden para mantener la integridad:

Entrada: El usuario introduce datos en la UI (JavaFX).

Validación de Infraestructura: El ServicioGeograficoORS valida que la dirección existe y obtiene las coordenadas.

Persistencia: Si hay coordenadas válidas, se llama al DireccionDAO para hacer el INSERT en PostgreSQL.

Cálculo: El HuellaCarbonoService utiliza esas coordenadas persistidas para calcular rutas precisas en cualquier momento, cumpliendo con los requisitos de auditoría de precisión técnica que definiste.
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
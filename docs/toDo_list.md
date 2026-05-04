# Roadmap de Desarrollo: CarbonAudit Manager

Este documento detalla la hoja de ruta para el desarrollo de la aplicación.

## Fase 0: Análisis y Diseño
Modelado previo del sistema antes de entrar en implementación.

- **Diagrama de Casos de Uso:**
  - [x] Identificar los actores del sistema (Administrador, Responsable, servicio externo ORS).
  - [x] Definir los casos de uso principales y sus relaciones.
  - [x] Generar el diagrama en PlantUML.
- **Diagrama de Clases:**
  - [x] Representar la capa de modelo.
  - [x] Representar la capa DAO con la interfaz genérica y sus implementaciones.
  - [x] Representar la capa de servicio con sus dependencias.
  - [x] Generar el diagrama en PlantUML.

---

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
  - [x] Implementar la lógica de cálculo del cálculo de la huella 
    - [x] Implementar método de consulta en el DAO para obtener los consumos mensuales de un departamento
    - [x] Implementar método de consulta en el DAO para obtener todos los commutings de los empleados de un departamento
    - [x] Implementar un método que calcule la huella total mensual de un departamento basado en los factores de emision asignados.
    - [x] Implementar una prueba para verificar el funcionamiento del calculo.
---

## Fase 3: Interfaz de Usuario y Dashboard 
Creación de la UI con JavaFX y visualización de resultados.
  - **Diseño y planificación:**
    - [x] Definir el flujo de navegación entre pantallas con un diagráma
    - [x] Documentar el mapa de navegación 
    - [x] Especificar cada pantalla: actores, objetivo, elementos y acciones (`screens.md`)
    - [ ] Crear wireframes de las pantallas principales
      - [x] P0 · Selección de rol
      - [x] PA1 · Lista de empresas
      - [ ] PA2 · Vista empresa
      - [ ] PA3 · Gestión de departamento
      - [ ] PR0 · Selección de responsable
      - [ ] PR1 · Vista responsable
    - [x] Ajustar tamaño y calidad del logo en P0
    - [ ] Resolver centrado de ventana P0 en el primer arranque en frío (ver error 8)
  - **Implementación:**
    - [ ] Diseñar pantallas (`.fxml`) y controladores
      - [x] P0 · Selección de rol
      - [x] PA1 · Lista de empresas — tabla, panel lateral alta empresa, navegación a PA2
      - [ ] PA2 · Vista empresa — tabs: Departamentos, Consumos, Gráficos
      - [ ] PA3 · Gestión de departamento (empleados + responsable)
      - [ ] PR0 · Selección de responsable activo
      - [ ] PR1 · Vista responsable — tabs: Consumos, Trabajadores, Huella, Gráficos
    - [ ] Edición y borrado de empresas (desde PA2)
    - [ ] Crear formularios de alta/edición como paneles laterales deslizantes
  - **Validaciones de integridad en la UI:**
    - [ ] Al borrar un Departamento: comprobar si tiene Empleados/Consumos asociados.
    - [ ] Al borrar un Empleado: comprobar si tiene registros de CommutingEmpleado asociados.
    - [ ] Al borrar una Empresa: comprobar si tiene Departamentos asociados.

---

## Fase 4: Pruebas Finales y Documentación
Refinado del software y preparación del proyecto final.
  - [ ] Pruebas de compatibilidad en el sistema operativo Linux.
  - [ ] Optimización de consultas SQL en PostgreSQL.
  - [ ] Finalizar la memoria del proyecto detallando la arquitectura y el cumplimiento de requerimientos.
  - [ ] Empaquetado para distribución
  - **Sistema de autenticación (opcional, si queda margen):**
    - [ ] Nueva tabla `usuarios` en BD (username, password hash, rol, referencia a empleado)
    - [ ] Clase `Usuario` (POJO) y `UsuarioDAO`
    - [ ] Pantalla de login (`login.fxml` + controlador)
    - [ ] Singleton `Sesion` que almacena el usuario logado y su contexto
    - [ ] Reemplazar P0 y PR0 por la sesión activa en la navegación
    - [ ] Actualizar documentación: navegacion.puml, navigation_map.md, screens.md
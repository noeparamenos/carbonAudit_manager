# Arquitectura de Clases y Modelo de Dominio

Describe la organización de la lógica orientada a objetos, enfocándose en la estructura de los datos y su navegación.

## 1. Diseño del modelo (POJOs)

- Se utiliza un modelo de dominio puro basado en Composición, donde las clases del paquete `com.carbonaudit.model` representan fielmente las entidades de negocio sin incluir lógica de cálculo.

### Identidad y Persistencia

- Se ha mantenido una distinción clara entre la persistencia y la lógica en memoria:
  
  - **Identificadores (IDs):**Cada clase incluye un atributo `id` (Primary Key en DB) que permite la sincronización inequívoca con la bd (esenciales para que el patrón DAO pueda realizar operaciones CRUD)
  
  - **Composición de Objetos:** Las relaciones entre tablas se han representado como como **referencias a objetos completos**. Para permitir el acceso a todos los datos navegando entre métodos sin tener que realizar constantemente consultas a la BD.


## 2. Capa Service
- La responsabilidad de transformar consumos en $CO_2$ recae exclusivamente en el `CarbonService`. Esto evita la dispersión de fórmulas y asegura un único punto de verdad.

- Algoritmos de Cálculo:
  ----- A completar

## 2. Tipos de Datos y Precisión Técnica

- Debido a que se trata de un sistema de auditoría, la integridad del dato es un requisito crítico. Por lo que se a optado con tipos de datos con gran precisión para los cálculos.


## 3. Entidades Clave

### A. Entidad "Direccion" 

- La clase `Direccion` actúa como un objeto de valor fundamental que es compartido. 
- Permite integrar APIs de geolocalización (latitud/longitud) de forma global en toda la app.


## 4. Separación de Responsabilidades (Layering)

- Se ha optado por un Modelo de Dominio puro para priorizar la separación de responsabilidades. 
- La lógica de cálculo reside exclusivamente en la Capa de Servicio, garantizando que los modelos permanezcan como POJOs (Plain Old Java Objects) puros, facilitando su mantenimiento y persistencia

1. **Modelo:** Define la estructura y las relaciones entre clases.
2. **DAO (Data Access Object):** Es el único responsable de traducir los IDs de las tablas SQL a los objetos compuestos de Java.
3. **Service:** Realiza los cálculos de emisiones utilizando los datos cargados en el Modelo.

## 5. Tipos de Datos Estandarizados
- **Magnitudes Físicas:** `BigDecimal`: permite mayor precisión en la gestión de datos clave que podrian ser auditados (distancias, cantidades, factores).
- **Fechas:** `java.time.LocalDate` (gestión de histórico de consumos y mandos).
- **Cadenas:** `String` con validación de formato para CIF y Email en la UI. Se realiza una clase Validador que sera usada en la UI (primper filtro) y en la capa de servicio

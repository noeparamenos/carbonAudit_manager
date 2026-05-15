# Arquitectura de Clases y Modelo de Dominio

Describe la organización de la lógica orientada a objetos, enfocándose en la estructura de los datos y su navegación.

## 1. Modelo de dominio (POJOs)

Las clases del paquete `com.carbonaudit.model` representan las entidades de negocio sin incluir lógica de cálculo. Son estructuras de datos puras.

### Identidad y persistencia

- Cada clase incluye un atributo `id` (PK en BD) que permite la sincronización con la base de datos y es necesario para las operaciones CRUD del DAO.
- Las relaciones entre tablas se representan como **referencias a objetos completos** (composición), no como IDs primitivos. Esto permite navegar entre entidades sin consultas adicionales a la BD.

## 2. Capa DAO

El paquete `com.carbonaudit.dao` abstrae toda la comunicación con PostgreSQL. El resto de la aplicación trabaja con objetos Java sin escribir SQL.

Se optó por **JDBC manual** (sin ORM) para garantizar control explícito sobre tipos numéricos de alta precisión y evitar dependencias pesadas como Hibernate.

Patrones aplicados:

1. **Interfaz genérica `DAO<T, K>`**: define las operaciones CRUD estándar. Los generics garantizan que cada DAO maneje el tipo de entidad (`T`) y clave primaria (`K`) correctos.
2. **Singleton `DatabaseManager`**: centraliza la conexión a la BD mediante una única instancia compartida.
3. **Composición en el mapeo**: los DAOs reconstruyen objetos complejos llamando a otros DAOs para resolver las relaciones FK.

Manejo de errores: las `SQLException` se capturan y se relanza como `IllegalArgumentException` con mensaje legible cuando se viola una restricción de integridad (`SQLState 23xxx`).

## 3. Capa de servicio

La responsabilidad de transformar consumos en kg CO₂e recae exclusivamente en `ServicioCalculoHuella`. Esto evita la dispersión de fórmulas y garantiza un único punto de verdad.

- **Fórmula base:** `Emisiones = Cantidad × Factor_Emisión`
- **Scopes:** el servicio agrupa las emisiones por Alcance 1, 2 y 3 según el factor de emisión asociado a cada consumo
- **Commuting:** el cálculo de Alcance 3 requiere geolocalización; se delega en `IServicioGeografico` (implementado por `ServicioGeograficoORS`)

## 4. Tipos de datos

La integridad del dato es un requisito crítico en un sistema de auditoría:

- **`BigDecimal`**: para distancias, cantidades y factores de emisión. Evita errores de precisión de `double` y permite recuperar `null` de la BD sin forzar un valor por defecto.
- **`java.time.LocalDate`**: para fechas de consumos y mandatos de responsables.
- **`String`** con validación de formato para CIF y email (validados en la UI como primer filtro y en la capa DAO como segundo).

## 5. Entidades clave

### `Direccion`
Objeto de valor compartido entre `Empresa`, `Departamento` y `Empleado`. Integra coordenadas de geolocalización (latitud/longitud) obtenidas vía API ORS.

### `Empleado`
Implementa **soft delete**: en lugar de borrar el registro, se registra `fecha_baja`. Las consultas filtran `WHERE fecha_baja IS NULL`. Esto preserva el historial de commuting y mandatos necesario para reproducir cálculos de períodos pasados.

### `Responsable`
Representa un mandato activo de un empleado sobre un departamento. Un mandato activo tiene `fecha_fin IS NULL`.

## 6. Separación de responsabilidades

| Capa | Responsabilidad |
|------|----------------|
| Model | Estructura y relaciones entre entidades (POJOs puros) |
| DAO | Traducción entre objetos Java y tablas SQL |
| Service | Cálculos de emisiones y geolocalización |
| Controller | Gestión de eventos de usuario y navegación entre pantallas |
| View (FXML) | Definición de la estructura visual |
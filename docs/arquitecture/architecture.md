# CarbonAudit Manager – Arquitectura del Sistema

## Objetivo del sistema
CarbonAudit Manager es una aplicación de escritorio diseñada para centralizar
el cálculo de la huella de carbono en PYMES.

## Stack Tecnológico

- Lenguaje: Java 21
- Base de datos: PostgreSQL
- Arquitectura: DAO
- Build: Maven
- UI: JavaFX

## Capas de la aplicación

### 1. Capa de Persistencia
Implementada mediante el patrón DAO para acceder a PostgreSQL.

### 2. Capa de Negocio
Contiene la lógica de cálculo de emisiones:

Emisiones = Cantidad * Factor_emision

### 3. Capa de Presentación
Interfaz de escritorio donde los usuarios introducen consumos,
gestionan departamentos y consultan el dashboard.

La capa de presentación se basa en **JavaFX** y aplica los siguientes patrones:

#### MVC (Model-View-Controller)
Cada pantalla se divide en tres piezas:
- **Model** → los POJOs del paquete `model/` (ej: `ConsumoMensual`, `Empleado`)
- **View** → el archivo `.fxml` que define la estructura visual
- **Controller** → la clase Java que conecta la vista con los datos y gestiona los eventos

JavaFX enlaza automáticamente la vista y el controlador a través del atributo `fx:controller` del FXML y la anotación `@FXML` en los campos del controlador.

#### Observer
Las tablas (`TableView`) se suscriben a una `ObservableList` mediante `setItems()`. Cuando la lista cambia (con `setAll()`), todas las tablas suscritas se actualizan automáticamente sin intervención explícita del controlador. Esto permite que dos tablas compartan la misma lista y se mantengan sincronizadas.

#### Callback / Strategy (lambdas en columnas)
Cada columna de una tabla define su propia función para extraer el valor a mostrar mediante `setCellValueFactory()`. La lambda recibe la fila actual y devuelve el campo correspondiente. Es una implementación del patrón Strategy: cada columna tiene su estrategia intercambiable de extracción de datos.

#### Paso de datos entre controladores
La navegación entre pantallas sigue siempre el mismo patrón:
1. Cargar el FXML destino con `FXMLLoader`
2. Obtener el controlador con `loader.getController()`
3. Pasar el objeto de contexto llamando a un método del controlador (ej: `setEmpresa()`, `setResponsable()`)
4. Mostrar la nueva escena en el mismo `Stage`

## Política de borrado de datos

El sistema aplica una estrategia híbrida consciente que distingue entre eventos de negocio y acciones administrativas:

### Soft-delete en `EMPLEADO`
Cuando un empleado causa baja se escribe `fecha_baja = hoy` pero la fila permanece en la BD. Esto preserva el historial de commuting (`COMMUTING_EMPLEADO`) y los mandatos de responsabilidad (`RESPONSABLE`) necesarios para reproducir cálculos de huella de períodos pasados. Las consultas operativas filtran siempre `WHERE fecha_baja IS NULL`.

### Hard-delete con CASCADE en `DEPARTAMENTO` y `EMPRESA`
Eliminar un departamento o empresa es una acción administrativa destructiva e irreversible. Las FKs llevan `ON DELETE CASCADE`, por lo que un único `DELETE` limpia toda la jerarquía dependiente (empleados, consumos, responsables, commutings). Esta pérdida de historial es aceptada: un departamento activo con datos de auditoría reales nunca debería eliminarse.

### Consecuencia y regla de uso
El soft-delete de un empleado tiene sentido dentro del ciclo de vida de su departamento, pero no sobrevive a la eliminación del departamento padre. La UI advierte explícitamente de esta pérdida antes de confirmar el borrado de una entidad padre. Los `FACTOR_EMISION` y `DIRECCION` no tienen CASCADE: los primeros son datos de referencia globales; las segundas se convierten en huérfanas pero no causan errores de integridad.

## Modelo de emisiones

El sistema calcula emisiones según el estándar:

- Scope 1 → emisiones directas
- Scope 2 → electricidad comprada
- Scope 3 → emisiones indirectas
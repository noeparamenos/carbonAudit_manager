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

## Modelo de emisiones

El sistema calcula emisiones según el estándar:

- Scope 1 → emisiones directas
- Scope 2 → electricidad comprada
- Scope 3 → emisiones indirectas
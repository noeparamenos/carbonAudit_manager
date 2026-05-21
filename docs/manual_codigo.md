# Manual de Código — CarbonAudit Manager

---

## 1. Introducción

CarbonAudit Manager es una aplicación de escritorio desarrollada en Java 21 para
la gestión y el cálculo de la huella de carbono en pequeñas y medianas empresas,
siguiendo la metodología del GHG Protocol Corporate Standard. Permite registrar
consumos energéticos por departamento, gestionar los desplazamientos al trabajo de
los empleados e integrar automáticamente distancias reales mediante la API externa
OpenRouteService.

El presente manual documenta el código fuente del proyecto, describiendo las
decisiones de diseño más relevantes, los patrones de programación aplicados y la
estructura técnica de cada capa. Va dirigido a un lector con conocimientos de Java
y programación orientada a objetos.

### Stack tecnológico

| Componente           | Tecnología                         |
|----------------------|------------------------------------|
| Lenguaje             | Java 21 (LTS)                      |
| Interfaz gráfica     | JavaFX 21 + FXML                   |
| Base de datos        | PostgreSQL 14+                     |
| Acceso a datos       | JDBC manual (sin ORM)              |
| Build y dependencias | Apache Maven 3.8+                  |
| Pool de conexiones   | HikariCP                           |
| Serialización JSON   | Gson 2.10.1                        |
| Variables de entorno | dotenv-java 3.0.0                  |
| API externa          | OpenRouteService (geocodificación) |

### Estructura del manual

El documento se organiza siguiendo las capas de la arquitectura del sistema,
de menor a mayor nivel de abstracción:

1. Introducción
2. Diseño de la base de datos
3. Arquitectura y listado de clases
4. Capa de persistencia — Patrón DAO
5. Capa de servicio — Motor de cálculo
6. Integración con API externa (ORS)
7. Capa de presentación — Controladores
8. Anexos técnicos

Cada sección incluye el diagrama UML correspondiente seguido del código fuente
comentado de los fragmentos más representativos.

---

## 2. Diseño de la base de datos

[[diagramaER]]
*Figura 1. Diagrama Entidad-Relación de CarbonAudit Manager.*

La base de datos se gestiona con PostgreSQL y está compuesta por ocho tablas que
modelan la estructura organizativa de la empresa y sus datos de consumo energético.
La jerarquía principal es la siguiente:

```
EMPRESA
  └── DEPARTAMENTO
        ├── CONSUMO_MENSUAL  ←→  FACTOR_EMISION
        ├── RESPONSABLE      ←→  EMPLEADO
        └── EMPLEADO
                ├── COMMUTING_EMPLEADO  ←→  FACTOR_EMISION
                └── DIRECCION
```

### 2.1 Diccionario de datos

---

#### DIRECCION
Dirección física compartida por empresas, departamentos y empleados. Las coordenadas
son rellenadas automáticamente por la API ORS tras guardar el registro.

| Campo         | Tipo           | Restricciones | Descripción                      |
|---------------|----------------|---------------|----------------------------------|
| id_direccion  | SERIAL         | PK            | Identificador único              |
| calle         | VARCHAR(200)   | NOT NULL      | Nombre de la vía                 |
| numero        | INT            | NOT NULL      | Número de portal                 |
| ciudad        | VARCHAR(100)   | NOT NULL      | Municipio                        |
| codigo_postal | VARCHAR(10)    | NOT NULL      | Código postal                    |
| provincia     | VARCHAR(100)   |               | Provincia                        |
| latitud       | DECIMAL(10,8)  |               | Latitud GPS (rellenada por ORS)  |
| longitud      | DECIMAL(11,8)  |               | Longitud GPS (rellenada por ORS) |

---

#### EMPRESA
Representa la organización auditada. Es la raíz de la jerarquía.

| Campo         | Tipo          | Restricciones         | Descripción             |
|---------------|---------------|-----------------------|-------------------------|
| id_empresa    | SERIAL        | PK                    | Identificador único     |
| nombre_social | VARCHAR(100)  | UNIQUE, NOT NULL      | Razón social            |
| cif           | VARCHAR(15)   | UNIQUE, NOT NULL      | CIF español validado    |
| telefono      | VARCHAR(20)   |                       | Teléfono de contacto    |
| email         | VARCHAR(100)  |                       | Email de contacto       |
| sector        | VARCHAR(50)   |                       | Sector de actividad     |
| id_direccion  | INT           | NOT NULL, FK → DIRECCION | Sede de la empresa   |

---

#### DEPARTAMENTO
Unidad organizativa de una empresa. Puede tener habilitado el cálculo de Alcance 3.

| Campo            | Tipo          | Restricciones               | Descripción                              |
|------------------|---------------|-----------------------------|------------------------------------------|
| id_departamento  | SERIAL        | PK                          | Identificador único                      |
| nombre           | VARCHAR(100)  | NOT NULL, UNIQUE por empresa | Nombre del departamento                 |
| descripcion      | TEXT          |                             | Descripción opcional                     |
| incluir_alcance3 | BOOLEAN       | DEFAULT TRUE                | Activa el cálculo de commuting (Scope 3) |
| id_direccion     | INT           | NOT NULL, FK → DIRECCION    | Ubicación física del departamento        |
| id_empresa       | INT           | NOT NULL, FK → EMPRESA, CASCADE | Empresa a la que pertenece           |

---

#### FACTOR_EMISION
Tabla de coeficientes oficiales de CO₂ equivalente. Los factores de Alcance 3
(tipo transporte) se usan también como medio de transporte de los empleados.

| Campo        | Tipo           | Restricciones              | Descripción                                         |
|--------------|----------------|----------------------------|-----------------------------------------------------|
| id_factor    | SERIAL         | PK                         | Identificador único                                 |
| nombre       | VARCHAR(100)   | NOT NULL, UNIQUE con unidad | Nombre descriptivo (ej. "Electricidad red")         |
| unidad       | VARCHAR(20)    | NOT NULL                   | Unidad de medida (ej. kWh, km, litros)              |
| valor_factor | DECIMAL(10,5)  | NOT NULL                   | kgCO₂e por unidad                                   |
| alcance      | INT            | NOT NULL, CHECK IN (1,2,3) | 1 = combustión directa, 2 = energía, 3 = transporte |

---

#### EMPLEADO
Trabajador adscrito a un departamento. Usa soft-delete para preservar historial.

| Campo             | Tipo          | Restricciones                    | Descripción                                    |
|-------------------|---------------|----------------------------------|------------------------------------------------|
| id_empleado       | SERIAL        | PK                               | Identificador único                            |
| nombre            | VARCHAR(100)  | NOT NULL                         | Nombre completo                                |
| fecha_alta        | DATE          | NOT NULL, DEFAULT CURRENT_DATE   | Fecha de incorporación                         |
| fecha_baja        | DATE          | CHECK >= fecha_alta              | Null = activo; fecha = dado de baja            |
| distancia_trabajo | DECIMAL(6,2)  |                                  | Distancia al trabajo en km (calculada por ORS) |
| medio_transporte  | INT           | NOT NULL, FK → FACTOR_EMISION    | Medio de transporte habitual                   |
| dias_presenciales | INT           | NOT NULL, CHECK (1-31), DEFAULT 20 | Días presenciales al mes                     |
| id_direccion      | INT           | NOT NULL, FK → DIRECCION         | Domicilio del empleado                         |
| id_dept           | INT           | NOT NULL, FK → DEPARTAMENTO, CASCADE | Departamento al que pertenece              |

---

#### RESPONSABLE
Registro de mandatos: qué empleado es responsable de un departamento y en qué período.
Permite trazabilidad histórica de quién introdujo los datos de cada período.

| Campo         | Tipo   | Restricciones                       | Descripción                            |
|---------------|--------|-------------------------------------|----------------------------------------|
| id_asignacion | SERIAL | PK                                  | Identificador único                    |
| fecha_inicio  | DATE   | NOT NULL                            | Inicio del mandato                     |
| fecha_fin     | DATE   | UNIQUE por departamento             | Fin del mandato. Null = mandato activo |
| id_dept       | INT    | NOT NULL, FK → DEPARTAMENTO, CASCADE | Departamento bajo su gestión          |
| id_empleado   | INT    | NOT NULL, FK → EMPLEADO, CASCADE    | Empleado designado como responsable    |

---

#### CONSUMO_MENSUAL
Registro de un recurso energético consumido por un departamento en un período.
Es la base para los cálculos de Alcance 1 y 2.

| Campo      | Tipo           | Restricciones                       | Descripción                              |
|------------|----------------|-------------------------------------|------------------------------------------|
| id_consumo | SERIAL         | PK                                  | Identificador único                      |
| cantidad   | DECIMAL(12,2)  | NOT NULL                            | Cantidad consumida en la unidad del factor |
| mes        | INT            | NOT NULL, CHECK (1-12)              | Mes del período                          |
| anio       | INT            | NOT NULL, CHECK >= 1950             | Año del período                          |
| id_dept    | INT            | NOT NULL, FK → DEPARTAMENTO, CASCADE | Departamento que registra el consumo    |
| id_factor  | INT            | NOT NULL, FK → FACTOR_EMISION       | Tipo de recurso consumido                |

Restricción adicional: combinación `(id_dept, id_factor, mes, anio)` es única — no se puede
registrar el mismo recurso dos veces para el mismo departamento y período.

---

#### COMMUTING_EMPLEADO
Snapshot mensual inmutable del commuting de un empleado. Se genera automáticamente
la primera vez que se consulta el Alcance 3 de un período, capturando los datos
del empleado en ese momento para garantizar la trazabilidad de auditorías pasadas.

| Campo                 | Tipo          | Restricciones                    | Descripción                        |
|-----------------------|---------------|----------------------------------|------------------------------------|
| id_empleado           | INT           | PK (compuesta), FK → EMPLEADO, CASCADE | Empleado al que corresponde  |
| id_factor             | INT           | NOT NULL, FK → FACTOR_EMISION    | Medio de transporte en ese período |
| distancia_diaria_km   | DECIMAL(6,2)  | NOT NULL                         | Distancia diaria al trabajo en km  |
| dias_presenciales_mes | INT           | NOT NULL                         | Días presenciales en ese mes       |
| mes                   | INT           | PK (compuesta), CHECK (1-12)     | Mes del período                    |
| anio                  | INT           | PK (compuesta), CHECK >= 1950    | Año del período                    |

La clave primaria es compuesta `(id_empleado, mes, anio)`: solo puede existir un registro
de commuting por empleado y período.

---

### 2.2 Política de borrado

El sistema aplica una política de borrado híbrida según el impacto de cada entidad
sobre el historial de auditoría:

- **Soft-delete en EMPLEADO:** al causar baja, se registra la `fecha_baja` y el
  empleado deja de aparecer en las vistas operativas, pero sus registros de
  commuting y cálculos históricos se conservan íntegros. Esto permite reproducir
  auditorías de períodos pasados sin pérdida de datos.

- **Hard-delete con CASCADE en DEPARTAMENTO y EMPRESA:** eliminarlos destruye
  de forma irreversible toda su jerarquía de datos (consumos, empleados, commuting).
  La aplicación exige confirmación explícita mediante diálogo modal antes de
  ejecutar estas operaciones.

---

## 3. Arquitectura y listado de clases

[[diagramaPaquetes]]
*Figura 2. Diagrama de paquetes: arquitectura en cuatro capas.*

La aplicación se organiza en cuatro capas horizontales con responsabilidades bien
delimitadas. Cada capa se comunica únicamente con las capas adyacentes, evitando
dependencias cruzadas:

- **Presentación** — `com.carbonaudit.controller` — Controladores JavaFX + FXML
- **Servicio** — `com.carbonaudit.service` — Lógica de negocio + API externa
- **Persistencia** — `com.carbonaudit.dao` — DAOs + DatabaseManager (JDBC)
- **Modelo** — `com.carbonaudit.model` — POJOs del dominio
- **Base de datos** — PostgreSQL

[[diagramaClasesModelo]]
*Figura 3. Diagrama de clases del modelo de dominio.*

[[diagramaClasesDAOServicio]]
*Figura 4. Diagrama de clases de las capas DAO y Servicio.*

### 3.1 Capa de modelo — `com.carbonaudit.model`

POJOs puros que representan las entidades del dominio. No contienen lógica de
negocio ni acceso a datos. Cada clase incluye un campo `id` para la sincronización
con la base de datos. Las relaciones entre entidades se gestionan por composición
de objetos completos, no por claves foráneas primitivas.

| Clase                | Entidad BD asociada   | Descripción                                      |
|----------------------|-----------------------|--------------------------------------------------|
| `Empresa`            | EMPRESA               | Organización auditada                            |
| `Departamento`       | DEPARTAMENTO          | Unidad organizativa de una empresa               |
| `Empleado`           | EMPLEADO              | Trabajador adscrito a un departamento            |
| `Responsable`        | RESPONSABLE           | Mandato de un empleado sobre un departamento     |
| `ConsumoMensual`     | CONSUMO_MENSUAL       | Registro de consumo energético mensual           |
| `CommutingEmpleado`  | COMMUTING_EMPLEADO    | Snapshot mensual de commuting de un empleado     |
| `FactorEmision`      | FACTOR_EMISION        | Coeficiente de emisión de CO₂ por unidad         |
| `Direccion`          | DIRECCION             | Dirección física con coordenadas GPS             |

> **Excepción justificada:** `ConsumoMensual` incluye el método `calcularEmision()`
> (cantidad × factor). Se permitió este cálculo en el modelo por ser una operación
> atómica sobre sus propios campos, sin dependencias externas. Si en el futuro el
> cálculo requiriera lógica adicional (conversiones, correcciones normativas), debería
> migrarse a `ServicioCalculoHuella`.

[[codigo:ConsumoMensual#calcularEmision]]
*ConsumoMensual.java — único método de dominio del modelo.*

### 3.2 Capa de persistencia — `com.carbonaudit.dao`

Abstrae toda la comunicación con PostgreSQL. El SQL nunca sale de esta capa; el
resto de la aplicación trabaja exclusivamente con objetos Java.

| Clase                  | Descripción                                                  |
|------------------------|--------------------------------------------------------------|
| `DAO<T,K>`             | Interfaz genérica con contrato CRUD común a todos los DAOs   |
| `DatabaseManager`      | Singleton que gestiona el pool de conexiones HikariCP        |
| `EmpresaDAO`           | CRUD sobre EMPRESA                                           |
| `DepartamentoDAO`      | CRUD sobre DEPARTAMENTO                                      |
| `EmpleadoDAO`          | CRUD sobre EMPLEADO (con soporte de soft-delete)             |
| `DireccionDAO`         | CRUD sobre DIRECCION                                         |
| `FactorEmisionDAO`     | CRUD sobre FACTOR_EMISION                                    |
| `ResponsableDAO`       | CRUD sobre RESPONSABLE                                       |
| `ConsumoMensualDAO`    | CRUD y consultas por departamento/empresa/período            |
| `CommutingEmpleadoDAO` | CRUD y consultas de snapshots de commuting por período       |

### 3.3 Capa de servicio — `com.carbonaudit.service`

Contiene toda la lógica de negocio. Es la única puerta de acceso entre los
controladores y la persistencia: ningún controlador instancia ni llama a un DAO
directamente.

| Clase                       | Descripción                                                   |
|-----------------------------|---------------------------------------------------------------|
| `ServicioCalculoHuella`     | Motor de cálculo GHG Protocol (Scope 1, 2 y 3)               |
| `ServicioGestionEmpresa`    | Alta, edición, baja y consulta de empresas                    |
| `ServicioGestionDepartamento` | Gestión de departamentos y sus relaciones                   |
| `ServicioGestionEmpleado`   | Gestión de empleados, incluyendo geocodificación y soft-delete|
| `ServicioGestionFactores`   | Gestión de factores de emisión                                |
| `ServicioGestionResponsable`| Gestión de mandatos de responsables por departamento          |
| `IServicioGeografico`       | Interfaz que desacopla el proveedor de geolocalización        |
| `ServicioGeograficoORS`     | Implementación de `IServicioGeografico` con OpenRouteService  |

### 3.4 Capa de presentación — `com.carbonaudit.controller`

Controladores JavaFX que gestionan los eventos de usuario, coordinan las llamadas
a la capa de servicio y actualizan los componentes visuales. Cada controlador está
enlazado a su vista FXML mediante el atributo `fx:controller`.

| Clase                    | Pantalla | Descripción                                          |
|--------------------------|----------|------------------------------------------------------|
| `SeleccionRolController` | P0       | Pantalla de entrada: selección de rol                |
| `PA0Controller`          | PA0      | Lista de empresas y gestión de factores de emisión   |
| `PA1Controller`          | PA1      | Vista de empresa: departamentos, empleados, gráficos |
| `PA2Controller`          | PA2      | Gestión detallada de un departamento                 |
| `PR0Controller`          | PR0      | Selección del responsable activo                     |
| `PR1Controller`          | PR1      | Vista de trabajo del responsable: consumos y gráficos|

### 3.5 Utilidades

| Clase       | Descripción                                                              |
|-------------|--------------------------------------------------------------------------|
| `Validador` | Métodos estáticos de validación de formato y reglas de negocio           |
| `Launcher`  | Punto de entrada real de la aplicación (patrón Launcher para JavaFX)     |
| `Main`      | Clase que extiende `Application` de JavaFX e inicializa la primera vista |

---

## 4. Capa de persistencia — Patrón DAO

La capa de persistencia abstrae toda la comunicación con PostgreSQL mediante el
patrón **DAO (Data Access Object)**. El resto de la aplicación trabaja exclusivamente
con objetos Java; el SQL nunca sale de esta capa.

Se compone de tres elementos:

- Una **interfaz genérica** `DAO<T,K>` que impone el mismo contrato CRUD a todos los DAOs.
- Un **Singleton** `DatabaseManager` que centraliza el pool de conexiones.
- **Ocho DAOs concretos**, uno por entidad, que implementan la interfaz.

### 4.1 Interfaz genérica DAO

La interfaz `DAO<T, K>` define el contrato CRUD común mediante **tipos genéricos**:
`T` es el tipo de entidad gestionada y `K` el tipo de la clave primaria. Cada DAO
concreto declara sus tipos al implementarla, por ejemplo `EmpresaDAO implements DAO<Empresa, Integer>`.

Esto garantiza que todos los DAOs expongan las mismas operaciones, lo que permite
tratarlos de forma uniforme desde la capa de servicio.

El método `findById` devuelve `Optional<T>` en lugar de `T` directamente. `Optional`
es un contenedor que puede estar vacío, y obliga al llamador a gestionar explícitamente
el caso de que el registro no exista, eliminando comprobaciones de `null` en los
controladores.

[[codigo:DAO]]
*DAO.java — interfaz genérica que define el contrato CRUD de la capa de persistencia.*

**Gestión del Optional en la capa de servicio**

`Optional` obliga al llamador a decidir explícitamente qué hacer cuando el registro
no existe. Sin él, un `null` desapercibido causaría un `NullPointerException` en
tiempo de ejecución sin ningún aviso del compilador. Las formas de consumirlo:

```java
// orElseThrow — la más habitual: lanza excepción con mensaje legible si no existe
Empresa empresa = empresaDAO.findById(id)
    .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + id));

// ifPresent — cuando la ausencia no es un error, simplemente no se actúa
empresaDAO.findById(id).ifPresent(e -> procesarEmpresa(e));

// isPresent — equivalente al if != null clásico, pero más explícito
Optional<Empresa> resultado = empresaDAO.findById(id);
if (resultado.isPresent()) {
    Empresa e = resultado.get();
}
```

En este proyecto se usa principalmente `orElseThrow`: la excepción sube hasta el
controlador, que la captura y muestra el mensaje al usuario sin que la lógica de
negocio tenga que gestionar valores nulos.

### 4.2 Singleton DatabaseManager y pool de conexiones

`DatabaseManager` es el único punto de acceso a la base de datos en toda la
aplicación. Implementa el patrón **Singleton**: solo puede existir una instancia,
creada la primera vez que se solicita y reutilizada en adelante.

**¿Por qué Singleton?**
Sin él, cada DAO podría crear su propio pool de conexiones, multiplicando las
conexiones abiertas con PostgreSQL innecesariamente. El Singleton garantiza que
todos los DAOs comparten el mismo pool.

**El pool HikariCP** mantiene entre 2 y 10 conexiones abiertas y listas. Cada DAO
obtiene una conexión al inicio de cada operación mediante `getConnection()` y la
devuelve automáticamente al cerrarla con `try-with-resources`. Esto elimina el
coste de establecer una nueva conexión TCP con PostgreSQL en cada consulta, que
era la causa de la latencia visible en la UI antes de introducirlo.

**Seguridad en multihilo:** el método `getInstance()` está marcado como
`synchronized`. Sin esta palabra clave, dos hilos podrían ejecutar simultáneamente
la comprobación `if (instance == null)` durante el arranque y crear dos instancias
distintas del pool, lo que abriría conexiones duplicadas con la base de datos.

[[codigo:DatabaseManager]]
*DatabaseManager.java — Singleton con pool de conexiones HikariCP.*

### 4.3 Composición en el mapeo — el problema N+1

Los DAOs concretos no devuelven objetos planos con IDs numéricos — reconstruyen
objetos completamente compuestos. Por ejemplo, `DepartamentoDAO` no devuelve un
`int idDireccion`, sino que llama internamente a `DireccionDAO` para resolver la
relación y entregar un `Departamento` con su `Direccion` ya cargada:

```java
departamento.getDireccion().getCiudad();   // sin consulta adicional
departamento.getEmpresa().getNombreSocial(); // sin consulta adicional
```

**El trade-off:** este diseño favorece la legibilidad y la reutilización de los
DAOs, pero genera el **problema N+1**: cargar una lista de N departamentos dispara
N consultas adicionales a `DireccionDAO` y N a `EmpresaDAO`. En las vistas de
gráficos anuales esto provocó que la UI se congelara, lo que obligó a mover esas
cargas a hilos de fondo (ver Sección 7). Una solución estructural sería usar
consultas con `JOIN` en los DAOs para recuperar el grafo completo en una sola
consulta SQL.

### 4.4 Consulta y mapeo: `findById` y `mapResultSetToEmpresa`

El par `findById` + `mapResultSetToEmpresa` de `EmpresaDAO` ilustra el ciclo
completo de una consulta en la capa DAO: preparar la query parametrizada,
ejecutarla, convertir la fila en objeto Java y gestionar la ausencia de resultado.

[[codigo:EmpresaDAO#findById]]
*EmpresaDAO.java — consulta por clave primaria con Optional y composición.*

```java
public Optional<Empresa> findById(Integer id) {
    String sql = "SELECT * FROM EMPRESA WHERE id_empresa = ?";
    try (Connection conn = DatabaseManager.getInstance().getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, id);       // parámetro enlazado: evita SQL injection
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {           // fila encontrada
            // mapea la fila y envuelve el objeto en un Optional
            return Optional.of(mapResultSetToEmpresa(rs));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return Optional.empty();       // empresa no encontrada
}

private Empresa mapResultSetToEmpresa(ResultSet rs) throws SQLException {
    Empresa e = new Empresa();
    e.setIdEmpresa(rs.getInt("id_empresa"));
    e.setNombreSocial(rs.getString("nombre_social"));
    // ... resto de campos escalares ...

    // COMPOSICIÓN: resuelve la FK cargando el objeto Direccion completo
    int idDir = rs.getInt("id_direccion");
    direccionDAO.findById(idDir).ifPresent(e::setDireccion);
    return e;
}
```

**`PreparedStatement` y SQL injection:** los parámetros se pasan con `setInt` /
`setString`, nunca concatenados al SQL. PostgreSQL separa la query de los datos,
por lo que un valor malicioso en `id` no puede alterar la consulta.

**`Optional.of` vs `Optional.empty`:** el llamador recibe un contenedor que le
obliga a decidir explícitamente qué hacer si la empresa no existe — sin posibilidad
de recibir un `null` desapercibido que cause un `NullPointerException` más adelante.

**`ifPresent` en la composición:** `direccionDAO.findById(idDir)` también devuelve
un `Optional`. Con `ifPresent` se asigna la dirección solo si existe, sin necesidad
de comprobar nulos manualmente.

---

## 5. Capa de servicio — Motor de cálculo

La capa de servicio centraliza toda la lógica de negocio del sistema. Ningún
controlador accede directamente a un DAO; toda llamada de la UI pasa primero
por un servicio. Esto garantiza que si la fórmula de cálculo cambia, o si
una regla de negocio evoluciona, la modificación afecta a un único fichero.

`ServicioCalculoHuella` es el servicio más relevante: implementa las tres
categorías del GHG Protocol Corporate Standard y calcula las emisiones tanto
por departamento como por empresa, tanto mensual como anualmente.

[[diagramaSecuenciaHuella]]
*Figura 5. Diagrama de secuencia: consulta de huella total de un departamento.*

### 5.1 El GHG Protocol y los tres Scopes

El GHG Protocol clasifica las emisiones en tres categorías:

- **Scope 1** — combustión directa: gas natural, gasóleo, propano...
- **Scope 2** — energía adquirida: electricidad consumida de la red.
- **Scope 3** — cadena de valor: en este sistema, los desplazamientos al trabajo
  (*commuting*) de los empleados.

La fórmula base es la misma para Scope 1 y 2:

```
Emisión = Cantidad consumida × Factor de emisión (kgCO₂e/unidad)
```

Para Scope 3 (commuting):

```
Emisión mensual = Distancia_diaria_km × 2 (ida y vuelta)
               × Días_presenciales_mes × Factor_transporte (kgCO₂e/km)
```

### 5.2 BigDecimal y precisión de auditoría

En Java, `double` y `float` representan números en coma flotante binaria, lo que
introduce errores de redondeo acumulativos. En un contexto de auditoría
medioambiental estos errores son inaceptables — las emisiones reportadas deben
ser reproducibles con exactitud aritmética.

`BigDecimal` almacena el número en decimal exacto y requiere especificar la
escala y el modo de redondeo en cada operación. Las tres operaciones utilizadas
en el motor:

```java
// multiply: producto exacto entre dos BigDecimal
BigDecimal resultado = cantidad.multiply(factor.getValorFactor());

// add: suma acumulativa inicializada en ZERO (constante predefinida de BigDecimal)
totalEmisiones = totalEmisiones.add(emisionConsumo);

// setScale + HALF_UP: redondeo estándar a 2 decimales en el resultado final
return totalEmisiones.setScale(2, RoundingMode.HALF_UP);
```

### 5.3 Método principal: `getHuellaTotalDepartamentoMes`

[[codigo:ServicioCalculoHuella#getHuellaTotalDepartamentoMes]]
*ServicioCalculoHuella.java — método central del motor de cálculo GHG Protocol.*

```java
public BigDecimal getHuellaTotalDepartamentoMes(Departamento departamento,
                                                 int mes, int anio) {
    BigDecimal totalEmisiones = BigDecimal.ZERO;

    // Scope 1 + 2: consumos energéticos del departamento
    List<ConsumoMensual> consumos = consumoDAO.getConsumosDepartamentoMes(
            departamento.getIdDepartamento(), mes, anio);
    for (ConsumoMensual consumo : consumos) {
        BigDecimal emision = consumo.calcularEmision(); // cantidad × factor
        if (emision != null) totalEmisiones = totalEmisiones.add(emision);
    }

    // Scope 3: commuting (solo si el departamento lo tiene habilitado)
    if (departamento.isIncluirAlcance3()) {
        garantizarRegistrosCommuting(departamento.getIdDepartamento(), mes, anio);
        List<CommutingEmpleado> commutings = commutingDAO
                .getCommutingsDepartamentoMes(departamento.getIdDepartamento(),
                                              mes, anio);
        for (CommutingEmpleado c : commutings) {
            BigDecimal emision = calcularEmisionCommuting(c);
            if (emision != null) totalEmisiones = totalEmisiones.add(emision);
        }
    }

    return totalEmisiones.setScale(2, RoundingMode.HALF_UP);
}
```

El flag `incluirAlcance3` se almacena por departamento en la BD
(`BOOLEAN DEFAULT TRUE`), permitiendo que cada departamento decida
de forma independiente si incluye el commuting en sus emisiones.

### 5.4 Generación lazy de snapshots de commuting

La tabla `COMMUTING_EMPLEADO` almacena registros **inmutables**: una vez
creados para un período, no se modifican aunque cambien los datos del empleado.
Esto garantiza que una auditoría de un mes pasado devuelva siempre los mismos
números.

`garantizarRegistrosCommuting` implementa la creación *lazy*: genera el snapshot
únicamente la primera vez que se consulta un período histórico. Si ya existen
registros, los reutiliza; si el período es futuro, no actúa.

```java
private void garantizarRegistrosCommuting(int idDepartamento, int mes, int anio) {
    LocalDate ahora = LocalDate.now();
    // No generar datos para períodos futuros
    if (anio > ahora.getYear() ||
       (anio == ahora.getYear() && mes > ahora.getMonthValue())) return;

    // Si ya existe el snapshot, reutilizarlo
    if (!commutingDAO.getCommutingsDepartamentoMes(idDepartamento, mes, anio)
                     .isEmpty()) return;

    // Crear el snapshot con los empleados activos en este momento
    for (Empleado emp : empleadoDAO.findAllByDepartamento(idDepartamento)) {
        if (emp.getDistanciaTrabajo() == null || emp.getMedioTransporte() == null)
            continue;
        commutingDAO.create(new CommutingEmpleado(
                emp, emp.getMedioTransporte(), emp.getDistanciaTrabajo(),
                emp.getDiasPresenciales(), mes, anio));
    }
}
```

> **Limitación conocida:** el snapshot captura los empleados activos en el
> momento de la primera consulta. Si un empleado causa baja y después se
> consulta un período sin snapshot previo, ese empleado no aparecerá en el
> registro. La corrección requeriría filtrar por `fecha_alta` y `fecha_baja`
> relativas al período consultado (ver `errorCorrections.md`, entrada 15).

### 5.5 Desglose por Scope y cálculo anual

Además del total mensual, el servicio expone dos variantes para los gráficos:

- **`getHuellaPorScope(departamento, mes, anio)`** — devuelve un
  `Map<Integer, BigDecimal>` con las emisiones desglosadas por Scope (1, 2 y 3).
  Usa `Map.merge()` para acumular valores por clave sin comprobar si ya existe
  una entrada:

  ```java
  // Si "scope" ya está en el mapa, suma; si no, inserta directamente
  resultado.merge(scope, consumo.calcularEmision(), BigDecimal::add);
  ```

- **`getHuellaAnualDepartamento` / `getHuellaAnualEmpresa`** — iteran sobre
  los 12 meses reutilizando `getHuellaTotalDepartamentoMes`. La empresa agrega
  además sobre todos sus departamentos.

---
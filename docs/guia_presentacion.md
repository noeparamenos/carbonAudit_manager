# Guía de Presentación — CarbonAudit Manager

> Documento de apoyo para construir el discurso de la presentación.
> Más extenso que el guión — explica el *por qué* detrás de cada decisión.

---

## 1. Por qué este proyecto

### El problema real
Las empresas están bajo una presión creciente para medir y reportar su huella de carbono. Esta presión viene de dos frentes:

- **Normativo**: la directiva europea CSRD (Corporate Sustainability Reporting Directive) exige a empresas de cierto tamaño reportar sus emisiones. Aunque hoy aplica a grandes empresas, el efecto cascada alcanza a las PYMEs que forman parte de su cadena de suministro.
- **Mercado**: cada vez más clientes, inversores y licitaciones públicas exigen acreditar compromisos ambientales. Una PYME sin datos de emisiones pierde competitividad.

### La brecha que cubre
Las herramientas existentes están pensadas para grandes corporaciones con departamentos dedicados. Son caras, complejas y requieren infraestructura cloud. Una PYME no tiene ni el presupuesto ni el personal para operarlas.

CarbonAudit Manager cubre ese hueco: una aplicación de escritorio, que funciona en local sin depender de internet ni de suscripciones, que cualquier responsable de administración puede operar sin formación técnica especializada.

### Por qué es un buen proyecto de DAM
- Integra todos los bloques del ciclo: base de datos relacional, acceso a datos, lógica de negocio, interfaz gráfica, API externa, exportación de informes.
- Tiene un dominio real (sostenibilidad empresarial) con reglas de negocio concretas (GHG Protocol), no es un CRUD genérico.
- Permite demostrar toma de decisiones de diseño justificadas, no solo código que funciona.

---

## 2. Por qué estas tecnologías

### Java 21
Java es el lenguaje estándar del ciclo DAM. Elegir la versión 21 (LTS) garantiza soporte a largo plazo y acceso a las últimas mejoras del lenguaje. Para una aplicación de auditoría con cálculos de precisión, Java ofrece `BigDecimal` de forma nativa — algo que lenguajes como JavaScript o Python requieren librerías adicionales para conseguir de forma fiable.

### JavaFX 21
JavaFX es la tecnología oficial de interfaces de escritorio en Java. La alternativa más común (Swing) está en mantenimiento y tiene una API anticuada. JavaFX permite diseñar pantallas en FXML (separando vista de lógica), usa CSS para estilos y tiene componentes modernos como `TableView`, `ComboBox` y `DatePicker` que encajan directamente con el modelo de datos de la aplicación.

### PostgreSQL
Se eligió PostgreSQL sobre otras alternativas (MySQL, SQLite) por tres razones:
- Soporte completo de `DECIMAL` con precisión arbitraria — imprescindible para cálculos de auditoría.
- `ON DELETE CASCADE` y restricciones de integridad referencial robustas.
- Estándar en entornos profesionales — aprender PostgreSQL tiene valor directo en el mercado laboral.

### Maven
Maven gestiona las dependencias automáticamente (sin descargar JARs a mano), estandariza el ciclo de build y permite ejecutar la aplicación con un solo comando (`mvn javafx:run`). Es la herramienta de build más extendida en proyectos Java empresariales.

### JDBC manual (sin ORM)
Se descartó Hibernate o cualquier ORM deliberadamente. Los ORMs abstraen el SQL y pueden introducir conversiones implícitas de tipos que afectan a la precisión numérica. En una auditoría, un redondeo silencioso es un error de datos. JDBC manual garantiza control total sobre cada consulta y cada tipo de dato.

### Git / GitHub Flow
El control de versiones se llevó con ramas por funcionalidad (`features/UI`) y commits descriptivos. Esto refleja el flujo de trabajo estándar en equipos de desarrollo profesional.

---

## 3. Qué aporta la arquitectura

### Separación en capas
La arquitectura DAO de tres capas tiene un beneficio concreto: **el cambio en una capa no afecta a las demás**. Si mañana se quisiera cambiar PostgreSQL por MySQL, solo habría que modificar los DAOs. Si se quisiera cambiar JavaFX por una interfaz web, la capa de servicio y los DAOs seguirían intactos.

### Patrón DAO con interfaz genérica
La interfaz `DAO<T, K>` obliga a que todos los DAOs implementen las mismas operaciones básicas (create, findById, findAll, update, delete). Esto garantiza consistencia y facilita el mantenimiento — cualquier desarrollador nuevo sabe exactamente qué esperar de cada DAO.

### Singleton con pool de conexiones (HikariCP)
`DatabaseManager` es un Singleton: una única instancia compartida por toda la aplicación. HikariCP gestiona un pool de conexiones para no abrir y cerrar una conexión por cada operación. Esto mejora el rendimiento y evita el agotamiento de conexiones en operaciones encadenadas.

### Interfaz IServicioGeografico
El servicio de geolocalización se accede siempre a través de una interfaz, nunca directamente a la clase concreta. Esto significa que cambiar de OpenRouteService a Google Maps (o cualquier otro proveedor) solo requiere escribir una nueva implementación sin tocar ningún controlador ni servicio de negocio.

### Observer pattern con ObservableList
Las tablas de la interfaz están suscritas a listas observables. Cuando los datos cambian, la tabla se actualiza automáticamente sin código adicional. Esto elimina una clase entera de bugs (tablas desincronizadas con los datos) y simplifica los controladores.

### Composición sobre FK primitivas
Los objetos del modelo mantienen referencias completas a objetos relacionados (un `Empleado` tiene un objeto `Departamento`, no solo un `int idDepartamento`). Esto permite navegar el grafo de objetos en memoria sin consultas adicionales a la BD una vez cargados los datos.

---

## 4. Qué solucionan las funcionalidades actuales

### Gestión estructurada de la empresa
El administrador puede modelar la estructura real de la empresa: sede principal, departamentos con sus propias ubicaciones, empleados con sus datos de movilidad. Esto permite calcular emisiones a nivel granular — por departamento, por período, por tipo de energía — en lugar de un número global poco útil.

### Factores de emisión configurables
Los factores de emisión no están hardcodeados en el código: son datos en la BD que el administrador puede gestionar. Cuando los factores oficiales se actualicen (se publican anualmente), el responsable puede actualizar los valores sin tocar la aplicación.

### Geocodificación automática
Al introducir una dirección (empresa, departamento o empleado), la aplicación obtiene automáticamente las coordenadas en segundo plano llamando a la API de OpenRouteService. El usuario no necesita buscar coordenadas manualmente. Sobre esas coordenadas se calcula la distancia real por carretera al lugar de trabajo.

### Cálculo de commuting (Scope 3)
El commuting es la parte de la huella que más empresas ignoran porque es difícil de medir. La aplicación lo automatiza: con la distancia al trabajo, el tipo de transporte y los días presenciales, calcula el impacto mensual de cada empleado. Es el diferencial más relevante frente a otras herramientas básicas.

### Desglose por Scope
El informe de huella no es un número único — se desglosa por Scope 1, 2 y 3. Esto permite a la empresa identificar qué tipo de emisiones domina y dónde tiene más margen de mejora.

### Exportación CSV y PDF
Los datos tienen que salir de la aplicación para ser útiles: presentarlos a dirección, enviarlos a un auditor externo, incluirlos en una memoria de sostenibilidad. CSV para análisis en hoja de cálculo, PDF para informes formales.

### Historial de responsables
El mandato de cada responsable queda registrado con fechas de inicio y fin. Esto preserva la trazabilidad de quién introdujo los datos en cada período — requisito básico en cualquier proceso de auditoría.

### Soft-delete de empleados
Los empleados no se borran físicamente al darse de baja — se registra la fecha de baja. Sus datos de commuting históricos se conservan para que los informes de períodos pasados sigan siendo precisos. Borrarlos destruiría la auditoría retrospectiva.

---

## 5. Qué solucionarán las funcionalidades futuras

### Sistema de autenticación
Actualmente cualquiera que abra la aplicación puede seleccionar cualquier rol. Un sistema de login con roles garantizaría que solo el administrador puede modificar la estructura de la empresa, y que cada responsable solo ve su departamento. Es el paso necesario para que la aplicación sea usable en un entorno real con múltiples usuarios.

### Gráficos de evolución histórica
Los datos mes a mes tienen poco valor sin visualización temporal. Los gráficos permitirían ver tendencias: ¿las emisiones suben o bajan? ¿Qué departamento mejoró más tras instalar placas solares? ¿El teletrabajo redujo el Scope 3? Sin esta vista, la monitorización es reactiva; con ella, se vuelve proactiva.

### Empaquetado para distribución
Actualmente instalar la aplicación requiere instalar Java, PostgreSQL, Maven y configurar archivos manualmente. Con `jpackage` se generaría un instalador nativo (`.exe` en Windows, `.deb` en Linux) que incluye la JVM. Con `docker-compose` para PostgreSQL, el usuario levantaría la base de datos con un solo comando. Esto hace la aplicación accesible para usuarios no técnicos.

### Optimización de consultas con JOIN
La arquitectura de composición actual genera muchas consultas encadenadas (N+1). Reemplazarlas por queries con JOIN reduciría la latencia de carga de datos significativamente, permitiendo eliminar los hilos de fondo en los controladores y simplificar el código.

### Integración con Google Maps API
OpenRouteService es una API open source con límites de uso. Google Maps ofrece mayor precisión en zonas urbanas y mayor disponibilidad. La interfaz `IServicioGeografico` ya está diseñada para que este cambio sea transparente al resto de la aplicación.

---

## 6. Mensaje clave para el tribunal

Si el tribunal solo se quedara con una idea, debería ser esta:

> **El proyecto no es un ejercicio académico de CRUD. Es una herramienta con un dominio real, decisiones de diseño justificadas y un modelo de negocio concreto. Cada decisión técnica — desde el uso de BigDecimal hasta la interfaz IServicioGeografico — responde a un requisito real del problema que resuelve.**

---

## 7. Preguntas difíciles y cómo responderlas

**¿Por qué no es una aplicación web?**
> El requisito era una herramienta que funcione en local, sin depender de un servidor ni de conexión a internet continua. Una PYME no quiere que sus datos de emisiones estén en un servidor de terceros. La privacidad y la independencia de infraestructura son ventajas competitivas de la solución.

**¿Escala esta arquitectura?**
> Para el caso de uso objetivo (una PYME con decenas de empleados y consumos mensuales) escala perfectamente. Para una consultora que gestione cientos de empresas simultáneamente, habría que migrar a una arquitectura cliente-servidor con backend REST. La separación de capas actual facilita esa migración — la capa de servicio no cambiaría.

**¿Por qué no usas Spring Boot?**
> Spring Boot está orientado a aplicaciones web con servidor embebido. Añadiría una capa de complejidad (contexto de aplicación, inyección de dependencias automática, autoconfiguración) que no aporta nada en una aplicación de escritorio standalone. El patrón DAO manual es más transparente y más apropiado para este caso.

**¿Qué pasa si la API de OpenRouteService deja de funcionar?**
> La aplicación sigue funcionando. La geocodificación y el cálculo de distancias se hacen en segundo plano y son opcionales — si fallan, el empleado simplemente no tiene distancia calculada. La interfaz `IServicioGeografico` permite añadir un proveedor alternativo sin modificar el resto del código.

**¿Cómo garantizas la precisión de los cálculos?**
> Usando `BigDecimal` en todos los campos numéricos en Java y `DECIMAL` con precisión explícita en PostgreSQL. Esto evita los errores de redondeo de punto flotante (`double`, `float`) que en una auditoría pueden acumular diferencias significativas. Es una decisión deliberada documentada en la arquitectura.

1. La implementación de la permisividad de NOT NULL para empresas se vuelve tediosa en la clase EmpresaDAO
    - Se ha exigido que la empresa tenga una dirección (not null en la BD) eliminando la tediosidad
    - Esto además es más lógico ya que toda empresa tiene una dirección y muchos departamentos estarán instalados en la misma dirección de la empresa
2. El calculo de direcciones sin numero de calle es impreciso
    - Se ha añadido el número de calle a la dirección (int) a la BD y la clase modelo
    - Esto no acaba de solucionar todos los problemas de la API (ver error 5)
3. Ejecución de tests de las clases DAO: Errores de integridad no bien descritos
    - Se han comprobado las restricciones de integridad en cada una de las clases DAO
    - Dan información al usuario sobre la regla que se está incumpliendo 
    - Sirven de última barrera antes de intentar meter datos en la BD que sean susceptibles de generar un error
4. La API ORS es imprecisa 
   - **Causa**: ORS no geolocaliza a la perfección las direcciones ituadas en pueblos y pequeñas ciudades
   - **Solución**: Se propone el uso de otra API más precisa (como google) aunque se ha de valorar el gasto económico y que dejaría de ser OpenSource.
     - Se Puede plantear la opción de elegir la API deseada durante la ejecución del programa
5. Al ejecutar la aplicación JavaFX da error. Al tener la versión community de Intellj no puedo generar los fxml automáticamente
    - **Causa:** Desde Java 9, JavaFX ya no viene incluido con el JDK. 
      - Cuando una clase que contiene `main()` ADEMÁS extiende `Application`, la JVM exige que JavaFX esté en el *module path*. 
      - Como en este proyecto JavaFX se carga desde el *classpath* (configuración Maven clásica), esa comprobación falla.
    - **Solución:** Se ha creado una clase intermedia `Launcher` que NO extiende `Application` (simplemente delega en `Main.main()`). 
      - Al ejecutar `Launcher`, el JVM no dispara la comprobación estricta.
      - Es el *Launcher pattern*: común en proyectos JavaFX empaquetados con Maven/Gradle.
6. La ventana P0 no se centra correctamente en el primer arranque en frío desde IntelliJ.
   - **Causa**: En Linux, el gestor de ventanas reposiciona la ventana después de que JavaFX la coloca, ignorando cualquier `setX`/`setY` o `centerOnScreen()` previos al `show()`. El problema se agrava al lanzar desde IntelliJ por la latencia extra del agente IDE.
   - **Solución**: Llamar a `Platform.runLater()` tras `show()` para encolar el centrado después del primer ciclo de renderizado, cuando la ventana ya tiene posición real. Se usa `Screen.getPrimary().getVisualBounds()` para calcular `setX`/`setY` manualmente. Funciona en Linux y Windows.
7. `TableView.CONSTRAINED_RESIZE_POLICY` no se puede referenciar desde FXML en JavaFX 21.
   - **Causa**: El loader FXML de JavaFX 21 no puede convertir el string "TableView.CONSTRAINED_RESIZE_POLICY" al tipo `Callback` que espera `columnResizePolicy`.
   - **Solución**: Eliminar el atributo del FXML y asignarlo en el controlador desde `initialize()` con `tablaEmpresas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN)`.
8. Borrar un empleado destruía su historial de commuting y mandatos de responsabilidad.
    - **Causa**: El esquema original no contemplaba el ciclo de vida del empleado. Un `DELETE` en `EMPLEADO`  bloqueando los registros de `COMMUTING_EMPLEADO` y `RESPONSABLE`, que debería persistir siempre.
    - **Solución**: Implementar **soft delete** añadiendo `fecha_alta` y `fecha_baja` a la tabla `EMPLEADO` (se registra la fecha en lugar de borrar el registro). El `delete()` se mantiene para en tests.
9. Al modificar la dirección de un empleado o departamento, la distancia al trabajo no se recalculaba.
    - **Causa**: `AsignarDistanciaTrabajo()` solo completaba coordenadas cuando `latitud == null || longitud == null`. Los controladores no detectaban si la dirección había cambiado (las coordenadas eran las antiguas)
    - **Solución**: `AsignarDistanciaTrabajo()` llama siempre a `completarCoordenadas()`, sin condición.
10. Varios controladores llamaban directamente a DAOs, violando la separación de capas. Además, `PA1Controller` contenía lógica de negocio.
    - **Causa**: Los controladores instanciaban y llamaban a DAOs directamente para obtener listas y persistir resultados. `PA1Controller.cargarConsumos()` calculaba además el total sin commuting.
    - **Solución**: 
      - Se añaden métodos de acceso al Servicio que encapsulan las llamadas al DAO
      - Se crea un Servicio para cada tabla para centralizar el CRUD. `ServicioCalculoHuella` queda dedicado exclusivamente a cálculos de huella
      - En el hilo asíncrono de cálculo de distancia, `Task.call()` invoca `servicioHuella` seguido de `servicioGestion` para la persistencia
11. `PSQLException` por clave duplicada en `ConsumoMensualDAO.create()` no llegaba al controlador.
    - **Causa**: El `catch (SQLException e)` del DAO solo hacía `printStackTrace()` y no relanzaba nada. El controlador capturaba `IllegalArgumentException` para mostrar avisos al usuario, pero nunca recibía el tipo de error.
    - **Solución**: En el `catch` del DAO se comprueba el SQLState (`23xxx` = violación de integridad) y se relanza como `IllegalArgumentException` con mensaje legible. Cualquier otro `SQLException` se relanza como `RuntimeException`.
12. El Alcance 3 (commuting) siempre mostraba 0 aunque estuviera habilitado para el departamento.
    - **Causa**: El servicio calculaba el scope 3 consultando la tabla `COMMUTING_EMPLEADO`, que nunca se llenaba desde la UI. 
      -  Además, `DepartamentoDAO.update()` absorbía errores SQL silenciosamente (`e.printStackTrace()`), por lo que fallos al guardar `incluir_alcance3` pasaban desapercibidos.
    - **Solución**:
      - `DepartamentoDAO.update()` ahora relanza el `SQLException` como `RuntimeException`, y `PA2Controller` lo captura mostrando un diago.
      - Generación **lazy** de registros de commuting: metodo que comprueba antes de cada cálculo de scope 3 si existen registros en `COMMUTING_EMPLEADO` para el período consultado. Si no existen, genera el snapshot y los persiste.
13. El soft-delete de empleados bloqueaba la eliminación de departamentos y empresas desde la UI.
    - **Causa**: El esquema usaba FKs estrictas sin `ON DELETE CASCADE`. El soft-delete de `EMPLEADO` (campo `fecha_baja`) no elimina la fila física, por lo que la FK seguía activa y PostgreSQL rechazaba el `DELETE` del padre con `PSQLException: violates foreign key constraint`. La guarda de la UI solo contaba empleados activos (`fecha_baja IS NULL`), dejando pasar el intento de borrado que luego fallaba en la BD.
    - **Solución**: Se añade `ON DELETE CASCADE` a las FKs del árbol de dependencias.
      - Los empleados mantienen soft-delete para preservar historial de commuting y responsabilidades). 
      - Cuando el administrador decide eliminar explícitamente un departamento o empresa, el CASCADE limpia toda la jerarquía, incluyendo empelados (tienen que estar dados de baja o en otro departamento).
      - `FACTOR_EMISION` y `DIRECCION` no tienen CASCADE
14. La UI se congelaba al cargar las pestañas de Gráficos y Consumos (especialmente con "Año completo").
    - **Causa**: Problema **N+1 queries** derivado del patrón de composición en los DAOs. 
        Cada llamada a `getHuellaTotalEmpresaMes()` genera una cadena de queries (~84 queries para 2 dept), además los gráficos de barras y líneas las repetían por separado (~168 queries en total). Todo en el hilo de JavaFX, bloqueando la UI.
    - **Causa raíz estructural**: la decisión de diseño *"Composición sobre Almacenamiento de FK"* (cada DAO reconstruye objetos completos con sus relaciones) hace que los métodos de servicio encadenen llamadas DAO en lugar de ejecutar una sola query con JOINs. Es eficiente para operaciones CRUD puntuales pero genera N+1 en operaciones de reporting.
    - **Solución aplicada**:
      - `cargarGraficos()` y `cargarConsumos()` en PA1Controller y PR1Controller movidos a hilos de fondo (`Task<Void>`) 
      - En PA1Controller, la matriz `dept × mes` se calcula una sola vez y se reutiliza para los tres gráficos, eliminando el trabajo duplicado.
    - **Mejora FUTURA**: añadir métodos de *reporting* en los DAOs con queries SQL específicas (JOIN + GROUP BY) para los casos de uso de gráficos, evitando el N+1 en origen. No se aplica ahora porque el volumen de datos actual no lo justifica.

15. **[MEJORA FUTURA]** Los registros de commuting se generan con los empleados activos en el momento de la consulta, no con los que estaban activos durante el mes del período histórico.
    - **Causa**: Se filtra por `fecha_baja IS NULL`. Si un empleado se da de baja hoy y después se consulta un mes pasado que aún no tenía snapshot, ese empleado no aparecerá en el registro aunque sí estuviera activo ese mes.
    - **Mejora**: **Lista de empleados por período**: la pestaña Trabajadores de PR1 debe mostrar los empleados que estaban de alta ese mes concreto, no los activos en este momento. Requiere un nuevo método  que filtre solo a los empleados activos en ese mes.

16. Los DAOs mezclaban validación de reglas de negocio con lógica de persistencia, violando la separación de capas.
    - **Causa**: Cada DAO tenía un métodos para validar que comprobaban nulos y formatos antes de ejecutar la query. Esto colocaba lógica de negocio dentro de la capa DAO en lugar de la de servicio. 
    - **Solución**: Se distribuye la validación en tres niveles según su naturaleza:
      1. **Capa Service**: valida campos obligatorios de negocio (nulos, blancos, relaciones requeridas) antes de llamar al DAO.
      2. **`Validador`**: validar todos sus campos obligatorios. La UI nunca gestiona `Direccion` directamente, El formulario llama a `Validador` directamente (en este caso estan todas las comprobaciones aqui).
      3. **Capa DAO**: los `catch (SQLException e)` comprueban el `SQLState` de PostgreSQL y relanza como `IllegalArgumentException` con mensaje legible:
         - `23505` (unique_violation) - `23502` (not_null_violation)

17. Credenciales de base de datos en texto plano en el código fuente.
    - **Causa**: `DatabaseManager` tenía la URL, el usuario y la contraseña de PostgreSQL como constantes `static final String` directamente en el código. Al estar versionadas en Git, cualquier persona con acceso al repositorio podía leer las credenciales.
    - **Solución**: Las tres variables se mueven al fichero `.env` y se leen en tiempo de arranque con `Dotenv.load()`.
      `DatabaseManager` pasa a leer `dotenv.get("DB_URL")`, `dotenv.get("DB_USER")` y `dotenv.get("DB_PASSWORD")`. El fichero `.env` está en `.gitignore` y nunca se sube al repositorio.
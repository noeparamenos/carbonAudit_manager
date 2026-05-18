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
6. ~~Posible Error al Actualizar distancia al trabajo en el Servicio calculo huella.~~ 
   - **Causa**: `AsignarDistanciaTrabajo()` solo geocodificaba si `latitud == null`, por lo que cambios de dirección no se reflejaban en el cálculo.
   - **Solución**: Ver error 11.
7. Riesgo de seguridad: cualquier usuario puede seleccionar cualquier rol o responsable sin autenticación.
   - **Causa**: La aplicación no dispone de sistema de login. Cualquiera puede elegir el rol Administrador o responsable activo.
   - **Solución**: Implementar un sistema de autenticación (usuario + contraseña) que determine el rol y el contexto automáticamente al iniciar sesión, eliminando las pantallas de selección de rol.
8. La ventana P0 no se centra correctamente en el primer arranque en frío desde IntelliJ.
   - **Causa**: En Linux, el gestor de ventanas reposiciona la ventana después de que JavaFX la coloca, ignorando cualquier `setX`/`setY` o `centerOnScreen()` previos al `show()`. El problema se agrava al lanzar desde IntelliJ por la latencia extra del agente IDE.
   - **Solución**: Llamar a `Platform.runLater()` tras `show()` para encolar el centrado después del primer ciclo de renderizado, cuando la ventana ya tiene posición real. Se usa `Screen.getPrimary().getVisualBounds()` para calcular `setX`/`setY` manualmente. Funciona en Linux y Windows.
9. `TableView.CONSTRAINED_RESIZE_POLICY` no se puede referenciar desde FXML en JavaFX 21.
   - **Causa**: El loader FXML de JavaFX 21 no puede convertir el string "TableView.CONSTRAINED_RESIZE_POLICY" al tipo `Callback` que espera `columnResizePolicy`.
   - **Solución**: Eliminar el atributo del FXML y asignarlo en el controlador desde `initialize()` con `tablaEmpresas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN)`.
10. Borrar un empleado destruía su historial de commuting y mandatos de responsabilidad.
    - **Causa**: El esquema original no contemplaba el ciclo de vida del empleado. Un `DELETE` en `EMPLEADO` propagaba en cascada (o fallaba por FK) eliminando o bloqueando los registros de `COMMUTING_EMPLEADO` y `RESPONSABLE`, que son datos de auditoría necesarios para reproducir cálculos de huella de períodos pasados.
    - **Solución**: Implementar **soft delete** añadiendo `fecha_alta` y `fecha_baja` a la tabla `EMPLEADO`. Al dar de baja se registra la fecha en lugar de borrar el registro. 
      - Las consultas filtran `WHERE fecha_baja IS NULL`. 
      - El método `darDeBaja()` encapsula esta lógica; el `delete()` se mantiene para en tests.
11. Al modificar la dirección de un empleado o departamento, la distancia al trabajo no se recalculaba.
    - **Causa**: `AsignarDistanciaTrabajo()` solo llamaba a `completarCoordenadas()` cuando `latitud == null || longitud == null`. Como los controladores no detectaban si la dirección había cambiado, las coordenadas anteriores persistían y la distancia calculada era incorrecta.
    - **Solución**: `AsignarDistanciaTrabajo()` en `ServicioCalculoHuella` llama siempre a `completarCoordenadas()` para ambas direcciones (empleado y departamento), sin condición.
13. Varios controladores llamaban directamente a DAOs, violando la separación de capas. Además, `PA1Controller` contenía lógica de negocio (cálculo del total de emisiones).
    - **Causa**: Los controladores instanciaban y llamaban a DAOs directamente para obtener listas y persistir resultados. `PA1Controller.cargarConsumos()` calculaba además el total sin commuting con `stream().reduce()`. `PA2Controller` llamaba a `EmpleadoDAO` y `DireccionDAO` en los callbacks `onSucceeded` de los hilos asíncronos, y también en `onGuardarEmpleado()` y `onDarDeBaja()` para persistir el empleado.
    - **Solución (fase 1)**: Se añaden métodos de acceso a `ServicioCalculoHuella` que encapsulan las llamadas al DAO, manteniendo la regla Controller → Service → DAO:
      - `getConsumosMensuales(idDept, mes, anio)` — consumos de un departamento (`PR1Controller`)
      - `getConsumosMensualesEmpresa(idEmpresa, mes, anio)` — consumos mensuales de empresa (`PA1Controller`)
      - `getConsumosAnualesEmpresa(idEmpresa, anio)` — consumos anuales de empresa (`PA1Controller`)
      - `getTotalConsumos(consumos)` — total sin commuting; elimina lógica de negocio del controlador (`PA1Controller`)
    - **Solución (fase 2)**: Se crea `ServicioGestionEmpleado` para centralizar el CRUD de empleados y la persistencia de coordenadas, separando estas responsabilidades de `ServicioCalculoHuella` (que queda dedicado exclusivamente a cálculos de huella):
      - `crearEmpleado(empleado)` — INSERT de nuevo empleado (`PA2Controller`)
      - `actualizarEmpleado(empleado)` — UPDATE de empleado existente (`PA2Controller`)
      - `darDeBajaEmpleado(idEmpleado, fecha)` — soft delete (`PA2Controller`)
      - `getEmpleadosDepartamento(idDept)` — lista de empleados activos (`PA2Controller`)
      - `persistirCoordenadas(dir)` — persiste coordenadas geocodificadas tras geocodificar la dirección del departamento (`PA2Controller`)
      - `persistirResultadoDistancia(empleado)` — persiste distancia al trabajo + coordenadas de ambas direcciones tras el cálculo ORS (`PA2Controller`)
    - En el hilo asíncrono de cálculo de distancia, `Task.call()` invoca `servicioHuella.AsignarDistanciaTrabajo()` (cálculo puro) seguido de `servicioGestion.persistirResultadoDistancia()` (persistencia), respetando la separación de responsabilidades.
12. `PSQLException` por clave duplicada en `ConsumoMensualDAO.create()` no llegaba al controlador.
    - **Causa**: El `catch (SQLException e)` del DAO solo hacía `printStackTrace()` y no relanzaba nada. El controlador capturaba `IllegalArgumentException` para mostrar avisos al usuario, pero nunca recibía el error de la restricción `UNIQUE(id_dept, id_factor, mes, anio)`.
    - **Solución**: En el `catch` del DAO se comprueba el SQLState (`23xxx` = violación de integridad) y se relanza como `IllegalArgumentException` con mensaje legible. Cualquier otro `SQLException` se relanza como `RuntimeException`.
14. El Alcance 3 (commuting) siempre mostraba 0 aunque estuviera habilitado para el departamento.
    - **Causa**: `ServicioCalculoHuella` calculaba el scope 3 consultando la tabla `COMMUTING_EMPLEADO`, pero esa tabla nunca se llenaba desde la UI. Los empleados tienen `distanciaTrabajo` calculada (via ORS), pero ese dato no se trasladaba automáticamente a `COMMUTING_EMPLEADO`. Además, `DepartamentoDAO.update()` absorbía errores SQL silenciosamente (`e.printStackTrace()`), por lo que fallos al guardar `incluir_alcance3` pasaban desapercibidos como guardados exitosos.
    - **Solución**:
      - `DepartamentoDAO.update()` ahora relanza el `SQLException` como `RuntimeException`, y `PA2Controller` lo captura mostrando un diálogo de error al usuario.
      - Se introduce generación **lazy** de registros de commuting: `ServicioCalculoHuella.garantizarRegistrosCommuting()` comprueba antes de cada cálculo de scope 3 si existen registros en `COMMUTING_EMPLEADO` para el período consultado. Si no existen (y el período no es futuro), genera el snapshot desde los datos actuales de los empleados activos y los persiste. Las consultas posteriores al mismo período usan los registros ya guardados, preservando el historial.
      - Se elimina `chkIncluirCommuting` de PA1 (era un toggle de UI sin persistencia que confundía con el flag `incluirAlcance3` por departamento). El total de PA1 ahora llama siempre a `getHuellaTotalEmpresaMes()` / `getHuellaAnualEmpresa()`, que respetan la configuración por departamento.
16. El soft-delete de empleados bloqueaba la eliminación de departamentos y empresas desde la UI.
    - **Causa**: El esquema usaba FKs estrictas sin `ON DELETE CASCADE`. El soft-delete de `EMPLEADO` (campo `fecha_baja`) no elimina la fila física, por lo que la FK seguía activa y PostgreSQL rechazaba el `DELETE` del padre con `PSQLException: violates foreign key constraint`. La guarda de la UI solo contaba empleados activos (`fecha_baja IS NULL`), dejando pasar el intento de borrado que luego fallaba en la BD.
    - **Solución**: Se añade `ON DELETE CASCADE` a las FKs del árbol de dependencias en `create_db_tables.sql` y se aplica mediante migración a la BD existente:
      - `DEPARTAMENTO.id_empresa` → CASCADE
      - `EMPLEADO.id_dept` → CASCADE
      - `CONSUMO_MENSUAL.id_dept` → CASCADE
      - `RESPONSABLE.id_dept` e `id_empleado` → CASCADE
      - `COMMUTING_EMPLEADO.id_empleado` → CASCADE
    - **Comportamiento resultante**: los empleados mantienen soft-delete durante su ciclo de vida normal (preserva historial de commuting y responsabilidades). Cuando el administrador decide eliminar explícitamente un departamento o empresa, el CASCADE limpia toda la jerarquía de una sola vez. El flujo correcto en la UI es: dar de baja a todos los empleados activos → eliminar el departamento (el CASCADE borra el resto).
    - **Nota**: `FACTOR_EMISION` y `DIRECCION` no tienen CASCADE — los factores son datos de referencia que no deben borrarse por accidente, y las direcciones huérfanas no causan errores de FK.
    - **Decisión de diseño (Opción B)**: la aplicación mezcla conscientemente soft-delete y hard-delete para dos casos de uso distintos:
      - **Soft-delete en `EMPLEADO`** (`fecha_baja`): modela un evento de negocio (el empleado causa baja). Su historial de commuting y mandatos de responsabilidad se preserva para reproducir cálculos de períodos pasados.
      - **Hard-delete con CASCADE en `DEPARTAMENTO` y `EMPRESA`**: modela una acción administrativa destructiva e irreversible. Al borrar un departamento se destruye todo su historial de auditoría (empleados, consumos, commutings). Esta pérdida es aceptada y esperada — un departamento activo con historial real nunca debería eliminarse.
      - **Consecuencia**: el soft-delete de empleados tiene sentido dentro del ciclo de vida de un departamento, pero no sobrevive a la eliminación del departamento padre. La UI debe advertir explícitamente de esta pérdida antes de confirmar el borrado.

15. **[MEJORA FUTURA]** Los registros de commuting se generan con los empleados activos en el momento de la consulta, no con los que estaban activos durante el mes del período histórico.
    - **Causa**: `garantizarRegistrosCommuting()` usa `EmpleadoDAO.findAllByDepartamento()`, que filtra por `fecha_baja IS NULL` (activos ahora). Si un empleado se da de baja hoy y después se consulta un mes pasado que aún no tenía snapshot, ese empleado no aparecerá en el registro aunque sí estuviera activo ese mes.
    - **Mejora propuesta** (dos partes):
      1. **Lista de empleados por período**: la pestaña Trabajadores de PR1 debe mostrar los empleados que estaban de alta ese mes concreto, no los activos en este momento. Requiere un nuevo método `EmpleadoDAO.findAllByDepartamentoEnPeriodo(idDepartamento, mes, anio)` que filtre por `fecha_alta <= último día del mes` y (`fecha_baja IS NULL` OR `fecha_baja >= primer día del mes`).
      2. **Cálculo de commuting por período**: `garantizarRegistrosCommuting()` debe llamar a ese mismo método en lugar de `findAllByDepartamento()`, de forma que el snapshot refleje únicamente los empleados que estaban activos durante el mes auditado.
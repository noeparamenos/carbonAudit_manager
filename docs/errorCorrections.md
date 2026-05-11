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
6. Posible Error al Actualizar distancia al trabajo en el Servicio calculo huella.
   - **Causa**: Ahora solo se actualiza si no tiene asignadas coordenadas previamente
   - **Solución**: O bien se hace otro metodo para actualizar si cambia de dirección o bien siempre (eliminando el if)
7. Riesgo de seguridad: cualquier usuario puede seleccionar cualquier rol o responsable sin autenticación.
   - **Causa**: La aplicación no dispone de sistema de login. Cualquiera puede elegir el rol Administrador o responsable activo.
   - **Solución**: Implementar un sistema de autenticación (usuario + contraseña) que determine el rol y el contexto automáticamente al iniciar sesión, eliminando las pantallas de selección de rol.
8. La ventana P0 no se centra correctamente en el primer arranque en frío desde IntelliJ.
   - **Causa**: En el primer lanzamiento la JVM inicializa JavaFX mientras el gestor de ventanas aún no ha terminado de posicionar la ventana. `centerOnScreen()`, `Platform.runLater()` y `setOnShown()` fallan en ese primer ciclo porque la ventana aún no tiene posición definitiva en pantalla.
   - **Solución pendiente**: Investigar uso de `Screen.getPrimary().getVisualBounds()` para calcular y fijar manualmente `setX`/`setY`, o aplicar un retardo controlado tras `setOnShown`.
9. `TableView.CONSTRAINED_RESIZE_POLICY` no se puede referenciar desde FXML en JavaFX 21.
   - **Causa**: El loader FXML de JavaFX 21 no puede convertir el string "TableView.CONSTRAINED_RESIZE_POLICY" al tipo `Callback` que espera `columnResizePolicy`.
   - **Solución**: Eliminar el atributo del FXML y asignarlo en el controlador desde `initialize()` con `tablaEmpresas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN)`.
10. Borrar un empleado destruía su historial de commuting y mandatos de responsabilidad.
   - **Causa**: El esquema original no contemplaba el ciclo de vida del empleado. Un `DELETE` en `EMPLEADO` propagaba en cascada (o fallaba por FK) eliminando o bloqueando los registros de `COMMUTING_EMPLEADO` y `RESPONSABLE`, que son datos de auditoría necesarios para reproducir cálculos de huella de períodos pasados.
   - **Solución**: Implementar **soft delete** añadiendo `fecha_alta DATE NOT NULL` y `fecha_baja DATE` a la tabla `EMPLEADO`. Al causar baja se registra la fecha en lugar de borrar el registro físicamente. Las consultas operativas filtran `WHERE fecha_baja IS NULL`. El método `darDeBaja()` en `EmpleadoDAO` encapsula esta lógica; el `delete()` se mantiene únicamente para limpieza en tests.
11. Error de compilación en los tests al añadir `fecha_alta` al modelo `Empleado`.
   - **Causa**: Al refactorizar `Empleado` se eliminó el constructor `Empleado(String, FactorEmision, int, Direccion, Departamento)` y se reemplazó por uno que incluye `LocalDate fechaAlta` como segundo parámetro. Los tests `TestCalculoHuellaDepartamento` y `TestServicioCalculoCommuting` seguían usando el constructor antiguo.
   - **Solución**: Añadir `import java.time.LocalDate` y pasar `LocalDate.now()` como segundo argumento en las dos llamadas afectadas.
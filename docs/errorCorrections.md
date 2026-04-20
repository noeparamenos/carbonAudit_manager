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
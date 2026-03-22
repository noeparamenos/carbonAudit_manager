1. La implementación de la permisividad de NOT NULL para empresas se vuelve tediosa en la clase EmpresaDAO
    - Se ha exigido que la empresa tenga una dirección (not null en la BD)
2. El calculo de direcciones sin numero de calle es impreciso
    - Se ha añadido el número de calle a la dirección (int) a la BD y la clase modelo
3. Ejecución de tests de las clases DAO: Errores de integridad no bien descritos
    - Se han comprobado las restricciones de integridad en cada una de las clases DAO
    - Dan información al usuario sobre la regla que se está incumpliendo 
    - Sirven de última barrera antes de intentar meter datos en la BD que sean susceptibles de generar un error
4. La API ORS es imprecisa a la hora de trabajar con direcciones situadas en pueblos y pequeñas ciudades
   - Se propone el uso de otra API más precisa (como google) aunque se ha de valorar el gasto que esto conlleva.

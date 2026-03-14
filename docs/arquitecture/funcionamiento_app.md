

# 1. Fase de Configuración (El "Set-up")
- Antes de calcular nada, el administrador define la estructura de la empresa. Es una tarea que se hace una sola vez o cuando hay cambios.

- Alta de Departamentos: Se crean áreas (ej. Producción, Administración). Aquí es donde marcas el booleano de "Incluir Alcance 3" que diseñamos.

- Mapa de Empleados: Al añadir un empleado, introduces su dirección. Java llama a la API de mapas, calcula la distancia al departamento y guarda ese dato fijo en la base de datos para no volver a calcularlo.

- Asignación de Responsables: Vinculas a un empleado como el "dueño" de los datos de su departamento para la trazabilidad.

# 2. Ciclo de Carga Mensual (La "Rutina")
- Cada mes, el usuario responsable entra en la aplicación para alimentar el sistema. Hemos diseñado la UX para que sea a prueba de errores:

- Consumos Fijos (Sedes): El usuario elige el departamento y selecciona un recurso (Luz, Gas, Gasoil). Introduce la cantidad de la factura. El sistema ya sabe, gracias a la tabla de factores, que la Luz es Alcance 2 y el Gas es Alcance 1.

- Movilidad (Commuting): Solo si hay cambios (ej. un empleado empieza a teletrabajar o cambia de coche), se ajusta su perfil. Si no, el sistema reutiliza la distancia y el factor de su vehículo por defecto.

# 3. El Motor de Cálculo (La "Caja Negra")
- Cuando el usuario pulsa el botón "Calcular Huella", la aplicación realiza tres sumatorios paralelos:

    - Directas: Suma todos los consumos de Alcance 1 de la tabla consumo_mensual.

    - Energéticas: Suma los consumos de Alcance 2.

    - Indirectas (Opcional): Si el departamento tiene el permiso activado, calcula la huella de todos sus empleados usando la fórmula:

        - Huella_{mensual} = (Distancia_{km} \times 2) \times D\text{í}as_{presenciales} \times Factor_{emisi\text{ó}n}$$
 
# 4. Visualización y Decisión (El "Dashboard")
Mediante JAVAFX El sistema  ofrece contexto:

- Gráficos Evolutivos: Muestra si la huella está subiendo o bajando mes a mes (Comparativa temporal).

- Reparto por Alcances: Un gráfico de tarta que permite ver si el problema es la electricidad o el transporte de los empleados.

- Simulador de Decisiones: El usuario puede jugar con los datos. Por ejemplo: "¿Qué pasaría si el departamento de Ventas teletrabajara 2 días más a la semana?". El sistema recalcula la proyección de Alcance 3 instantáneamente.

- Petición: El usuario solicita el informe del Mes X.

    - Extracción: El DAO ejecuta un JOIN entre consumos, empleados y factores de emisión.
    - Transformación: Los objetos Java (POJOs) convierten los datos planos en objetos con lógica.
    - Presentación: La capa Controller envía los resultados a la vista de JavaFX para renderizar los gráficos.
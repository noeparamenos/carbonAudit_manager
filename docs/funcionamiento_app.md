

# 1. Fase de Configuración
El administrador define la estructura de la empresa.
   - Alta de la Empresa: Se asignan los datos de la empresa (Cif, nombre...) y una Dirección
     - Al guardar el servicio de Geolocalización le asigna las coordenadas.
   - Alta de Departamentos: Se crean áreas (ej. Producción, Administración). Se asigna una **Dirección**
     - Al guardar el servicio de Geolocalización le asigna las coordenadas.
   - Alta de Empleados: Al añadir un empleado, se introduce su **dirección**. 
     - Antes de guardar la API de geolocalización, calcula:
       - Las coordenadas
       - La distancia al departamento en km y guarda ese dat en la BD.
   - Alta de Responsables: Vincula a un empleado como el "responsable" de los datos de su departamento para la trazabilidad.
     - Permite la asignación de la fecha de incio y fin
   - Alta de Factores de emisión: Se agregan los consumos propios de la empresa (ej. Luz, gas...) y los médios de desplazamiento de los trabajadores
     - Se configura cada uno de ellos en relación con el factor de emisión basado en los datos oficiales.

---

# 2. Ciclo de Carga Mensual 
Cada mes, el usuario responsable entra en la aplicación para alimentar el sistema.
  - **Consumos Fijos** (Departamentos): 
    1. El usuario elige el departamento y selecciona un recurso (Luz, Gas, Gasoil). 
      - Se permite la creación de un nuevo factor de emisión si fuera necesario
    2. Introduce la cantidad de la factura (litros...). 
       - El sistema ya sabe, gracias a la tabla de factores, el alcance de cada un de los consumibles (ej. luz->2).
  - **Movilidad (Commuting)**: Solo si hay cambios. (Si no, el sistema reutiliza la distancia y el factor de su vehículo por defecto)
    - Un empleado empieza a teletrabajar
    - Un empleado cambia de método de desplazamiento. 

---

# 3. El Motor de Cálculo de la Huella
- Cuando el usuario pulsa el botón **Calcular Huella**, la aplicación realiza tres sumas:
    - **Directas**: Suma todos los consumos de Alcance 1 de la tabla consumo_mensual.
    - **Energéticas**: Suma los consumos de Alcance 2.
    - **Indirectas** (Opcional): Si el departamento tiene el permiso activado, calcula la huella de todos sus empleados usando la fórmula:

---
 
# 4. Visualización y Decisión. Dashboard
Mediante JAVAFX El sistema  ofrece contexto:

  - Gráficos Evolutivos: Muestra si la huella está subiendo o bajando mes a mes.
  - Reparto por Alcances: Un gráfico de tarta que permite ver sí que alcance es el que está contaminando más.
  - Simulador de Decisiones: Simular cambio de transporte o días de teletrabajo 
  - Generación de informes: El usuario solicita el informe del Mes X.

---
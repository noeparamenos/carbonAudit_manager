
# 🏛️ Arquitectura de Clases – CarbonAudit Manager

Este documento describe cómo se organiza la **lógica orientada a objetos** en el sistema, para mantener el código escalable y profesional mediante polimorfismo y separación de responsabilidades.

---

## 1. Interfaz Madre: `Consumible`

Es el contrato que garantiza que **cualquier objeto que emita CO₂** pueda ser procesado por los gráficos.

**Método principal:**

```java
double calcularHuella();
```

---

## 2. Clases de Implementación

### FactorEmision

* Clase "catálogo" que representa una fila de la tabla de factores.
* Atributos: `nombre`, `unidad`, `valor`, `alcance`.
* No realiza cálculos, sirve como referencia.

### ConsumoEnergia (Alcance 1 y 2)

* Representa las facturas de energía de la empresa.
* Atributos: `FactorEmision factor`, `double cantidad`, `int mes`, `int anio`.
* **Cálculo de huella:**

  ```java
  cantidad * factor.getValor()
  ```

### TransporteEmpleado (Alcance 3)

* Representa la movilidad de los empleados.
* Atributos: `FactorEmision factor`, `double distanciaKm`, `int diasPresenciales`.
* **Cálculo de huella:**

  ```java
  (distanciaKm * 2) * diasPresenciales * factor.getValor()
  ```

---

## 3. Relaciones entre Objetos

La conexión de objetos en memoria de Java es la siguiente:

| Relación                          | Descripción                                                                   |
| --------------------------------- | ----------------------------------------------------------------------------- |
| Empresa 1 ⮕ N Departamento        | Una empresa tiene varios departamentos                                        |
| Departamento 1 ⮕ N Empleado       | Cada empleado pertenece a un departamento                                     |
| Departamento 1 ⮕ N ConsumoEnergia | Cada departamento tiene sus consumos de luz/gas                               |
| Empleado 1 ⮕ 1 TransporteEmpleado | Cada empleado tiene configurado su transporte                                 |
| Consumible N ⮕ 1 FactorEmision    | Tanto transporte como energía apuntan a un factor para obtener el coeficiente |

---

## 4. Motor de Cálculo (Capa `service`)

Se recomienda **no poner lógica pesada en la interfaz**.

**Clase principal:** `CalculoService`

**Métodos principales:**

### `obtenerHuellaDepartamento(idDept)`

1. Suma todos los `ConsumoEnergia` del departamento.
2. Si `incluir_alcance3` es `true`, recorre todos los `Empleado` del departamento y suma sus `TransporteEmpleado`.

### `obtenerHuellaPorAlcance(idEmpresa, alcance)`

* Filtra todos los `Consumible` de la empresa según el nivel (1, 2 o 3).
* Se utiliza para generar los gráficos de tarta o barras.

---

## 5. Estructura de Paquetes Recomendada

Para mantener el proyecto ordenado en IntelliJ:

```text
src/main/java/com/carbonaudit/
├── model/          # POJOs e Interfaces (Empleado, Departamento, Consumible...)
├── dao/            # Clases de conexión a DB (EmpleadoDAO, FactoresDAO...)
├── service/        # Lógica de cálculo y APIs de mapas
├── controller/     # Controladores de las vistas JavaFX
└── MainApp.java    # Clase principal para lanzar la app
```

---

## 6. Resumen de Flujo de Datos

| Etapa       | Descripción                                                                                                                       |
| ----------- | --------------------------------------------------------------------------------------------------------------------------------- |
| **Carga**   | Al abrir la app, `FactoresDAO` lee la tabla de la DB y llena una lista de objetos `FactorEmision`.                                |
| **Entrada** | Cuando se crea un empleado, `EmpleadoService` llama a la API de mapas y guarda la distancia en el objeto `Empleado` (y en la DB). |
| **Proceso** | Al solicitar un informe, `CalculoService` pide los datos al DAO, crea los objetos `Consumible` al vuelo y suma los resultados.    |
| **Salida**  | Los resultados se pasan a un `PieChart` o `BarChart` de JavaFX.                                                                   |

```

---

Si quieres, puedo **hacer una versión completa de documentación de arquitectura** que combine:

- Diccionario de datos  
- ER Diagram (Mermaid)  
- Arquitectura de clases  
- Flujo de cálculo  


```

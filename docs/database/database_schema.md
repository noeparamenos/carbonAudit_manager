# Database Schema – CarbonAudit Manager

## Tablas principales

* Dirección
* Empresa
* Departamento
* Empleado
* Responsable
* Factores de Emisión
* Consumo Mensual
* Commuting Empleado

---

# Relaciones principales

### Dirección → Empresa

Relación: **Uno a Muchos**

Una dirección puede estar asociada a **varias empresas** (ej. sedes compartidas o reutilización de direcciones).

---

### Dirección → Departamento

Relación: **Uno a Uno**

Cada departamento tiene una **ubicación física** utilizada para cálculos de movilidad y emisiones.

---

### Dirección → Empleado

Relación: **Uno a Uno**

Los empleados tienen una **dirección de residencia** que permite calcular:

* distancia al trabajo
* emisiones por desplazamiento

---

### Empresa → Departamento

Relación: **Uno a Muchos**

Una empresa puede tener múltiples departamentos.

---

### Departamento → Empleado

Relación: **Uno a Muchos**

Cada departamento puede tener múltiples empleados.

---

### Departamento ↔ Empleado (Responsable)

Relación: **Muchos a Muchos histórica**

Se gestiona mediante la tabla **Responsable**.

Permite:

* mantener historial de responsables
* auditoría y cumplimiento normativo (CSRD)
* identificar responsable actual

---

### Departamento → Consumo Mensual

Relación: **Uno a Muchos**

Cada departamento registra consumos energéticos mensuales.

Estos datos alimentan:

* cálculo de emisiones
* dashboard de huella de carbono

---

### Factores de Emisión → Consumo Mensual

Relación: **Uno a Muchos**

Cada consumo se asocia a un **factor de emisión** que permite calcular las emisiones de CO₂ equivalente.

---

### Empleado → Commuting

Relación: **Uno a Muchos**

Permite almacenar el **histórico de desplazamientos mensuales** del empleado.

Se utiliza para:

* cálculo de emisiones de **Alcance 3**
* análisis de teletrabajo
* evolución del transporte sostenible

---

### Factores de Emisión → Commuting

Relación: **Uno a Muchos**

Cada registro de commuting utiliza un factor de emisión según el **medio de transporte**.

---

# Modelo conceptual

```
    Empresa
         └── Departamento
                ├── Empleado
                │      └── Commuting
                ├── Consumo Mensual
                └── Responsable
```

---


### Índices recomendados
- Posibles mejoras en el **rendimiento para las consultas**

Tabla **Consumo Mensual**

* índice `(anio, mes)`
* índice `(id_dept)`

Tabla **Commuting**

* índice `(id_empleado)`
* índice `(anio, mes)`


---


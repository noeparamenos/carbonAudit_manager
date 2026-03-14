# Database Schema – CarbonAudit Manager

## Tablas principales

- Empresa
- Departamento
- Empleados
- Responsables
- Factores de Emisión
- Consumos Mensuales
- Commuting Empleados

---

## Relaciones principales

### Empresa → Departamento
Relación: Uno a Muchos

Una empresa puede tener varios departamentos.

### Departamento → Empleados
Relación: Uno a Muchos

Un departamento puede tener múltiples empleados.

### Departamento ↔ Empleados (Responsables)
Relación histórica gestionada mediante la tabla Responsables.

---

# Modelo conceptual

Empresa
 └── Departamento
      ├── Empleados
      │     └── Commuting
      ├── Consumos Mensuales
      └── Responsables
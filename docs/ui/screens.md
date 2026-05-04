# Pantallas

Listado de las pantallas de la aplicación.

---

## P0 · Selección de rol

- **Actor:** todos
- **Objetivo:** elegir con qué rol se entra en la aplicación.
- **Elementos:** logo, dos tarjetas grandes ("Administrador", "Responsable"), versión.
- **Acciones:**
  - Clic en *Administrador* → PA1
  - Clic en *Responsable* → PR0

---

# Flujo Administrador

## PA1 · Lista de empresas

- **Actor:** Administrador
- **Objetivo:** ver y seleccionar la empresa con la que trabajar.
- **Elementos:** lista/tabla de empresas (nombre, CIF), botón de nueva empresa.
- **Acciones:**
  - Clic en empresa → PA2 (departamentos de esa empresa)
  - Nueva empresa → formulario panel lateral

## PA2 · Vista empresa

- **Actor:** Administrador
- **Objetivo:** punto central de gestión y consulta de una empresa; tres tabs.
- **Elementos:** breadcrumb (empresa actual), selector de mes/año, tres tabs.

### Tab · Departamentos

- **Objetivo:** listar y gestionar los departamentos de la empresa.
- **Elementos:** tabla de departamentos (nombre, nº empleados, responsable activo).
- **Acciones:** Clic en departamento → PA3 · Nuevo departamento → formulario panel lateral.

### Tab · Consumos

- **UC:** UC_Consumo
- **Objetivo:** ver el consumo mensual agregado de toda la empresa, desglosado por departamento.
- **Elementos:** selector mes/año, tabla (departamento, recurso, cantidad, unidad, emisión CO₂e), totales por alcance y por departamento.
- **Acciones:** filtrar por mes/año · Exportar CSV.

### Tab · Gráficos

- **UC:** UC_Dashboard
- **Objetivo:** visualización histórica de la huella de toda la empresa.
- **Elementos:** gráfico de evolución (12 meses), tarta por alcance, comparativa entre departamentos.
- **Acciones:** —

## PA3 · Gestión de departamento

- **Actor:** Administrador
- **Objetivo:** gestionar empleados y el responsable de un departamento concreto.
- **Elementos:** breadcrumb (empresa › departamento), dos secciones:
  - **Empleados** — tabla con todos los trabajadores del departamento.
  - **Responsable** — responsable activo actual + historial de mandatos.
- **Acciones:**
  - Nuevo empleado → formulario panel lateral (datos personales, dirección, vehículo por defecto)
  - Editar empleado → mismo panel
  - Eliminar empleado → modal de confirmación (bloqueado si tiene consumos registrados)
  - Asignar responsable → selector de empleados del departamento + fechas del mandato
  - Finalizar mandato activo

---

# Flujo Responsable

## PR0 · Selección de responsable

- **Actor:** Responsable
- **Objetivo:** identificarse eligiendo el propio nombre de la lista de mandatos activos.
- **Elementos:** lista de responsables con mandato activo (nombre, departamento, empresa).
- **Acciones:** clic en nombre → PR1 (vista responsable filtrada a ese departamento)
- **Nota:** sustituye al login mientras la app no tenga autenticación.

## PR1 · Vista responsable

- **Actor:** Responsable
- **Objetivo:** pantalla principal del responsable; acceso a todas sus funciones mediante tabs.
- **Elementos:** cabecera (nombre responsable, departamento, empresa), selector de mes/año, cuatro tabs.

### Tab · Consumos mensuales

- **UC:** UC_Consumo
- **Objetivo:** registrar las cantidades de cada recurso consumido en el mes.
- **Elementos:** tabla editable (recurso, unidad, cantidad, factor de emisión, alcance, emisión calculada), totales por alcance.
- **Acciones:** Guardar · Duplicar mes anterior.

### Tab · Trabajadores

- **UC:** UC_Empleado
- **Objetivo:** consultar y gestionar los empleados del departamento.
- **Elementos:** tabla (nombre, dirección, vehículo por defecto, distancia al dpto.).
- **Acciones:** Nuevo trabajador · Editar · Eliminar (modal de confirmación).

### Tab · Huella mensual

- **UC:** UC_Huella, UC_Informe
- **Objetivo:** calcular y visualizar el informe de huella de carbono del mes seleccionado.
- **Elementos:** KPIs (total CO₂e, por alcance), desglose por recurso, sección commuting.
- **Acciones:** Calcular huella · Exportar PDF · Exportar CSV.

### Tab · Gráficos

- **UC:** UC_Dashboard
- **Objetivo:** visualización histórica de la huella del departamento.
- **Elementos:** gráfico de evolución (12 meses), tarta por alcance.
- **Acciones:** —

---

## Matriz casos de uso ↔ pantallas

| Caso de uso    | Pantalla principal        | Secundarias |
|----------------|---------------------------|-------------|
| UC_Empresa     | PA1                         | PA2                  |
| UC_Dept        | PA2-Departamentos           | PA3                  |
| UC_Empleado    | PA3, PR1-Trabajadores       | —                    |
| UC_AsignarResp | PA3                         | —                    |
| UC_Consumo     | PR1-Consumos, PA2-Consumos  | —                    |
| UC_Commuting   | PR1-Huella                  | —                    |
| UC_Distancia   | PA3                         | —                    |
| UC_Huella      | PR1-Huella                  | —                    |
| UC_Dashboard   | PR1-Gráficos, PA2-Gráficos  | —                    |
| UC_Informe     | PR1-Huella                  | —                    |

# Wireframe · PA2 · Gestión de departamento

## Layout general

```
┌──────────────────────────────────────────────────────────────────────┐
│  CarbonAudit Manager  Empresas › Empresa Ejemplo › Logística  Admin  │  ← topbar
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Logística                                          [ Editar ]       │
│  Empresa Ejemplo S.L. · C/ Mayor 1, Madrid       [ Eliminar ]        │
│                                                                      │
│  ┌──────────────┬──────────────┐                                     │
│  │  Empleados   │ Responsable  │                                     │
│  └──────────────┴──────────────┘                                     │
│                                                                      │
│  ╔═══════════════════════════════════════════════════════════════╗   │
│  ║  (contenido del tab activo)                                   ║   │
│  ╚═══════════════════════════════════════════════════════════════╝   │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Tab · Empleados (estado normal)

```
┌──────────────────────────────────────────────────────────────────────┐
│  Logística · Empresa Ejemplo S.L.              [ Editar ] [ Eliminar ]│
├──────────────────┬───────────────────────────────────────────────────┤
│ Empleados        │ Responsable                                       │
├──────────────────┴───────────────────────────────────────────────────┤
│                                              [ + Nuevo empleado ]    │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  Nombre        │ Ciudad    │ Días pres. │ Vehículo  │ Dist.(km) │ │
│  ├────────────────┼───────────┼────────────┼───────────┼───────────┤ │
│  │  Ana García    │ Madrid    │ 20         │ Coche     │ 12.4      │ │
│  ├────────────────┼───────────┼────────────┼───────────┼───────────┤ │
│  │  Pedro López   │ Alcorcón  │ 15         │ Metro     │ 8.1       │ │
│  ├────────────────┼───────────┼────────────┼───────────┼───────────┤ │
│  │  ...           │ ...       │ ...        │ ...       │ —         │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  Haz clic en un empleado para editar sus datos.                      │
└──────────────────────────────────────────────────────────────────────┘
```

## Tab · Empleados (con panel lateral — nuevo/editar empleado)

```
┌────────────────────────────────────┬─────────────────────────────────┐
│  Logística  [Editar] [Eliminar]    │  Nuevo empleado          [Cerrar]│
├──────────────────┬─────────────────┤  ───────────────────────────────│
│ Empleados │ Resp.│                 │  Nombre *   [ ________________ ] │
├──────────────────┴─────────────────┤  Días pres.*[ ________________ ] │
│                [+ Nuevo empleado]  │  Vehículo * [      ▾           ] │
│  ┌───────────────────────────────┐ │                                  │
│  │ Nombre  │Ciudad│Días│Veh.│km  │ │  Residencia                      │
│  ├─────────┼──────┼────┼────┼────┤ │  Calle *    [ ________________ ] │
│  │ Ana G.  │Madrid│ 20 │Coch│12.4│ │  Número *   [ ________________ ] │
│  └───────────────────────────────┘ │  Ciudad *   [ ________________ ] │
│                                    │  C. Postal* [ ________________ ] │
│                                    │  Provincia  [ ________________ ] │
│                                    │                                  │
│                                    │  [Guardar] [Dar de baja][Cancelar│
└────────────────────────────────────┴─────────────────────────────────┘
```

---

## Tab · Responsable

```
┌──────────────────────────────────────────────────────────────────────┐
│  Logística · Empresa Ejemplo S.L.              [ Editar ] [ Eliminar ]│
├──────────────────┬───────────────────────────────────────────────────┤
│ Empleados        │ Responsable                                       │
├──────────────────┴───────────────────────────────────────────────────┤
│                                                                      │
│  Responsable activo                                                  │
│  Ana García · desde 01/01/2025    [ Finalizar mandato ] [ Asignar ]  │
│  ─────────────────────────────────────────────────────────────────── │
│                                                                      │
│  Historial de mandatos                                               │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  Empleado          │  Desde          │  Hasta                   │ │
│  ├────────────────────┼─────────────────┼──────────────────────────┤ │
│  │  Ana García        │  01/01/2025     │  (activo)                │ │
│  ├────────────────────┼─────────────────┼──────────────────────────┤ │
│  │  Luis Torres       │  01/03/2024     │  31/12/2024              │ │
│  ├────────────────────┼─────────────────┼──────────────────────────┤ │
│  │  ...               │  ...            │  ...                     │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

## Tab · Responsable (con panel lateral — asignar responsable)

```
┌────────────────────────────────────┬─────────────────────────────────┐
│  Logística  [Editar] [Eliminar]    │  Asignar responsable    [Cerrar] │
├──────────────────┬─────────────────┤  ───────────────────────────────│
│ Empleados │ Resp.│                 │  Empleado * [      ▾           ] │
├──────────────────┴─────────────────┤  Desde *    [ ________________ ] │
│  Responsable activo                │                                  │
│  Ana García        [Finalizar][As.]│                                  │
│  ─────────────────────────────     │                                  │
│  Historial                         │                                  │
│  ┌────────────────────────────┐    │                                  │
│  │ Empleado │ Desde  │ Hasta  │    │                                  │
│  └────────────────────────────┘    │  [ Guardar ]  [ Cancelar ]       │
└────────────────────────────────────┴─────────────────────────────────┘
```

---

## Panel lateral · Editar departamento

```
┌────────────────────────────────────┬─────────────────────────────────┐
│                                    │  Editar departamento    [Cerrar] │
│                                    │  ───────────────────────────────│
│                                    │  Nombre *   [ ________________ ] │
│                                    │  Descripción[ ________________ ] │
│                                    │  ☐ Incluir Alcance 3 (commuting) │
│                                    │                                  │
│                                    │  Ubicación                       │
│                                    │  Calle *    [ ________________ ] │
│                                    │  Número *   [ ________________ ] │
│                                    │  Ciudad *   [ ________________ ] │
│                                    │  C. Postal* [ ________________ ] │
│                                    │  Provincia  [ ________________ ] │
│                                    │                                  │
│                                    │  [ Guardar ]  [ Cancelar ]       │
└────────────────────────────────────┴─────────────────────────────────┘
```

---

## Elementos

| Elemento              | Tipo        | Notas                                                              |
|-----------------------|-------------|--------------------------------------------------------------------|
| Topbar                | HBox        | Breadcrumb: Empresas › NombreEmpresa › NombreDpto (3 niveles)     |
| Cabecera departamento | HBox        | Nombre, dirección, botones Editar / Eliminar                      |
| TabPane               | TabPane     | Dos tabs: Empleados, Responsable                                  |
| **Tab Empleados**     |             |                                                                    |
| Botón nuevo empleado  | Button      | Abre panel lateral vacío                                          |
| Tabla empleados       | TableView   | Columnas: Nombre, Ciudad, Días pres., Vehículo, Distancia (km)    |
| Panel lateral empleado| VBox        | Campos: Nombre, Días, Vehículo, Dirección completa                |
| Botón Dar de baja     | Button      | Solo visible en modo edición; aplica soft delete                  |
| **Tab Responsable**   |             |                                                                    |
| Tarjeta activo        | VBox        | Nombre del responsable activo + botones Finalizar / Asignar       |
| Tabla historial       | TableView   | Columnas: Empleado, Desde, Hasta                                  |
| Panel asignar         | VBox        | ComboBox empleados activos + DatePicker fecha inicio              |
| **Panel Editar dpto** |             |                                                                    |
| Panel editar dpto     | VBox        | Nombre, Descripción, CheckBox Alcance 3, Dirección completa       |

## Comportamiento

- Clic en fila de empleado → abre panel lateral en modo edición con datos pre-rellenados.
- *Dar de baja* → soft delete: registra `fecha_baja`, el empleado desaparece de la tabla activa.
- *Asignar responsable* → cierra el mandato activo (si lo hay) y crea uno nuevo.
- *Finalizar mandato* → modal de confirmación; registra `fecha_fin` en el mandato activo.
- *Eliminar departamento* → bloqueado si tiene empleados activos.
- Guardar empleado/departamento → la geocodificación se lanza en segundo plano (ORS API).
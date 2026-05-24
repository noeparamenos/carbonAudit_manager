# Wireframe · PR1 · Vista responsable

## Layout general

```
┌──────────────────────────────────────────────────────────────────────┐
│  CarbonAudit Manager  Inicio › Selección responsable › Ana García  R │  ← topbar
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Ana García                                                          │
│  Departamento: Logística · Empresa: Empresa Ejemplo S.L.            │
│  Período: [ Enero ▾ ]  [ 2025 ▾ ]                                    │
│                                                                      │
│  ┌──────────────────┬──────────────┬────────────────┬─────────────┐  │
│  │Consumos mensuales│ Trabajadores │ Huella mensual │  Gráficos   │  │
│  └──────────────────┴──────────────┴────────────────┴─────────────┘  │
│                                                                      │
│  ╔═══════════════════════════════════════════════════════════════╗   │
│  ║  (contenido del tab activo)                                   ║   │
│  ╚═══════════════════════════════════════════════════════════════╝   │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Tab · Consumos mensuales (estado normal)

```
┌──────────────────────────────────────────────────────────────────────┐
│  Ana García · Logística · Empresa Ejemplo S.L.                       │
│  Período: [ Enero ▾ ] [ 2025 ▾ ]                                     │
├──────────────────────────────────────────────────────────────────────┤
│  [ + Nuevo consumo ]                    [ Duplicar mes anterior ]    │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  Recurso       │ Unidad │ Cantidad │ Alcance   │ Emisión CO₂e   │ │
│  ├────────────────┼────────┼──────────┼───────────┼────────────────┤ │
│  │  Electricidad  │ kWh    │ 1.200    │ Alcance 2 │ 264,00         │ │
│  ├────────────────┼────────┼──────────┼───────────┼────────────────┤ │
│  │  Gas natural   │ m³     │ 80       │ Alcance 1 │ 163,20         │ │
│  ├────────────────┼────────┼──────────┼───────────┼────────────────┤ │
│  │  ...           │ ...    │ ...      │ ...       │ ...            │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                          Total departamento: 427,20  │
└──────────────────────────────────────────────────────────────────────┘
```

## Tab · Consumos mensuales (con panel lateral — nuevo/editar consumo)

```
┌──────────────────────────────────────┬───────────────────────────────┐
│  Ana García · Logística              │  Nuevo consumo         [Cerrar]│
│  Período: [Ene ▾] [2025 ▾]           │  ─────────────────────────    │
├──────────────────────────────────────┤  Recurso * [      ▾         ] │
│ [+Nuevo]          [Duplicar mes ant.]│  Unidad    kWh               │
│  ┌────────────────────────────────┐  │  Alcance   Alcance 2         │
│  │ Recurso│Unidad│Cant.│Alc.│Em.  │  │  Cantidad* [ ______________ ] │
│  ├────────┼──────┼─────┼────┼─────┤  │                              │
│  │ Eléctr.│ kWh  │1.200│ A2 │264,0│  │  [ Guardar ]  [ Cancelar ]   │
│  └────────────────────────────────┘  │  [ Eliminar ]                │
│  Total: 264,00                        │                              │
└──────────────────────────────────────┴───────────────────────────────┘
```

---

## Tab · Trabajadores

```
┌──────────────────────────────────────────────────────────────────────┐
│  Ana García · Logística · Empresa Ejemplo S.L.                       │
│  Período: [ Enero ▾ ] [ 2025 ▾ ]                                     │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  Nombre        │ Localidad  │ Días pres. │ Vehículo  │ Dist.(km)│ │
│  ├────────────────┼────────────┼────────────┼───────────┼──────────┤ │
│  │  Ana García    │ Madrid     │ 20         │ Coche     │ 12,4     │ │
│  ├────────────────┼────────────┼────────────┼───────────┼──────────┤ │
│  │  Pedro López   │ Alcorcón   │ 15         │ Metro     │ 8,1      │ │
│  ├────────────────┼────────────┼────────────┼───────────┼──────────┤ │
│  │  ...           │ ...        │ ...        │ ...       │ ...      │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│  Vista de solo lectura. Para modificar empleados, contacta con el    │
│  administrador.                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Tab · Huella mensual

```
┌──────────────────────────────────────────────────────────────────────┐
│  Ana García · Logística · Empresa Ejemplo S.L.                       │
│  Período: [ Enero ▾ ] [ 2025 ▾ ]                                     │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────┐ ┌───────────┐  │
│  │   Total CO₂e   │ │   Alcance 1    │ │ Alcance 2  │ │Alcance 3  │  │
│  │  427,20 kg     │ │  163,20 kg     │ │ 264,00 kg  │ │ (commuting│  │
│  └────────────────┘ └────────────────┘ └────────────┘ └───────────┘  │
│                                                                      │
│  Desglose por recurso                                                │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  Recurso       │ Unidad │ Cantidad │ Alcance   │ Emisión CO₂e   │ │
│  ├────────────────┼────────┼──────────┼───────────┼────────────────┤ │
│  │  Electricidad  │ kWh    │ 1.200    │ Alcance 2 │ 264,00         │ │
│  ├────────────────┼────────┼──────────┼───────────┼────────────────┤ │
│  │  Gas natural   │ m³     │ 80       │ Alcance 1 │ 163,20         │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  Commuting (Alcance 3)                                               │
│  Huella de commuting incluida en el total: 48,60 kg CO₂e            │
│                                                                      │
│  [ Exportar PDF ]  [ Exportar CSV ]                                  │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Tab · Gráficos (placeholder)

```
┌──────────────────────────────────────────────────────────────────────┐
│  Ana García · Logística · Empresa Ejemplo S.L.                       │
│  Período: [ Enero ▾ ] [ 2025 ▾ ]                                     │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│           Los gráficos estarán disponibles próximamente.             │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Elementos

| Elemento                | Tipo        | Notas                                                           |
|-------------------------|-------------|-----------------------------------------------------------------|
| Topbar                  | HBox        | Breadcrumb: Inicio › Sel. responsable › Nombre (3 niveles)     |
| Cabecera responsable    | VBox        | Nombre, Departamento · Empresa, selector mes/año               |
| Selector período        | HBox        | ComboBox mes + ComboBox año; compartido entre todos los tabs   |
| TabPane                 | TabPane     | Cuatro tabs: Consumos, Trabajadores, Huella, Gráficos          |
| **Tab Consumos**        |             |                                                                 |
| Botón nuevo consumo     | Button      | Abre panel lateral vacío                                       |
| Botón duplicar mes      | Button      | Copia consumos del mes anterior al mes actual                  |
| Tabla consumos          | TableView   | Columnas: Recurso, Unidad, Cantidad, Alcance, Emisión CO₂e     |
| Total departamento      | Label       | Suma de emisiones del período                                   |
| Panel lateral consumo   | VBox        | ComboBox factor, Labels unidad/alcance (solo lectura), cantidad|
| Botón Eliminar          | Button      | Solo visible en modo edición                                   |
| **Tab Trabajadores**    |             |                                                                 |
| Tabla empleados         | TableView   | Solo lectura. Columnas: Nombre, Localidad, Días, Vehículo, km  |
| **Tab Huella**          |             |                                                                 |
| KPI cards               | VBox×4      | Total CO₂e, Alcance 1, Alcance 2, Alcance 3                    |
| Tabla desglose          | TableView   | Misma lista que Tab Consumos (ObservableList compartida)       |
| Sección commuting       | Label       | Mensaje con kg CO₂e o aviso si Alcance 3 no está habilitado   |
| Botones exportar        | Button×2    | Exportar PDF / CSV (placeholder)                               |

## Comportamiento

- El selector de período se mantiene al cambiar de tab; recarga Consumos y Huella automáticamente.
- Clic en fila de consumo → abre panel lateral en modo edición con datos pre-rellenados.
- *Duplicar mes anterior* → copia consumos del mes previo; omite los que ya existen en el mes actual.
- Tab Trabajadores es de solo lectura; no permite edición.
- Tab Huella y Tab Consumos comparten la misma `ObservableList` — se sincronizan automáticamente.
- Tab Gráficos es un placeholder pendiente de implementación.
- Si Alcance 3 no está habilitado en el departamento, la sección commuting muestra un aviso.
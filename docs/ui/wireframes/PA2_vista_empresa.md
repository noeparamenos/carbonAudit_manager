# Wireframe · PA2 · Vista empresa

## Layout general

```
┌──────────────────────────────────────────────────────────────────────┐
│  CarbonAudit Manager     Empresas > Empresa Ejemplo S.L.  Admin      │  ← topbar
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Empresa Ejemplo S.L.                                                │
│  CIF: B12345678 · Madrid                    [ Editar ] [ Eliminar ]  │
│                                                                      │
│  Período:  [ Enero ▾ ]  [ 2025 ▾ ]                                   │
│                                                                      │
│  ┌──────────────┬──────────────┬──────────────┐                      │
│  │ Departamentos│   Consumos   │   Gráficos   │                      │
│  └──────────────┴──────────────┴──────────────┘                      │
│                                                                      │
│  ╔═══════════════════════════════════════════════════════════════╗   │
│  ║  (contenido del tab activo)                                   ║   │
│  ╚═══════════════════════════════════════════════════════════════╝   │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Tab · Departamentos (estado normal)

```
┌──────────────────────────────────────────────────────────────────────┐
│  Empresa Ejemplo S.L.   CIF: B12345678 · Madrid   [Editar][Eliminar] │
│  Período: [ Enero ▾ ] [ 2025 ▾ ]                                     │
├──────────────────┬───────────────────────────────────────────────────┤
│ Departamentos    │ Consumos │ Gráficos                               │
├──────────────────┴───────────────────────────────────────────────────┤
│                                                    [ + Nuevo dpto. ] │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  Nombre dpto.          │  Nº empleados  │  Responsable activo  │ │
│  ├────────────────────────┼────────────────┼──────────────────────┤ │
│  │  Logística             │  12            │  Ana García          │ │
│  ├────────────────────────┼────────────────┼──────────────────────┤ │
│  │  Administración        │  5             │  (sin responsable)   │ │
│  ├────────────────────────┼────────────────┼──────────────────────┤ │
│  │  ...                   │  ...           │  ...                 │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  Haz clic en un departamento para gestionarlo                        │
└──────────────────────────────────────────────────────────────────────┘
```

## Tab · Departamentos (con panel lateral — nuevo departamento)

```
┌──────────────────────────────────────┬───────────────────────────────┐
│  Empresa Ejemplo S.L.  [Ed.][El.]    │                               │
│  Período: [Ene ▾] [2025 ▾]           │   Nuevo departamento          │
├──────────────────────────────────────┤  ─────────────────────────    │
│ Departamentos │ Consumos │ Gráficos  │                               │
├──────────────────────────────────────┤  Nombre  [ ________________ ] │
│                    [+ Nuevo dpto.]   │                               │
│  ┌──────────────────────────────┐    │  [ Guardar ]  [ Cancelar ]    │
│  │  Nombre  │ Empleados │ Resp. │    │                               │
│  ├──────────┼───────────┼───────┤    │                               │
│  │ Logíst.  │ 12        │ Ana G.│    │                               │
│  └──────────────────────────────┘    │                               │
└──────────────────────────────────────┴───────────────────────────────┘
```

---

## Tab · Consumos

```
┌──────────────────────────────────────────────────────────────────────┐
│  Empresa Ejemplo S.L.   CIF: B12345678 · Madrid   [Editar][Eliminar] │
│  Período: [ Enero ▾ ] [ 2025 ▾ ]                                     │
├──────────────────┬───────────────────────────────────────────────────┤
│ Departamentos    │ Consumos │ Gráficos                               │
├──────────────────┴───────────────────────────────────────────────────┤
│                                                      [ Exportar CSV ] │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐   │
│  │ Departamento  │ Recurso   │ Cantidad │ Unidad │ Emisión CO₂e  │   │
│  ├───────────────┼───────────┼──────────┼────────┼───────────────┤   │
│  │ Logística     │ Gasoil    │ 340      │ L      │ 903,4 kg      │   │
│  ├───────────────┼───────────┼──────────┼────────┼───────────────┤   │
│  │ Logística     │ Eléctrica │ 1.200    │ kWh    │ 264,0 kg      │   │
│  ├───────────────┼───────────┼──────────┼────────┼───────────────┤   │
│  │ Administración│ Eléctrica │ 480      │ kWh    │ 105,6 kg      │   │
│  ├───────────────┼───────────┼──────────┼────────┼───────────────┤   │
│  │ ...           │ ...       │ ...      │ ...    │ ...           │   │
│  └───────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  Total empresa enero 2025:  1.273,0 kg CO₂e                         │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Tab · Gráficos

```
┌──────────────────────────────────────────────────────────────────────┐
│  Empresa Ejemplo S.L.   CIF: B12345678 · Madrid   [Editar][Eliminar] │
│  Período: [ Enero ▾ ] [ 2025 ▾ ]                                     │
├──────────────────┬───────────────────────────────────────────────────┤
│ Departamentos    │ Consumos │ Gráficos                               │
├──────────────────┴───────────────────────────────────────────────────┤
│                                                                      │
│  Evolución mensual — últimos 12 meses                                │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  kg CO₂e                                                        │ │
│  │  2000 │         ╭──╮                                            │ │
│  │  1500 │    ╭────╯  ╰──╮                                         │ │
│  │  1000 │╭───╯          ╰────╮    ╭──╮                            │ │
│  │   500 │╯                   ╰────╯  ╰──────                      │ │
│  │     0 └──────────────────────────────────────── mes             │ │
│  │        Feb Mar Abr May Jun Jul Ago Sep Oct Nov Dic Ene          │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  Distribución por alcance (mes actual)      Comparativa dptos.       │
│  ┌───────────────────────┐                 ┌───────────────────────┐ │
│  │     Alcance 1 55%     │                 │ Logística     903 kg  │ │
│  │     Alcance 2 35%     │                 │ Administración 106 kg │ │
│  │     Alcance 3 10%     │                 │ ...                   │ │
│  └───────────────────────┘                 └───────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Elementos

| Elemento            | Tipo            | Notas                                                    |
|---------------------|-----------------|----------------------------------------------------------|
| Topbar              | HBox            | Breadcrumb izquierda (Empresas › nombre), rol derecha   |
| Cabecera empresa    | HBox            | Nombre grande, CIF · ciudad, botones Editar / Eliminar  |
| Selector período    | HBox            | ComboBox mes + ComboBox año; persiste al cambiar de tab |
| TabPane             | TabPane         | Tres tabs: Departamentos, Consumos, Gráficos            |
| **Tab Departamentos** | | |
| Botón nuevo dpto.   | Button          | Abre panel lateral                                       |
| Tabla departamentos | TableView       | Columnas: Nombre, Nº empleados, Responsable activo      |
| Texto de ayuda      | Label           | "Haz clic en un departamento para gestionarlo"          |
| Panel lateral       | VBox            | Formulario: solo campo Nombre                            |
| **Tab Consumos** | | |
| Botón Exportar CSV  | Button          | Genera CSV de la tabla visible                           |
| Tabla consumos      | TableView       | Columnas: Departamento, Recurso, Cantidad, Unidad, CO₂e |
| Total empresa       | Label           | Suma de CO₂e del mes seleccionado                        |
| **Tab Gráficos** | | |
| Gráfico evolución   | LineChart       | 12 meses, eje Y en kg CO₂e                              |
| Gráfico tarta       | PieChart        | Distribución por alcance del mes actual                  |
| Gráfico barras      | BarChart        | Comparativa entre departamentos del mes actual           |

## Comportamiento

- El selector de período (mes/año) se mantiene al cambiar de tab.
- Clic en fila de departamentos → navega a PA3.
- Clic en *Nuevo departamento* → panel lateral desde la derecha (solo campo Nombre).
- *Guardar* departamento → valida que el nombre no esté vacío, inserta, refresca tabla, cierra panel.
- *Editar empresa* → panel lateral con los mismos campos del formulario de PA1 rellenos.
- *Eliminar empresa* → modal de confirmación; bloqueado si la empresa tiene departamentos.
- Tab Consumos muestra datos del período seleccionado; si no hay consumos, tabla vacía con mensaje.
- Tab Gráficos no tiene acciones; los gráficos se recalculan al cambiar el período.

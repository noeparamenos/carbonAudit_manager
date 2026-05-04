# Wireframe · PA1 · Lista de empresas

## Estado normal (sin panel lateral)

```
┌─────────────────────────────────────────────────────────────────┐
│  CarbonAudit Manager                              Administrador │  ← topbar
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Empresas                                   [ + Nueva empresa ] │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Nombre                  │  CIF          │  Ciudad        │  │
│  ├──────────────────────────┼───────────────┼────────────────┤  │
│  │  Empresa Ejemplo S.L.    │  B12345678    │  Madrid        │  │
│  ├──────────────────────────┼───────────────┼────────────────┤  │
│  │  Otra Empresa S.A.       │  A87654321    │  Barcelona     │  │
│  ├──────────────────────────┼───────────────┼────────────────┤  │
│  │  ...                     │  ...          │  ...           │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                 │
│  Haz clic en una empresa para ver sus departamentos             │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Estado con panel lateral (nueva empresa)

```
┌──────────────────────────────────┬──────────────────────────────┐
│  CarbonAudit Manager  Admin      │                              │
├──────────────────────────────────┤   Nueva empresa              │
│                                  │  ─────────────────────────   │
│  Empresas        [+ Nueva empr.] │                              │
│                                  │  Nombre   [ _____________ ]  │
│  ┌────────────────────────────┐  │  CIF      [ _____________ ]  │
│  │  Nombre   │ CIF  │ Ciudad  │  │  Ciudad   [ _____________ ]  │
│  ├───────────┼──────┼─────────┤  │  Calle    [ _____________ ]  │
│  │ Empresa.. │ B12. │ Madrid  │  │  Número   [ ___ ]            │
│  ├───────────┼──────┼─────────┤  │  C.Postal [ _____________ ]  │
│  │ Otra Emp. │ A87. │ Barcel. │  │  Provincia[ _____________ ]  │
│  └────────────────────────────┘  │                              │
│                                  │  [ Guardar ]  [ Cancelar ]   │
│                                  │                              │
└──────────────────────────────────┴──────────────────────────────┘
```

## Elementos

| Elemento         | Tipo            | Notas                                          |
|------------------|-----------------|------------------------------------------------|
| Topbar           | HBox            | Nombre app a la izquierda, rol a la derecha    |
| Título sección   | Label           | "Empresas"                                     |
| Botón nueva      | Button          | Abre el panel lateral                          |
| Tabla empresas   | TableView       | Columnas: Nombre, CIF, Ciudad. Clic en fila → PA2 |
| Texto de ayuda   | Label           | Solo visible cuando la tabla tiene datos       |
| Panel lateral    | VBox            | Desliza desde la derecha al pulsar "Nueva"     |
| Formulario       | GridPane        | Campos: Nombre, CIF, Ciudad, Calle, Número, CP, Provincia |
| Botón Guardar    | Button          | Valida y persiste; cierra el panel             |
| Botón Cancelar   | Button          | Cierra el panel sin guardar                    |

## Comportamiento

- Clic en fila de la tabla → navega a PA2 (vista empresa).
- Clic en *Nueva empresa* → panel lateral se desliza desde la derecha.
- Clic en *Cancelar* o fuera del panel → panel se cierra.
- Clic en *Guardar* → valida campos, inserta empresa, refresca tabla, cierra panel.
- Si no hay empresas, la tabla muestra un mensaje "No hay empresas registradas".

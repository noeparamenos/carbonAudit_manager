# Wireframe · PR0 · Selección de responsable activo

## Layout general

```
┌──────────────────────────────────────────────────────────────────────┐
│  CarbonAudit Manager     Inicio › Selección de responsable           │  ← topbar
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ¿Quién eres?                                                        │
│  Selecciona tu nombre de la lista de responsables activos.           │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │  Nombre              │  Departamento       │  Empresa           │ │
│  ├──────────────────────┼─────────────────────┼────────────────────┤ │
│  │  Ana García          │  Logística          │  Empresa Ejemplo   │ │
│  ├──────────────────────┼─────────────────────┼────────────────────┤ │
│  │  Pedro Martínez      │  Administración     │  Empresa Ejemplo   │ │
│  ├──────────────────────┼─────────────────────┼────────────────────┤ │
│  │  ...                 │  ...                │  ...               │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                      │
│  Haz clic en tu nombre para acceder a tu panel de responsable.       │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Elementos

| Elemento            | Tipo       | Notas                                                         |
|---------------------|------------|---------------------------------------------------------------|
| Topbar              | HBox       | Breadcrumb: Inicio (clicable) › Selección de responsable     |
| Título              | Label      | "¿Quién eres?"                                               |
| Subtítulo           | Label      | Instrucción de uso                                           |
| Tabla responsables  | TableView  | Columnas: Nombre, Departamento, Empresa                      |
| Texto de ayuda      | Label      | "Haz clic en tu nombre para acceder..."                      |

## Comportamiento

- Solo se muestran responsables con mandato activo (`fecha_fin IS NULL`).
- Clic en una fila → navega a PR1 pasando el objeto `Responsable` seleccionado.
- Si no hay responsables activos → tabla vacía con mensaje de contacto con el administrador.
- "Inicio" en el breadcrumb → vuelve a P0.
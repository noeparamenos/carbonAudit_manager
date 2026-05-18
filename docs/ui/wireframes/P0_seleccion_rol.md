# Wireframe · P0 · Selección de rol

## Layout

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                                                             │
│                      [ LOGO ]                               │
│                  CarbonAudit Manager                        │
│                                                             │
│              Selecciona tu perfil de acceso                 │
│                                                             │
│                                                             │
│     ┌───────────────────┐     ┌───────────────────┐         │
│     │                   │     │                   │         │
│     │     [ icono ]     │     │     [ icono ]     │         │
│     │                   │     │                   │         │
│     │  Administrador    │     │   Responsable     │         │
│     │                   │     │                   │         │
│     │ Gestión completa  │     │ Gestión de tu     │         │
│     │ de la empresa     │     │ departamento      │         │
│     │                   │     │                   │         │
│     └───────────────────┘     └───────────────────┘         │
│                                                             │
│                                                             │
│                                                    v1.0.0   │
└─────────────────────────────────────────────────────────────┘
```

## Elementos

| Elemento         | Tipo              | Notas                                 |
|------------------|-------------------|---------------------------------------|
| Logo             | ImageView         | Centrado, parte superior              |
| Título           | Label             | Nombre de la app                      |
| Subtítulo        | Label             | Texto de invitación                   |
| Tarjeta Admin    | Button / VBox     | Icono + título + descripción breve    |
| Tarjeta Resp.    | Button / VBox     | Icono + título + descripción breve    |
| Versión          | Label             | Esquina inferior derecha, texto pequeño |

## Comportamiento

- Hover sobre tarjeta: sombra o borde resaltado.
- Clic en *Administrador* → navega a PA0.
- Clic en *Responsable* → navega a PR0.
- No hay botón de volver — es el punto de entrada de la app.

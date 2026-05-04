# Mapa de Navegación

## Árbol de pantallas

```
P0 · Selección de rol
  ├── [Administrador]
  │     PA1 · Lista de empresas
  │       └── [clic en empresa]
  │             PA2 · Vista empresa
  │                   ├── Tab: Departamentos
  │                   │     └── [clic en departamento]
  │                   │           PA3 · Gestión del departamento
  │                   │                 ├── Empleados (añadir / editar)
  │                   │                 └── Asignar responsable
  │                   ├── Tab: Consumos  (tabla por dpto., filtro mes/año)
  │                   └── Tab: Gráficos  (evolución histórica, tarta por alcance)
  │
  └── [Responsable]
        PR0 · Selección de responsable activo
          └── [responsable seleccionado]
                PR1 · Vista responsable
                      ├── Tab: Consumos mensuales
                      ├── Tab: Trabajadores
                      ├── Tab: Huella mensual
                      └── Tab: Gráficos
```

## Diagrama de flujo

Ver [`../diagrams/navegacion.puml`](../diagrams/navegacion.puml).

## Nota sobre autenticación

La aplicación no dispone de sistema de login. La selección de rol en P0 y de responsable
en PR0 actúan como mecanismo de contexto:

- El **Administrador** accede sin restricciones adicionales.
- El **Responsable** se identifica eligiendo su nombre de la lista de mandatos activos.
  La app usa esa selección para filtrar todos los datos (departamento, consumos, empleados)
  al departamento correspondiente.

## Visibilidad por rol

| Pantalla                        | Administrador | Responsable |
|---------------------------------|:-------------:|:-----------:|
| P0 · Selección de rol           | ✅            | ✅          |
| PA1 · Lista de empresas         | ✅            | ❌          |
| PA2 · Tab Departamentos         | ✅            | ❌          |
| PA2 · Tab Consumos (empresa)    | ✅            | ❌          |
| PA2 · Tab Gráficos (empresa)    | ✅            | ❌          |
| PA3 · Gestión departamento      | ✅            | ❌          |
| PR0 · Selección responsable     | ❌            | ✅          |
| PR1 · Tab Consumos mensuales    | ❌            | ✅          |
| PR1 · Tab Trabajadores          | ❌            | ✅          |
| PR1 · Tab Huella mensual        | ❌            | ✅          |
| PR1 · Tab Gráficos              | ❌            | ✅          |

## Reglas generales

- Los **formularios de alta/edición** de empleados y responsables se abren como panel lateral
  dentro de la misma vista, no como modales bloqueantes. La única excepción es la confirmación
  de borrado, que sí usa modal.
- El selector de **mes/año** se mantiene al cambiar de tab, tanto en PA2 como en PR1.

# Cálculo de Huella de Carbono

## Descripción

El sistema calcula la huella de carbono de un departamento para un período específico (mes/año), sumando:
- **Scope 1 y 2:** Consumos directos de energía, agua, combustible, etc.
- **Scope 3:** Emisiones por desplazamiento (commuting) de empleados

## Fórmulas Implementadas

### Scope 1 y 2 (Consumos)
```
Emisión = Cantidad × Factor_Emisión
```

**Ejemplo:**
- Consumo: 500 kWh de electricidad
- Factor: 0.275 kgCO2e/kWh
- Emisión: 500 × 0.275 = 137.5 kgCO2e

### Scope 3 (Commuting)
```
Emisión = Distancia_diaria × 2 × Días_presenciales × Factor_medio_transporte
```

**Ejemplo:**
- Distancia diaria: 25 km
- Días presenciales en mes: 20
- Factor (coche gasolina): 0.137 kgCO2e/km
- Emisión: 25 × 2 (ida+vuelta) × 20 × 0.137 = 137 kgCO2e

## Métodos Disponibles en la capa servicio

### Geolocalización

| Método | Descripción |
|--------|-------------|
| `asignarDistanciaTrabajo` | Llama a ORS para obtener coordenadas y calcular la distancia real por carretera entre el domicilio del empleado y su departamento. |

### Commuting individual (Scope 3)

| Método | Descripción |
|--------|-------------|
| `getCommutingMensual` | Calcula las emisiones de commuting de un empleado para un mes usando su distancia y días presenciales. Para previsualización antes de guardar en BD. |

### Cálculos por Departamento

| Método | Descripción |
|--------|-------------|
| `getHuellaTotalDepartamentoMes` | Huella total del departamento en un mes. Suma Scope 1+2 (consumos) y Scope 3 (commuting) si `incluirAlcance3 = true`. |
| `getHuellaPorScope` | Misma lógica que el anterior pero desglosada por scope `{1: x, 2: y, 3: z}`. Para gráficos de tarta en el dashboard. |
| `getHuellaAnualDepartamento` | Suma los 12 meses del año para un departamento. Para la vista histórica anual. |
| `getVariacionMensual` | Diferencia de emisiones entre el mes actual y el anterior (positivo = más emisiones, negativo = mejora). Gestiona el cambio de año en enero. |

### Cálculos por Empresa

| Método | Descripción |
|--------|-------------|
| `getHuellaTotalEmpresaMes` | Suma la huella de todos los departamentos de la empresa en un mes. Para el dashboard principal. |
| `getHuellaAnualEmpresa` | Huella total anual de la empresa sumando todos sus departamentos. Para el informe de auditoría final. |


## Próximos métodos a implementar

- [ ] Cálculos históricos (período personalizado)
- [ ] Dashboard con gráficos de emisiones

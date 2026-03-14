

# Diccionario de datos

## 1. Empresa

Entidad principal que representa a la organización usuaria del sistema.

| Campo         | Tipo de Dato | Restricciones   | Descripción                                    |
| ------------- | ------------ | --------------- | ---------------------------------------------- |
| id_empresa    | SERIAL       | PK              | Identificador único autoincremental            |
| nombre_social | VARCHAR(100) | NOT NULL        | Nombre legal de la PYME                        |
| cif           | VARCHAR(15)  | UNIQUE NOT NULL | Código de Identificación Fiscal                |
| direccion     | VARCHAR(255) |                 | Dirección de la sede principal                 |
| telefono      | VARCHAR(20)  |                 | Teléfono de contacto                           |
| email         | VARCHAR(100) |                 | Email corporativo para notificaciones          |
| sector        | VARCHAR(50)  |                 | Sector económico (industrial, servicios, etc.) |

---

## 2. Departamento

Permite la **segmentación del impacto y consumo energético** por áreas de la empresa.

| Campo              | Tipo de Dato | Restricciones            | Descripción                                                          |
| ------------------ | ------------ | ------------------------ | -------------------------------------------------------------------- |
| id_departamento    | SERIAL       | PK                       | Identificador único                                                  |
| id_empresa         | INT          | FK → Empresa(id_empresa) | Empresa a la que pertenece                                           |
| nombre             | VARCHAR(100) | NOT NULL                 | Ej: Ventas, RRHH, Producción                                         |
| descripcion        | TEXT         |                          | Breve explicación de la actividad                                    |
| direccion_completa | VARCHAR(255) |                          | Dirección física usada para cálculos de movilidad                    |
| incluir_alcance3   | BOOLEAN      | DEFAULT TRUE             | Permite incluir o excluir emisiones de **Alcance 3** en los cálculos |

---

## 3. Responsables de Departamento (Trazabilidad y Auditoría)

- Permite mantener un **historial de responsables de sostenibilidad**
- Util para auditorías y cumplimiento normativo (por ejemplo **CSRD europea**).

| Campo         | Tipo de Dato | Restricciones                      | Descripción                  |
| ------------- | ------------ | ---------------------------------- | ---------------------------- |
| id_asignacion | SERIAL       | PK                                 | Identificador único          |
| id_dept       | INT          | FK → Departamento(id_departamento) | Departamento gestionado      |
| id_empleado   | INT          | FK → Empleados(id_empleado)        | Empleado responsable         |
| fecha_inicio  | DATE         | NOT NULL                           | Inicio de la responsabilidad |
| fecha_fin     | DATE         |                                    | Fin de la responsabilidad    |
| activo        | BOOLEAN      | DEFAULT TRUE                       | Indica el responsable actual |

---

## 4. Empleados

- Representa a los empleados de la empresa. 
- Sus datos se utilizan principalmente para cálculos de movilidad y asignación de responsabilidades dentro del sistema.

| Campo                | Tipo         | Restricciones                      | Utilidad                                   |
| -------------------- | ------------ | ---------------------------------- | ------------------------------------------ |
| id_empleado          | SERIAL       | PK                                 | Identificador único                        |
| id_dept              | INT          | FK → Departamento(id_departamento) | Departamento actual                        |
| nombre               | VARCHAR(100) | NOT NULL                           | Nombre del trabajador                      |
| direccion_residencia | VARCHAR(255) |                                    | Dirección para cálculo de desplazamientos  |
| distancia_trabajo_km | DECIMAL(6,2) |                                    | Distancia aproximada residencia-trabajo(km)|

---

## 5. Factores de Emisión

Tabla maestra que contiene los **coeficientes oficiales de emisión de CO₂ equivalente**.

| Campo        | Tipo          | Restricciones              | Descripción                           |
| ------------ | ------------- | -------------------------- | ------------------------------------- |
| id_factor    | SERIAL        | PK                         | Identificador del factor              |
| nombre       | VARCHAR(100)  | NOT NULL                   | Ej: Electricidad Red, Gas Natural     |
| unidad       | VARCHAR(20)   |                            | Unidad de medida (kWh, litros, m3)    |
| valor_factor | DECIMAL(10,5) | NOT NULL                   | Factor de emisión (kgCO2e por unidad) |
| alcance      | INT           | CHECK (alcance IN (1,2,3)) | Alcance del **GHG Protocol**          |

---

## 6. Consumos Mensuales

Registro centralizado del **consumo energético por departamento**.

Estos datos alimentan el **dashboard de emisiones**.

| Campo      | Tipo de Dato  | Restricciones                      | Función                            |
| ---------- | ------------- | ---------------------------------- | ---------------------------------- |
| id_consumo | SERIAL        | PK                                 | Identificador único                |
| id_dept    | INT           | FK → Departamento(id_departamento) | Departamento que genera el consumo |
| id_factor  | INT           | FK → FactoresDeEmision(id_factor)  | Tipo de energía o combustible      |
| cantidad   | DECIMAL(12,2) | NOT NULL                           | Cantidad consumida                 |
| mes        | INT           | CHECK (mes BETWEEN 1 AND 12)       | Mes del consumo                    |
| anio       | INT           |                                    | Año del registro                   |

---

## 7. Commuting Empleados

- Tabla que registra los datos de desplazamiento diario de los empleados 
- Util para calcular las emisiones asociadas al commuting (desplazamiento al trabajo).

| Campo                 | Tipo         | Restricciones               | Descripción                 |
| --------------------- | ------------ | --------------------------- | --------------------------- |
| id_transporte         | SERIAL       | PK                          | Identificador               |
| id_empleado           | INT          | FK → Empleados(id_empleado) | Empleado asociado           |
| id_factor             | INT          | FK → Factores(id_factor)    | Medio de transporte         |
| distancia_diaria_km   | DECIMAL(6,2) |                             | Distancia total diaria      |
| dias_presenciales_mes | INT          |                             | Días presenciales en el mes |

---

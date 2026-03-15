# Diccionario de datos


## 0. Dirección

- Tabla que almacena las **ubicaciones físicas** utilizadas en el sistema.
- Permite obtener coordenadas geográficas mediante una API de geocoding para cálculos de movilidad y análisis de huella de carbono.

| Campo         | Tipo de Dato  | Restricciones | Descripción                                        |
|---------------|---------------|---------------|----------------------------------------------------|
| id_direccion  | SERIAL        | PK            | Identificador único de la ubicación                |
| calle         | VARCHAR(200)  | NOT NULL      | Nombre de la vía y número                          |
| ciudad        | VARCHAR(100)  | NOT NULL      | Municipio o ciudad                                 |
| codigo_postal | VARCHAR(10)   | NOT NULL      | Código postal para mejorar precisión del geocoding |
| provincia     | VARCHAR(100)  |               | Estado o provincia                                 |
| latitud       | DECIMAL(10,8) |               | Coordenada Y (calculada automáticamente vía API)   |
| longitud      | DECIMAL(11,8) |               | Coordenada X (calculada automáticamente vía API)   |

## 1. Empresa

- Entidad principal que representa a la organización usuaria del sistema.

| Campo         | Tipo de Dato | Restricciones                 | Descripción                                    |
|---------------|--------------|-------------------------------|------------------------------------------------|
| id_empresa    | SERIAL       | PK                            | Identificador único autoincremental            |
| nombre_social | VARCHAR(100) | UNIQUE, NOT NULL              | Nombre legal de la PYME                        |
| cif           | VARCHAR(15)  | UNIQUE, NOT NULL              | Código de Identificación Fiscal                |
| telefono      | VARCHAR(20)  |                               | Teléfono de contacto                           |
| email         | VARCHAR(100) |                               | Email corporativo para notificaciones          |
| sector        | VARCHAR(50)  |                               | Sector económico (industrial, servicios, etc.) |
| id_direccion  | INT          | FK -> Direccion(id_direccion) | Dirección de la sede principal                 | 

---

## 2. Departamento

- Permite la **segmentación del impacto y consumo energético** por áreas de la empresa.
- Permite excluir las emisiones de alcance 3 (que dependen del tranporte de los empleados del departamento)
- Evitar repeticiones: UNIQUE (id_empresa, nombre)

| Campo            | Tipo de Dato | Restricciones                          | Descripción                                            |
|------------------|--------------|----------------------------------------|--------------------------------------------------------|
| id_departamento  | SERIAL       | PK                                     | Identificador único                                    |
| nombre           | VARCHAR(100) | UNIQUE NOT NULL                        | Ej: Ventas, RRHH, Producción                           |
| descripcion      | TEXT         |                                        | Breve explicación de la actividad                      |
| incluir_alcance3 | BOOLEAN      | DEFAULT TRUE                           | Permite excluir emisiones de Alcance 3 en los cálculos |
| id_direccion     | INT          | FK -> Direccion(id_direccion) NOT NULL | Dirección del departamento                             |
| id_empresa       | INT          | FK -> Empresa(id_empresa)  NOT NULL    | Empresa a la que pertenece                             |



---
## 3. Empleado

- Representa a los empleados de la empresa.
- Sus datos se utilizan principalmente para cálculos de movilidad y asignación de responsabilidades dentro del sistema.

| Campo             | Tipo         | Restricciones                                   | Utilidad                                 |
|-------------------|--------------|-------------------------------------------------|------------------------------------------|
| id_empleado       | SERIAL       | PK                                              | Identificador único                      |
| nombre            | VARCHAR(100) | NOT NULL                                        | Nombre del trabajador                    |
| distancia_trabajo | DECIMAL(6,2) |                                                 | Distancia al trabajo (calculada via API) |
| medio_transporte  | INT          | FK -> Factores de emisison (id_factor) NOT NULL | Medio de desplazamiento a la oficina     |
| dias_presenciales | INT          | NOT NULL DEFAULT (20)                           | Total de dias que se desplaza al mes     |
| id_direccion      | INT          | FK -> Direccion(id_direccion)  NOT NULL         | Dirección de residencia                  |
| id_dept           | INT          | FK -> Departamento(id_departamento)             | Departamento actual                      |

---

## 4. Responsable

- Permite mantener un **historial de responsables de sostenibilidad**
- Util para auditorías y cumplimiento normativo (ej: CSRD europea).
- Para que solo haya un responsable por departamento: UNIQUE (id_dept, fecha_fin IS NULL)

| Campo           | Tipo de Dato  | Restricciones                                | Descripción                                  |
|-----------------|---------------|----------------------------------------------|----------------------------------------------|
| id_asignacion   | SERIAL        | PK                                           | Identificador del periodo de responsabilidad |
| fecha_inicio    | DATE          | NOT NULL                                     | Inicio de la responsabilidad                 |
| fecha_fin       | DATE          |                                              | Fin de la responsabilidad                    |
| id_dept         | INT           | FK → Departamento(id_departamento)  NOT NULL | Departamento gestionado                      |
| id_empleado     | INT           | FK → Empleados(id_empleado) NOT NULL         | Empleado responsable                         |

---

## 5. Factores de Emisión

- Contiene los **coeficientes oficiales de emisión de CO₂ equivalente**.
- Evitar registros repetidos: UNIQUE(id_dept, id_factor, mes, anio)

| Campo         | Tipo           | Restricciones                       | Descripción                               |
|---------------|----------------|-------------------------------------|-------------------------------------------|
| id_factor     | SERIAL         | PK                                  | Identificador del factor                  |
| nombre        | VARCHAR(100)   | NOT NULL                            | Ej: Electricidad Red, Gas, coche gasolida |
| unidad        | VARCHAR(20)    | NOT NULL                            | Unidad de medida (kWh, litros, m3, km)    |
| valor_factor  | DECIMAL(10,5)  | NOT NULL                            | Factor de emisión (kgCO2e por unidad)     |
| alcance       | INT            | CHECK (alcance IN (1,2,3)) NOT NULL | Alcance del **GHG Protocol**              |

---

## 6. Consumo Mensual

- Registro centralizado del **consumo energético por departamento**.
- Alimentan el **dashboard de emisiones** para visualizar los datos y filtarlos (por año, departamento...).
- Será la tabla más consultada (necesidad de índices)
- Evitar registros repetidos: UNIQUE(id_dept, id_factor, mes, anio)

| Campo      | Tipo de Dato  | Restricciones                               | Función                            |
|------------|---------------|---------------------------------------------|------------------------------------|
| id_consumo | SERIAL        | PK                                          | Identificador único                |
| cantidad   | DECIMAL(12,2) | NOT NULL                                    | Cantidad consumida                 |
| mes        | INT           | CHECK (mes BETWEEN 1 AND 12)     NOT NULL   | Mes del consumo                    |
| anio       | INT           | CHECK (anio >= 1950)             NOT NULL   | Año del registro                   |
| id_dept    | INT           | FK → Departamento(id_departamento) NOT NULL | Departamento que genera el consumo |
| id_factor  | INT           | FK → FactoresDeEmision(id_factor) NOT NULL  | Tipo de energía o combustible      |

---

## 7. Commuting Empleado

- Tabla que registra los datos de desplazamiento mensual de los empleados
- Util para calcular las emisiones asociadas al desplazamiento al trabajo (alcance 3).
- Permite mantener un historico de los medios de desplazamiento, evolución del teletrabajo...
- Garantizar un unico registro empleado/mes: PRIMARY KEY (id_empleado, mes, anio)

| Campo                 | Tipo         | Restricciones                          | Descripción                           |
|-----------------------|--------------|----------------------------------------|---------------------------------------|
| id_empleado           | INT          | FK → Empleados(id_empleado)  NOT NULL  | Empleado asociado                     |
| id_factor             | INT          | FK → Factores(id_factor) NOT NULL      | Medio de transporte                   |
| distancia_diaria_km   | DECIMAL(6,2) | NOT NULL                               | Distancia total diaria (ida y vuelta) |
| dias_presenciales_mes | INT          | NOT NULL                               | Días presenciales en el mes           |
| mes                   | INT          | CHECK (mes BETWEEN 1 AND 12)  NOT NULL | Mes trabajado                         |
| anio                  | INT          | CHECK (anio >= 1950)          NOT NULL | Año del mes trabajado                 |

---


erDiagram
EMPRESA ||--o{ DEPARTAMENTO : "tiene"
DEPARTAMENTO ||--o{ EMPLEADOS : "contiene"
DEPARTAMENTO ||--o{ CONSUMOS_MENSUALES : "registra"
DEPARTAMENTO ||--o{ RESPONSABLES : "gestiona"
EMPLEADOS ||--o| COMMUTING_EMPLEADOS : "realiza"
EMPLEADOS ||--o{ RESPONSABLES : "es asignado"
FACTORES_EMISION ||--o{ CONSUMOS_MENSUALES : "aplica a"
FACTORES_EMISION ||--o{ COMMUTING_EMPLEADOS : "aplica a"

    EMPRESA {
        int id_empresa PK
        string nombre_social NOT_NULL
        string cif UK_NOT_NULL
        string direccion
        string telefono
        string email
        string sector
    }

    DEPARTAMENTO {
        int id_departamento PK
        int id_empresa FK
        string nombre NOT_NULL
        string descripcion
        string direccion_completa
        boolean incluir_alcance3
    }

    EMPLEADOS {
        int id_empleado PK
        int id_dept FK
        string nombre NOT_NULL
        string direccion_residencia
        decimal distancia_trabajo_km
    }

    FACTORES_EMISION {
        int id_factor PK
        string nombre NOT_NULL
        string unidad NOT_NULL
        decimal valor_factor NOT_NULL
        int alcance NOT_NULL
    }

    CONSUMOS_MENSUALES {
        int id_consumo PK
        int id_dept FK
        int id_factor FK
        decimal cantidad NOT_NULL
        int mes NOT_NULL
        int anio NOT_NULL
    }

    COMMUTING_EMPLEADOS {
        int id_transporte PK
        int id_empleado FK
        int id_factor FK
        decimal distancia_diaria_km NOT_NULL
        int dias_presenciales_mes NOT_NULL
    }

    RESPONSABLES {
        int id_asignacion PK
        int id_dept FK
        int id_empleado FK
        date fecha_inicio NOT_NULL
        date fecha_fin
        boolean activo
    }
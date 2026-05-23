# CarbonAudit Manager

Proyecto de fin de curso — Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Multiplataforma (DAM).

Aplicación de escritorio para calcular y auditar la huella de carbono en PYMEs, siguiendo el estándar internacional **GHG Protocol** (Scope 1, 2 y 3).

---

## ¿Qué es?

CarbonAudit Manager centraliza el cálculo de emisiones de CO₂ de una empresa, permitiendo:

- Registrar consumos mensuales de energía, combustible y otros recursos por departamento
- Calcular las emisiones de commuting de empleados con integración geográfica vía API
- Gestionar factores de emisión configurables por alcance (Scope 1 / 2 / 3)
- Consultar la huella total desglosada por departamento, alcance y período
- Exportar informes en CSV y PDF

Diseñada para PYMEs que necesitan cumplir con exigencias normativas y de mercado sin depender de software empresarial complejo.

---

## Requisitos

| Componente | Versión mínima |
|------------|---------------|
| Java (JDK) | 21 |
| PostgreSQL | 14 |
| Maven | 3.8 |
| API Key OpenRouteService | gratuita — [openrouteservice.org](https://openrouteservice.org) |

---

## Instalación

```bash
# 1. Clonar el repositorio
git clone <repo-url>
cd carbon_audit_manager

# 2. Crear base de datos y usuario
psql -U postgres -f database/create_user_and_db.sql

# 3. Crear tablas
psql -U carbon_audit -d carbon_audit -f database/create_db_tables.sql

# 4. Cargar factores de emisión iniciales
psql -U carbon_audit -d carbon_audit -f database/seed_factores_emision.sql

# 5. Configurar API key de OpenRouteService
echo "ORS_API_KEY=tu_clave_aqui" > carbon_audit_manager_app/.env

# 6. Compilar y ejecutar
cd carbon_audit_manager_app
mvn clean package
mvn javafx:run
```

---

## Arquitectura

Patrón **DAO de tres capas** sobre PostgreSQL con JDBC manual (sin ORM):

| Capa | Paquete | Responsabilidad |
|------|---------|----------------|
| Model | `com.carbonaudit.model` | POJOs puros con relaciones por composición |
| DAO | `com.carbonaudit.dao` | Persistencia JDBC — `DatabaseManager` singleton con pool HikariCP |
| Service | `com.carbonaudit.service` | Lógica de cálculo de emisiones e integración con ORS |
| Controller | `com.carbonaudit.controller` | Controladores JavaFX — gestión de UI y navegación entre pantallas |

Más detalles en `/docs/arquitecture/`.

---

## Estructura del Proyecto

```
carbon_audit_manager/
├── carbon_audit_manager_app/
│   ├── src/main/java/com/carbonaudit/
│   │   ├── controller/     Controladores JavaFX (6 pantallas)
│   │   ├── dao/            Capa de persistencia (JDBC)
│   │   ├── model/          Entidades (POJOs)
│   │   ├── service/        Lógica de cálculo y geolocalización
│   │   ├── util/           Validaciones y clases de soporte
│   │   └── test/           Clases de prueba
│   └── pom.xml
├── database/               Scripts SQL de inicialización y seed
└── docs/                   Arquitectura, esquema BD, diagramas y manual
```

---

## Estado del Proyecto

🟢 **Funcional**

- [x] Modelo de dominio y DAOs
- [x] Cálculo de huella de carbono (Scopes 1, 2 y 3)
- [x] Integración con OpenRouteService (geolocalización y distancias)
- [x] Persistencia en PostgreSQL
- [x] Interfaz gráfica JavaFX (6 pantallas, 2 roles de usuario)
- [x] Exportación CSV y PDF
- [x] Documentación técnica (manual de código, diagramas UML, diccionario de datos)
- [ ] Tests unitarios con JUnit *(previsto en próxima iteración)*
- [ ] Gráficos de evolución histórica *(placeholders implementados)*
- [ ] Empaquetado para distribución

---

## Resolución de Problemas

**Error de conexión a PostgreSQL**
- Verifica que el servicio esté activo: `psql -U postgres`
- Comprueba usuario y contraseña en `database/create_user_and_db.sql`

**OpenRouteService devuelve error**
- Valida que `ORS_API_KEY` en `.env` sea correcta en [openrouteservice.org](https://openrouteservice.org)
- Comprueba que la dirección del empleado tenga coordenadas geocodificadas

---

## Autor

Noé Tostón Carballo

# CarbonAudit Manager

Proyecto de fin de curso DAM.  
Aplicación de escritorio para calcular y auditar la huella de carbono en PYMEs, siguiendo los estándares Scope 1, 2 y 3.

## ¿Qué es?

CarbonAudit Manager centraliza el cálculo de emisiones de carbono permitiendo:
- Registrar consumos de energía, combustible y otros recursos
- Calcular emisiones de commuting de empleados con integración geográfica
- Gestionar factores de emisión por sector
- Generar reportes de huella por departamento y período

Ideal para PYMEs que necesitan reportar su huella de carbono sin complejidad de software empresarial.

## Requisitos

- Java 21+
- PostgreSQL 12+
- Maven 3.8+
- API Key de OpenRouteService (gratuita en https://openrouteservice.org)

## Instalación

```bash
# 1. Clonar el repositorio
git clone <repo-url>
cd carbon_audit_manager

# 2. Crear base de datos
psql -U postgres -f database/create_user_and_db.sql
psql -U carbon_audit -d carbon_audit -f database/create_db_tables.sql

# 3. Configurar API key
echo "ORS_API_KEY=tu_clave_aqui" > .env

# 4. Compilar y ejecutar
cd carbon_audit_manager_app
mvn clean package
mvn javafx:run
```

## Scopes de Emisión

| Scope | Tipo | Ejemplos |
|-------|------|---------|
| Scope 1 | Emisiones directas | Combustible propio, calefacción |
| Scope 2 | Electricidad comprada | Factura eléctrica |
| Scope 3 | Emisiones indirectas | Commuting de empleados |

## Arquitectura

Patrón DAO de tres capas sobre PostgreSQL con JDBC manual (sin ORM):

- **Model** — POJOs puros con relaciones por composición
- **DAO** — Persistencia con `DatabaseManager` singleton
- **Service** — Lógica de cálculo de emisiones e integración con ORS

Más detalles en `/docs/architecture/`.

## Estructura del Proyecto

```
carbon_audit_manager/
├── carbon_audit_manager_app/
│   ├── src/main/java/com/carbonaudit/
│   │   ├── dao/        Capa de persistencia (JDBC)
│   │   ├── model/      Entidades (POJOs)
│   │   ├── service/    Lógica de cálculo y geolocalización
│   │   └── test/       Clases de prueba
│   └── pom.xml
├── database/           Scripts SQL de inicialización
└── docs/               Arquitectura, esquema BD y diagramas
```

## Estado del Proyecto

🟡 **En Desarrollo**

- [x] Modelo de dominio y DAOs
- [x] Cálculo de huella de carbono (Scopes 1, 2, 3)
- [x] Integración con OpenRouteService
- [x] Persistencia en PostgreSQL
- [x] Documentación técnica
- [ ] Interfaz gráfica JavaFX
- [ ] Tests unitarios con JUnit

## Resolución de Problemas

**Error de conexión a PostgreSQL**
- Verifica que el servicio esté activo: `psql -U postgres`
- Comprueba usuario/contraseña en `create_user_and_db.sql`

**OpenRouteService devuelve error**
- Valida que `ORS_API_KEY` en `.env` sea correcta en https://openrouteservice.org

## Licencia

MIT License — ver [LICENSE](LICENSE)

## Autor

Noé Tostón Carballo
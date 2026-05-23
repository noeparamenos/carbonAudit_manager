# Guión de Presentación — CarbonAudit Manager

> Tiempo estimado total: 10–12 minutos + demostración.
> Las notas entre corchetes son recordatorios internos, no se dicen en voz alta.

---

## Diapositiva 1 · Portada

*[Esperar a que el público esté atento. No hace falta decir mucho.]*

> "Buenos días. Mi proyecto se llama **CarbonAudit Manager**, una aplicación de escritorio para gestionar auditorías de huella de carbono en pequeñas y medianas empresas."

---

## Diapositiva 2 · El surgir de una Idea

> "La idea nace de una realidad que afecta a muchas empresas hoy en día. Por un lado, la normativa y los mercados exigen cada vez más transparencia sobre el impacto ambiental. Por otro, los precios de la energía han subido considerablemente, y las empresas necesitan saber exactamente dónde consumen y cuánto les cuesta en emisiones."

> "El problema es que las herramientas que existen para medir esto están pensadas para grandes corporaciones. Las PYMEs quedan en desventaja: o no tienen acceso, o son demasiado complejas."

> "La solución que propongo es una aplicación de escritorio, que funciona en local, que automatiza el cálculo de emisiones, permite monitorizar los consumos mes a mes, y organiza todo por alcances siguiendo el estándar GHG Protocol."

---

## Diapositiva 3 · Segmentación por Alcances (GHG Protocol)

> "Para entender cómo funciona el cálculo, hay que conocer el GHG Protocol, que es el estándar internacional más extendido para medir emisiones."

> "Divide las emisiones en tres alcances. El **Alcance 1** recoge las emisiones directas de la empresa: calderas, vehículos propios... El **Alcance 2** recoge las indirectas derivadas de la energía que compras: la factura eléctrica principalmente."

> "Y el **Alcance 3** engloba el resto de emisiones indirectas de la cadena de valor. Aquí es donde se centra el proyecto: el **commuting**, es decir, los desplazamientos diarios de los empleados para ir al trabajo. Es un dato que muchas empresas ignoran pero que tiene un impacto real."

> "La fórmula es sencilla: emisiones = cantidad consumida × factor de emisión. Lo que hace la aplicación es aplicar esa fórmula de forma automática para todos los consumos registrados."

---

## Diapositiva 4 · Herramientas software

> "El proyecto está desarrollado en **Java 21**, con **JavaFX** para la interfaz gráfica de escritorio. La base de datos es **PostgreSQL**, gestionada con JDBC puro, sin ORM, para mantener el control total sobre los tipos de datos."

> "**Maven** gestiona las dependencias y el ciclo de construcción. El control de versiones se ha llevado con **Git y GitHub**, siguiendo GitHub Flow. Y el entorno de desarrollo ha sido **IntelliJ IDEA**."

*[Si hay preguntas sobre por qué no Spring o Hibernate, la respuesta es: la aplicación es de escritorio y standalone, no necesita servidor web ni ORM.]*

---

## Diapositiva 5 · Arquitectura de la aplicación

> "La arquitectura sigue el patrón DAO de tres capas. En la base está **PostgreSQL**. Encima, la capa **DAO** que se encarga de toda la comunicación con la base de datos usando JDBC manual. La capa de **servicio** contiene la lógica de negocio: los cálculos de emisiones, la integración con la API externa de geolocalización. Y en la parte superior, los **controladores JavaFX** que gestionan la interfaz."

> "Algunas decisiones de diseño destacadas: se usa un **Singleton** con pool de conexiones HikariCP para no abrir una conexión nueva en cada operación. Y la integración con el servicio de mapas se hace a través de una **interfaz**, lo que permitiría cambiar el proveedor en el futuro sin tocar el resto del código."

---

## Diapositiva 6 · Base de datos

> "La base de datos tiene ocho tablas. La jerarquía principal es **empresa → departamento → empleado**. Cada empleado tiene asociada una dirección con coordenadas, que se usa para calcular su distancia al trabajo."

> "Los consumos mensuales y los datos de commuting se registran por separado, cada uno vinculado a su departamento. Los factores de emisión son globales y configurables por el administrador."

> "Una decisión importante: los empleados usan **soft-delete**, es decir, no se borran físicamente sino que se registra una fecha de baja. Esto preserva el historial de auditoría. Las empresas y departamentos, en cambio, usan borrado en cascada."

---

## Diapositiva 7 · Funcionalidades destacadas

> "La aplicación tiene dos perfiles de usuario. El **administrador** gestiona la estructura completa: empresas, departamentos, empleados y los factores de emisión. Cuando introduce una dirección, la aplicación geocodifica automáticamente las coordenadas en segundo plano."

> "El **responsable de departamento** registra los consumos mes a mes, seleccionando el tipo de energía y la cantidad. La aplicación calcula automáticamente las emisiones y las desglosa por Scope. También puede duplicar consumos entre meses para agilizar la introducción de datos, y exportar el informe en PDF o CSV."

> "El cálculo del commuting es especialmente relevante: la aplicación consulta la API de OpenRouteService para calcular la distancia real por carretera entre el domicilio del empleado y el lugar de trabajo, y luego estima el impacto mensual en función del tipo de transporte."

---

## Diapositiva 8 · Posibles desarrollos posteriores

> "El proyecto tiene margen de crecimiento. Las mejoras más relevantes serían: un **sistema de autenticación** con pantalla de login para sustituir la selección de rol actual; los **gráficos de evolución histórica**, que están preparados como placeholder pero sin datos; y el **empaquetado para distribución**, con un instalador nativo para la aplicación y Docker Compose para levantar la base de datos con un solo comando."

> "También está pendiente la optimización de consultas SQL, ya que la arquitectura de composición actual genera muchas consultas encadenadas que se podrían reducir con JOINs."

---

## Diapositiva 9 · Recursos bibliográficos

> "Las referencias principales han sido el estándar **GHG Protocol** para todo lo relacionado con el cálculo de emisiones, la documentación oficial de **JavaFX 21** y **PostgreSQL**, y la **API de OpenRouteService** para la geolocalización."

*[No hace falta leer toda la lista. Basta con mencionar las más relevantes.]*

---

## Diapositiva 10 · Agradecimientos

> "Quiero agradecer a [nombre del tutor] su orientación durante el desarrollo del proyecto."

> "Y con esto concluye la parte expositiva. Si os parece, pasamos directamente a la **demostración de la aplicación**."

*[En la demo, seguir el flujo: crear empresa → crear departamento → crear empleado → registrar consumo → consultar huella. Tener datos precargados para no perder tiempo.]*

---

## Posibles preguntas del tribunal

**¿Por qué no usas Spring o Hibernate?**
> "La aplicación es de escritorio y standalone. Spring está orientado a aplicaciones web con servidor, e Hibernate añadiría una capa de abstracción que en este caso no aporta nada y podría ocultar problemas de precisión numérica que en una auditoría son críticos."

**¿Por qué JavaFX y no una web app?**
> "El requisito era una aplicación de escritorio que funcione en local sin depender de un servidor ni de conexión a internet. JavaFX es la tecnología estándar de Java para eso."

**¿Qué es el commuting y por qué está en Scope 3?**
> "El commuting son los desplazamientos diarios de los empleados para ir al trabajo. Está en Scope 3 porque la empresa no controla directamente esas emisiones, pero sí las genera indirectamente al tener empleados que se desplazan."

**¿Cómo garantizas la precisión en los cálculos?**
> "Usando `BigDecimal` en todos los campos numéricos, tanto en Java como `DECIMAL` con precisión explícita en PostgreSQL. Esto evita los errores de redondeo que tendrían `double` o `float`, lo cual es esencial en una auditoría."
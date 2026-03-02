# Prices API - Prueba Técnica

API REST de consulta de precios para una cadena de comercio electrónico. Dado un producto, una marca y una fecha de aplicación, devuelve el precio y la tarifa aplicable, resolviendo solapamientos por prioridad.

## Stack Tecnológico

| Tecnología | Versión |
|---|---|
| Java | 21 (Temurin) |
| Spring Boot | 4.0.3 |
| Maven | 3.9+ |
| H2 Database | En memoria |
| MapStruct | 1.6.3 |
| JUnit 5 | Gestionada por Boot |

## Requisitos Previos

- Java 21+ instalado (`java -version`)
- Maven 3.9+ instalado (`mvn -version`)

## Cómo Ejecutar

```bash
# Compilar el proyecto
mvn compile

# Arrancar la aplicación
mvn spring-boot:run

# La aplicación arranca en http://localhost:8080
```

## Endpoint

```
GET /api/prices?applicationDate={date}&productId={id}&brandId={id}
```

**Parámetros:**
- `applicationDate`: Fecha de aplicación en formato ISO 8601 (ej: `2020-06-14T10:00:00`)
- `productId`: Identificador del producto (ej: `35455`)
- `brandId`: Identificador de la cadena (ej: `1` para ZARA)

**Ejemplos (los 5 casos del enunciado):**

```bash
# Caso 1: 14/06 10:00 → tarifa 1, precio 35.50 €
curl "http://localhost:8080/api/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1"

# Caso 2: 14/06 16:00 → tarifa 2, precio 25.45 € (promoción de mayor prioridad)
curl "http://localhost:8080/api/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"

# Caso 3: 14/06 21:00 → tarifa 1, precio 35.50 € (la promoción ya no aplica)
curl "http://localhost:8080/api/prices?applicationDate=2020-06-14T21:00:00&productId=35455&brandId=1"

# Caso 4: 15/06 10:00 → tarifa 3, precio 30.50 €
curl "http://localhost:8080/api/prices?applicationDate=2020-06-15T10:00:00&productId=35455&brandId=1"

# Caso 5: 16/06 21:00 → tarifa 4, precio 38.95 €
curl "http://localhost:8080/api/prices?applicationDate=2020-06-16T21:00:00&productId=35455&brandId=1"
```

**Respuesta de ejemplo (Caso 1):**
```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00",
  "endDate": "2020-12-31T23:59:59",
  "price": 35.50,
  "currency": "EUR"
}
```

## Cómo Ejecutar Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar solo los tests de integración
mvn test -Dtest=PriceRestAdapterIT
```

Se incluyen 5 tests de integración que validan el endpoint con distintas combinaciones de fecha, producto y marca, verificando la resolución correcta de prioridades.

## Consola H2

Con la aplicación arrancada, la consola de H2 está disponible en:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:pricesdb`
- Usuario: `sa`
- Contraseña: *(vacía)*

## Arquitectura

El proyecto sigue **Arquitectura Hexagonal** (Puertos y Adaptadores):

```
com.sergio.prices/
├── domain/              ← Lógica de negocio pura (sin framework)
│   ├── model/           ← Entidad de dominio (Price)
│   ├── port/in/         ← Puerto de entrada (FindApplicablePriceUseCase)
│   ├── port/out/        ← Puerto de salida (PriceRepositoryPort)
│   └── exception/       ← Excepciones de dominio
├── application/         ← Orquestación de casos de uso
│   └── service/         ← PriceService (implementa el use case)
└── infrastructure/      ← Adaptadores (REST, JPA, config)
    ├── adapter/in/rest/ ← Endpoint REST
    ├── adapter/out/     ← Persistencia JPA + H2
    ├── config/          ← Configuración Spring
    └── exception/       ← Manejo global de errores
```

**Regla fundamental**: El paquete `domain/` no tiene ninguna dependencia de Spring, JPA ni ningún otro framework. Las dependencias siempre apuntan hacia dentro (infrastructure → application → domain).

## Documentación

- [Decisiones Técnicas](docs/DECISIONES.md) - Registro de decisiones (ADR)
- [Proceso con Claude Code](docs/PROCESO_CLAUDE_CODE.md) - Cómo se integró Claude Code en el desarrollo

## Desarrollo con Claude Code

Este proyecto se desarrolló utilizando Claude Code como herramienta de asistencia al desarrollo. Se configuraron:

- **CLAUDE.md**: Fichero de convenciones del proyecto que Claude lee al inicio de cada sesión
- **Skills**: 2 skills personalizados (`test-validator` y `code-review`) para automatizar tareas repetitivas

El proceso completo está documentado en [docs/PROCESO_CLAUDE_CODE.md](docs/PROCESO_CLAUDE_CODE.md).

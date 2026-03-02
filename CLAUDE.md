# CLAUDE.md - Prices API

## Contexto del Proyecto
API REST de consulta de precios para una cadena de comercio electrónico.
Dado un producto, marca y fecha, devuelve el precio aplicable resolviendo solapamientos
por prioridad. Alcance acotado: 1 entidad (Price), 1 endpoint, 5 tests de integración.
El equipo usa Claude Code en su workflow diario.

## Stack Tecnológico
- Java 21 (Temurin 21.0.3) + Spring Boot 4.0.3 + Maven 3.9.11
- H2 en memoria
- MapStruct 1.6.3 para mapeo entre capas (genera código en compilación, sin reflexión)
- JUnit 5 para tests de integración

**Peculiaridades de Spring Boot 4 que debes conocer:**
- El starter web se llama `spring-boot-starter-webmvc` (antes `spring-boot-starter-web`)
- Los starters de test son `spring-boot-starter-webmvc-test` y `spring-boot-starter-data-jpa-test`
- Hibernate 7.1 con Jakarta Persistence 3.2 (JPQL soporta LIMIT)

## Arquitectura: Hexagonal (Puertos y Adaptadores)

Tres capas con una regla de dependencia estricta:

```
infrastructure/ → application/ → domain/
   (depende de)    (depende de)    (NO depende de NADIE)
```

- `domain/`: POJO Java puro. CERO imports de Spring, Jakarta o JPA. Contiene el modelo
  de dominio (Price), puertos (interfaces) y excepciones de dominio.
  MOTIVO: el dominio es el corazón del negocio y debe ser testeable sin framework.
- `application/`: Orquesta casos de uso. Los servicios implementan puertos de entrada
  e inyectan puertos de salida. Puede usar @Service y @Transactional.
  MOTIVO: separa "qué hace la aplicación" de "cómo lo hace".
- `infrastructure/`: Todo lo que depende de frameworks. Adaptadores REST, JPA, mappers,
  configuración, manejo de excepciones global.
  MOTIVO: si cambias H2 por MongoDB, solo tocas infrastructure/adapter/out/.

## Convenciones de Código
- Código, variables, métodos, clases: en INGLÉS
- Documentación (README, decisiones, proceso): en ESPAÑOL
- Google Java Style para formateo
- Usar `final` en parámetros de método y variables locales no reasignadas
- Tabs: NO. Usar 4 espacios de indentación

**DTOs inmutables como records de Java 21:**
```java
public record PriceResponse(
        long brandId,
        long productId,
        int priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String currency) {}
```

```java
public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
```

## Convenciones de Naming
- Paquetes: lowercase sin guiones (com.sergio.prices.domain.model)
- Clases: PascalCase
- Métodos/variables: camelCase
- Constantes: UPPER_SNAKE_CASE
- Entidades JPA: sufijo `JpaEntity` (PriceJpaEntity) para distinguir del modelo de dominio
- Controladores REST: sufijo `RestAdapter` (PriceRestAdapter) para ser explícito con hexagonal
- Puertos de entrada: sufijo `UseCase` (FindApplicablePriceUseCase)
- Puertos de salida: sufijo `Port` (PriceRepositoryPort)
- Tests de integración: sufijo `IT` (PriceRestAdapterIT)

## Restricciones Explícitas (DO NOT)
- NO usar Lombok. El proyecto es pequeño y los records de Java 21 cubren la necesidad
  de DTOs inmutables. MapStruct funciona mejor sin la complejidad de Lombok.
- NO añadir Swagger/OpenAPI. No forma parte del alcance del proyecto.
- NO crear Request DTO. Son 3 query params de un GET (@RequestParam). Un wrapper no aporta nada.
- NO sobredimensionar. Un solo fichero por responsabilidad. Sin capas de abstracción extra.
  Si te pido algo sencillo, no generes una solución enterprise de 8 ficheros.
- NO crear más paquetes de los definidos en la estructura hexagonal.
- NO añadir comentarios Javadoc triviales ("Gets the price" sobre getPrice()).
  Solo comentar donde la lógica no sea autoexplicativa.

## Base de Datos
- H2 en memoria: `jdbc:h2:mem:pricesdb`
- Tabla PRICES definida en `src/main/resources/schema.sql`
- Datos iniciales en `src/main/resources/data.sql` (4 registros del enunciado)
- Consola H2 habilitada en `/h2-console` (solo desarrollo)

## Testing
- Tests de integración con @SpringBootTest(webEnvironment = RANDOM_PORT)
- 5 tests de integración validando el endpoint GET /api/prices:
  - Test 1: 2020-06-14T10:00 → priceList=1, price=35.50
  - Test 2: 2020-06-14T16:00 → priceList=2, price=25.45
  - Test 3: 2020-06-14T21:00 → priceList=1, price=35.50
  - Test 4: 2020-06-15T10:00 → priceList=3, price=30.50
  - Test 5: 2020-06-16T21:00 → priceList=4, price=38.95
- Formato de fecha en tests: ISO 8601 (2020-06-14T10:00:00)

## Manejo de Errores
- `PriceNotFoundException` en domain/exception/ (RuntimeException, sin Spring)
- `GlobalExceptionHandler` en infrastructure/exception/ con @RestControllerAdvice
- Respuesta de error consistente: `{ "status": 404, "message": "...", "timestamp": "..." }`
- Usar `ErrorResponse` como Java record

## Git
- Conventional commits en inglés: feat:, fix:, test:, docs:, refactor:
- Un commit por fase de desarrollo
- No hacer push automático sin confirmación

## Comandos Útiles
```bash
mvn compile                    # Compilar
mvn test                       # Ejecutar tests
mvn spring-boot:run            # Arrancar aplicación
mvn clean verify               # Build completo
```

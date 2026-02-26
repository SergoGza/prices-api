# Registro de Decisiones Técnicas (ADR)

Formato: ADR lite. Cada decisión incluye contexto, decisión tomada y motivo.

---

## ADR-001: Arquitectura Hexagonal en lugar de Capas

**Contexto**: El enunciado pide un servicio Spring Boot con 1 endpoint y 5 tests.
Funcionalmente, una arquitectura por capas (Controller → Service → Repository) sería
suficiente y más rápida de implementar.

**Decisión**: Usar arquitectura hexagonal (puertos y adaptadores).

**Motivo**: El objetivo principal de esta prueba no es solo que funcione, sino demostrar
criterio técnico. Hexagonal muestra comprensión de principios SOLID (especialmente
Dependency Inversion), separación de concerns y diseño desacoplado. Además, es la primera
experiencia del desarrollador con este patrón, lo que añade valor formativo.

**Trade-off aceptado**: Más ficheros (~15 vs ~8) para el mismo resultado funcional.

---

## ADR-002: Resolución de prioridad en SQL, no en Java

**Contexto**: Cuando varias tarifas solapan en un rango de fechas para el mismo producto
y marca, debe aplicarse la de mayor prioridad (mayor valor numérico).

**Decisión**: La query JPQL incluye `ORDER BY p.priority DESC LIMIT 1`.

**Motivo**:
- Delega el filtrado a la BD, que está optimizada para ello
- Retorna 1 sola fila en vez de N candidatos
- El servicio queda limpio: solo llama al puerto y maneja el caso vacío
- En un escenario real con millones de registros, esto es crítico para el rendimiento

**Alternativa descartada**: Traer todas las tarifas coincidentes y ordenar/filtrar en Java
con streams. Funciona para 4 registros pero no escala.

---

## ADR-003: MapStruct para mapeo entre capas

**Contexto**: La arquitectura hexagonal requiere dos mapeos:
1. `PriceJpaEntity` ↔ `Price` (entre persistencia y dominio)
2. `Price` → `PriceResponse` (entre dominio y REST)

**Decisión**: Usar MapStruct 1.6.3.

**Motivo**:
- Genera código de mapeo en tiempo de compilación (sin reflexión en runtime)
- Type-safe: errores de mapeo se detectan al compilar
- Integración nativa con Spring (`componentModel = "spring"`)
- Es la herramienta que el desarrollador usa profesionalmente

**Alternativa descartada**: Mapeo manual con métodos estáticos. Funcionaría para este
caso simple, pero MapStruct es más mantenible y demuestra una práctica profesional.

---

## ADR-004: No usar Lombok

**Contexto**: Lombok reduce boilerplate (getters, setters, constructors) pero añade
una dependencia de compilación y complejidad en la configuración del IDE.

**Decisión**: No incluir Lombok.

**Motivo**:
- Java 21 records cubren la necesidad de DTOs inmutables (PriceResponse, ErrorResponse)
- El proyecto es pequeño: los getters/setters explícitos en PriceJpaEntity y Price son manejables
- MapStruct funciona mejor sin la complejidad que Lombok añade al procesamiento de anotaciones
- Menos "magia": el código hace exactamente lo que se ve

---

## ADR-005: No crear Request DTO

**Contexto**: El endpoint acepta 3 parámetros: `applicationDate`, `productId`, `brandId`.

**Decisión**: Usar `@RequestParam` directamente en el método del controlador.

**Motivo**:
- Es un GET con query parameters, no un POST con body
- Crear una clase wrapper para 3 parámetros simples añade un fichero sin valor
- `@RequestParam` es la forma idiomática de Spring para este caso
- La validación de tipos ya la hace Spring (LocalDateTime, Long)

---

## ADR-006: PriceJpaEntity separada de Price (dominio)

**Contexto**: En la mayoría de proyectos Spring Boot, la entidad JPA ES el modelo de
dominio. Una sola clase `Price.java` con `@Entity` y lógica de negocio.

**Decisión**: Dos clases separadas: `Price` (dominio puro) y `PriceJpaEntity` (JPA).

**Motivo**: Este es el corazón de la arquitectura hexagonal. Si el modelo de dominio
lleva `@Entity`, el dominio depende de JPA. Eso rompe la regla fundamental: el dominio
no depende de nadie. Con dos clases:
- `Price` se puede testear sin Spring, sin base de datos, sin framework
- Si cambias la BD, solo tocas `PriceJpaEntity` y su mapper
- El dominio queda protegido de cambios de infraestructura

**Trade-off**: Necesitas un mapper (PricePersistenceMapper) para convertir entre ambas.
MapStruct genera este código automáticamente.

---

## ADR-007: Spring Boot 4.0.3 con Java 21

**Contexto**: Spring Boot 3.x con Java 17 sería la opción conservadora y ampliamente
documentada. Spring Boot 4.0.3 es la última versión estable (Feb 2026).

**Decisión**: Usar Spring Boot 4.0.3 con Java 21.

**Motivo**:
- Demuestra conocimiento de las últimas versiones del ecosistema
- Java 21 es LTS y aporta records, pattern matching, virtual threads
- Spring Boot 4 está sobre Spring Framework 7, que trae mejoras en null safety y modularización
- La empresa a la que se postula probablemente valora estar al día tecnológicamente

**Riesgos asumidos**:
- Menos documentación y ejemplos en Stack Overflow que Boot 3.x
- Algunos cambios de API (Jackson 3 packages, starter names) requieren atención
- Si alguna herramienta no es compatible, se documentará el fallback

---

## ADR-008: H2 en memoria (sin PostgreSQL/Docker)

**Contexto**: El enunciado dice "base de datos en memoria (tipo H2)". El desarrollador
inicialmente consideró usar PostgreSQL en Docker.

**Decisión**: Seguir el enunciado y usar H2.

**Motivo**:
- Cumple exactamente lo que pide la prueba
- Simplifica la ejecución: `mvn spring-boot:run` sin dependencias externas
- Los 5 tests funcionan sin Docker ni configuración adicional
- La arquitectura hexagonal hace que cambiar a PostgreSQL sea trivial en el futuro
  (solo tocar el adaptador de persistencia)

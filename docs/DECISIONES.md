# Registro de Decisiones Técnicas (ADR)

Formato: ADR lite. Cada decisión incluye contexto, decisión tomada y motivo.

---

## ADR-001: Arquitectura Hexagonal en lugar de Capas

**Contexto**: El enunciado pide un servicio Spring Boot con 1 endpoint y 5 tests.
Mi arquitectura habitual es layered (Controller → Service → Repository), bien estructurada
y suficiente para este caso. Durante la entrevista se preguntó por el conocimiento de
arquitectura hexagonal.

**Decisión**: Usar arquitectura hexagonal (puertos y adaptadores).

**Motivo**: Aprovechar la prueba como contexto acotado para trabajar con hexagonal
directamente, sabiendo que es sobreingeniería para el problema en cuestión.

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


## ADR-003: No usar Lombok

**Contexto**: Lombok reduce boilerplate (getters, setters, constructors) pero añade
una dependencia de compilación y complejidad en la configuración del IDE.

**Decisión**: No incluir Lombok.

**Motivo**:
- Java 21 records cubren la necesidad de DTOs inmutables (PriceResponse, ErrorResponse)
- El proyecto es pequeño: los getters/setters explícitos en PriceJpaEntity y Price son manejables
- MapStruct funciona mejor sin la complejidad que Lombok añade al procesamiento de anotaciones

---

## ADR-004: No crear Request DTO

**Contexto**: El endpoint acepta 3 parámetros: `applicationDate`, `productId`, `brandId`.

**Decisión**: Usar `@RequestParam` directamente en el método del controlador.

**Motivo**:
- Es un GET con query parameters, no un POST con body
- Crear una clase wrapper para 3 parámetros simples añade un fichero sin valor
- `@RequestParam` es la forma idiomática de Spring para este caso
- La validación de tipos ya la hace Spring (LocalDateTime, Long)

---

## ADR-005: Spring Boot 4.0.3 con Java 21

**Contexto**: Spring Boot 3.x con Java 17 sería la opción conservadora y ampliamente
documentada. En la entrevista se mencionó que el equipo trabaja con versiones recientes
del stack debido a la criticidad del producto (pagos).

**Decisión**: Usar Spring Boot 4.0.3 (última versión estable, Feb 2026) con Java 21 LTS.

**Motivo**:
- Alinearse con la filosofía del equipo de trabajar con versiones recientes
- Java 21 es LTS y aporta records, pattern matching, virtual threads
- Spring Boot 4 está sobre Spring Framework 7, con mejoras en null safety y modularización

**Riesgos asumidos**:
- Menos documentación y ejemplos que Boot 3.x
- Algunos cambios de API (starters renombrados, Hibernate 7.1) requieren atención

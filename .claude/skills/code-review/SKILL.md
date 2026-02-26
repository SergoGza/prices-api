---
name: code-review
description: Revisa el código para verificar que cumple la arquitectura hexagonal y las convenciones del proyecto. Activar cuando el usuario pida una revisión, code review, o validación de arquitectura.
---

# Code Review - Prices API

## 1. Fronteras Arquitectónicas (CRÍTICO)

### Paquete domain/ (com.sergio.prices.domain)
- **PROHIBIDO**: Cualquier import de `org.springframework.*`, `jakarta.*`, `tools.jackson.*`
- **PERMITIDO**: Solo `java.*` y `com.sergio.prices.domain.*`
- **Verificación rápida**: `grep -r "import org.springframework" src/main/java/com/sergio/prices/domain/` debe devolver 0 resultados

### Paquete application/ (com.sergio.prices.application)
- **PERMITIDO**: `@Service`, `@Transactional` de Spring
- **PROHIBIDO**: Importar clases de `infrastructure.*` directamente
- **REGLA**: Solo referencia interfaces de `domain.port`, nunca implementaciones de adaptadores

### Paquete infrastructure/ (com.sergio.prices.infrastructure)
- **LIBRE**: Puede usar cualquier anotación de Spring, JPA, Jackson
- **REGLA**: Los adaptadores DEBEN implementar las interfaces (puertos) definidas en domain

## 2. Convenciones de Naming
- [ ] Entidad JPA: sufijo `JpaEntity` (PriceJpaEntity)
- [ ] Controller: sufijo `RestAdapter` (PriceRestAdapter)
- [ ] Puerto entrada: sufijo `UseCase` (FindApplicablePriceUseCase)
- [ ] Puerto salida: sufijo `Port` (PriceRepositoryPort)
- [ ] Test integración: sufijo `IT` (PriceRestAdapterIT)

## 3. Calidad de Código
- [ ] DTOs de respuesta son Java records (no clases con getters/setters)
- [ ] ErrorResponse es un Java record
- [ ] No hay uso de Lombok en ningún fichero
- [ ] `final` en parámetros de métodos y variables locales no reasignadas
- [ ] Constructor injection (no @Autowired en campos)
- [ ] MapStruct con componentModel = "spring" (configurado en el compiler plugin)

## 4. Lógica de Negocio
- [ ] La resolución de prioridad se hace en la query SQL (ORDER BY priority DESC LIMIT 1)
- [ ] El servicio NO contiene lógica de filtrado/ordenación
- [ ] PriceNotFoundException se lanza en la capa application, no en infrastructure
- [ ] GlobalExceptionHandler captura PriceNotFoundException y devuelve 404

## 5. Estructura de Respuesta
- [ ] GET /api/prices devuelve: productId, brandId, priceList, startDate, endDate, price, currency
- [ ] Error 404 devuelve: status, message, timestamp
- [ ] Los campos de fecha usan ISO 8601

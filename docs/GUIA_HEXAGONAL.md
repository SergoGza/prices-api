# Guía de Arquitectura Hexagonal (para desarrolladores con experiencia en Layered)

## 1. ¿Qué es la Arquitectura Hexagonal?

La **Arquitectura Hexagonal** (también llamada **Puertos y Adaptadores**) fue propuesta por
Alistair Cockburn en 2005. Su idea central es simple:

> **El dominio de negocio no debe depender de nada externo.**

Ni de la base de datos, ni del framework web, ni de ninguna librería. El dominio define
*qué necesita* mediante interfaces (puertos), y el mundo exterior proporciona implementaciones
concretas (adaptadores).

### Diagrama conceptual

```
                    ┌─────────────────────────────────────────┐
                    │           INFRASTRUCTURE                │
                    │                                         │
                    │   ┌─────────────────────────────────┐   │
                    │   │         APPLICATION              │   │
                    │   │                                   │   │
                    │   │   ┌───────────────────────────┐   │   │
  HTTP Request ───►│   │   │        DOMAIN              │   │   │──► Base de datos
  (Adaptador IN)   │   │   │                           │   │   │   (Adaptador OUT)
                    │   │   │  Price (modelo puro)      │   │   │
                    │   │   │  UseCase (puerto IN)      │   │   │
                    │   │   │  RepositoryPort (OUT)     │   │   │
                    │   │   │                           │   │   │
                    │   │   └───────────────────────────┘   │   │
                    │   │                                   │   │
                    │   │  PriceService (implementa UseCase)│   │
                    │   └─────────────────────────────────┘   │
                    │                                         │
                    │  RestAdapter, JpaEntity, Mappers, Config│
                    └─────────────────────────────────────────┘
```

**Regla de dependencia**: Las flechas SIEMPRE apuntan hacia dentro.
- `infrastructure` depende de `application`
- `application` depende de `domain`
- `domain` NO depende de nadie

---

## 2. Comparativa directa: tu Layered vs Hexagonal

Esta tabla compara exactamente lo que haces en tu trabajo con lo que hacemos en este proyecto.

### Modelo de datos

**Tu Layered:**
```java
// Un solo fichero: Price.java
@Entity
@Table(name = "PRICES")
public class Price {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "BRAND_ID")
    private Long brandId;
    // ... JPA está mezclado con el dominio
}
```

**Hexagonal:**
```java
// Fichero 1: domain/model/Price.java (POJO puro, sin @Entity)
public class Price {
    private Long brandId;
    private LocalDateTime startDate;
    // ... solo Java, nada de JPA
}

// Fichero 2: infrastructure/adapter/out/persistence/PriceJpaEntity.java
@Entity
@Table(name = "PRICES")
public class PriceJpaEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "BRAND_ID")
    private Long brandId;
    // ... aquí sí vive JPA, pero separado del dominio
}
```

**¿Por qué dos clases?** Porque si mañana cambias H2 por MongoDB, solo tocas
`PriceJpaEntity` y el adaptador de persistencia. `Price` (dominio) no se entera.
En tu layered, cambiar la BD te obliga a tocar la entidad que usan TODAS las capas.

---

### Interfaces de servicio

**Tu Layered:**
```java
// service/PriceService.java → nombre técnico
public interface PriceService {
    PriceDTO findPrice(LocalDateTime date, Long productId, Long brandId);
}

// impl/PriceServiceImpl.java
@Service
public class PriceServiceImpl implements PriceService {
    @Autowired
    private PriceRepository repository; // inyecta JpaRepository directamente
}
```

**Hexagonal:**
```java
// domain/port/in/FindApplicablePriceUseCase.java → nombre de NEGOCIO
public interface FindApplicablePriceUseCase {
    Price findApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}

// application/service/PriceService.java
@Service
public class PriceService implements FindApplicablePriceUseCase {
    private final PriceRepositoryPort repositoryPort; // inyecta un PUERTO, no JPA
}
```

**Diferencias clave:**
1. El nombre del puerto es de negocio (`FindApplicablePriceUseCase`), no técnico (`PriceService`)
2. El servicio inyecta un puerto abstracto (`PriceRepositoryPort`), no un `JpaRepository`
3. El servicio devuelve `Price` (dominio), no un DTO. El mapeo a DTO ocurre en el adaptador REST

---

### Acceso a datos

**Tu Layered:**
```java
// repository/PriceRepository.java → directamente es un JpaRepository
public interface PriceRepository extends JpaRepository<Price, Long> {
    // La interfaz del repositorio está acoplada a JPA Y a la entidad
}
```

**Hexagonal:**
```java
// domain/port/out/PriceRepositoryPort.java → interfaz PURA en el dominio
public interface PriceRepositoryPort {
    Optional<Price> findApplicablePrice(LocalDateTime date, Long productId, Long brandId);
}

// infrastructure/adapter/out/persistence/PriceJpaRepository.java → detalle de implementación
public interface PriceJpaRepository extends JpaRepository<PriceJpaEntity, Long> {
    @Query("SELECT p FROM PriceJpaEntity p WHERE ...")
    Optional<PriceJpaEntity> findApplicablePrice(...);
}

// infrastructure/adapter/out/persistence/PricePersistenceAdapter.java → CONECTA ambos mundos
@Component
public class PricePersistenceAdapter implements PriceRepositoryPort {
    private final PriceJpaRepository jpaRepository;
    private final PricePersistenceMapper mapper;

    @Override
    public Optional<Price> findApplicablePrice(...) {
        return jpaRepository.findApplicablePrice(...)
            .map(mapper::toDomain);  // Convierte JpaEntity → Domain
    }
}
```

**¿Por qué tres ficheros en vez de uno?**
- `PriceRepositoryPort`: dice "necesito buscar precios" (el QUÉ)
- `PriceJpaRepository`: sabe hacer queries SQL con JPA (el CÓMO con JPA)
- `PricePersistenceAdapter`: conecta el puerto con JPA y mapea entre mundos

Si cambias a MongoDB, solo reemplazas `PriceJpaRepository` por `PriceMongoRepository`
y ajustas el adapter. El puerto y todo lo que depende de él sigue intacto.

---

### Controller / Adaptador REST

**Tu Layered:**
```java
@RestController
@RequestMapping("/api/prices")
public class PriceController {
    @Autowired
    private PriceService priceService; // Inyecta la interfaz de servicio
}
```

**Hexagonal:**
```java
@RestController
@RequestMapping("/api/prices")
public class PriceRestAdapter {
    private final FindApplicablePriceUseCase findApplicablePriceUseCase; // Inyecta el PUERTO
    private final PriceRestMapper mapper;

    @GetMapping
    public ResponseEntity<PriceResponse> findApplicablePrice(...) {
        Price price = findApplicablePriceUseCase.findApplicablePrice(...);
        return ResponseEntity.ok(mapper.toResponse(price));
    }
}
```

**Diferencias:**
1. Se llama `RestAdapter` (no `Controller`) para ser explícito con el patrón
2. Inyecta `FindApplicablePriceUseCase` (el puerto), no `PriceService` (la implementación)
3. Mapea `Price` (dominio) → `PriceResponse` (DTO) en el adaptador, no en el servicio
4. Internamente sigue siendo un `@RestController` de Spring, no cambia la funcionalidad

---

## 3. ¿Por qué más ficheros NO es más complejidad?

| Aspecto | Layered (~8 ficheros) | Hexagonal (~15 ficheros) |
|---|---|---|
| Cada fichero tiene... | A veces varias responsabilidades | UNA sola responsabilidad |
| Si cambias la BD... | Tocas Entity + Repository + quizás Service | Solo infrastructure/adapter/out/ |
| Si añades gRPC además de REST... | Nuevo controller que inyecta el mismo Service | Nuevo adapter en infrastructure/adapter/in/ |
| Para testear el dominio... | Necesitas mockear JPA, Spring, etc. | Puro Java, sin framework |
| Para entender una capa... | Necesitas saber qué frameworks usa | Lees interfaces simples |

### Ejemplo práctico: "Si mañana cambias H2 por MongoDB"

**En tu Layered:**
1. Cambiar `@Entity` por `@Document` en `Price.java` → afecta a TODO lo que use Price
2. Cambiar `JpaRepository` por `MongoRepository` en `PriceRepository.java`
3. Posiblemente tocar `PriceServiceImpl.java` si usaba métodos específicos de JPA
4. Revisar los mappers porque la entidad cambió

**En Hexagonal:**
1. Crear `PriceMongoDocument.java` (nueva entidad Mongo) → NO tocas `Price.java` del dominio
2. Crear `PriceMongoRepository.java` (nuevo repo)
3. Modificar `PricePersistenceAdapter.java` para usar Mongo
4. Actualizar `PricePersistenceMapper.java` para mapear MongoDocument ↔ Price
5. **El dominio, el servicio y el adaptador REST no se tocan**

---

## 4. Cuándo SÍ y cuándo NO usar Hexagonal

### SÍ usar cuando:
- La lógica de dominio es compleja (reglas de negocio, validaciones, cálculos)
- Tienes múltiples adaptadores (REST + gRPC + eventos + colas)
- Quieres testear el dominio sin levantar Spring (tests rápidos y fiables)
- El proyecto va a crecer y lo mantienen varios equipos
- Quieres demostrar criterio técnico en una entrevista

### NO usar cuando:
- Es un CRUD simple sin lógica de negocio (ABM directo a BD)
- Es un prototipo rápido que se va a tirar
- Solo tú lo usas y sabes que no crecerá
- El plazo es muy ajustado y el equipo no conoce el patrón

### En esta prueba técnica:
Usamos hexagonal porque el objetivo principal es **demostrar criterio técnico**
ante la entrevistadora. La prueba en sí es sencilla (1 entidad, 1 endpoint),
pero la arquitectura muestra que sabemos cuándo y por qué aplicar patrones avanzados.
El "porqué" es tan importante como el "cómo".

---

## 5. Resumen: correspondencia rápida

| Lo que conoces (Layered) | Nombre en Hexagonal | Dónde vive |
|---|---|---|
| `Price.java` con `@Entity` | `Price.java` (POJO) + `PriceJpaEntity.java` | domain/model + infrastructure/adapter/out/ |
| `PriceService` (interfaz) | `FindApplicablePriceUseCase` | domain/port/in/ |
| `PriceServiceImpl` | `PriceService` | application/service/ |
| `PriceRepository extends JpaRepository` | `PriceRepositoryPort` (interfaz) + `PriceJpaRepository` + `PricePersistenceAdapter` | domain/port/out/ + infrastructure/adapter/out/ |
| `PriceController` | `PriceRestAdapter` | infrastructure/adapter/in/rest/ |
| `PriceDTO` (response) | `PriceResponse` (record) | infrastructure/adapter/in/rest/dto/ |
| `PriceMapper` | `PriceRestMapper` + `PricePersistenceMapper` | Cada adaptador tiene su mapper |
| `GlobalExceptionHandler` | `GlobalExceptionHandler` (igual) | infrastructure/exception/ |

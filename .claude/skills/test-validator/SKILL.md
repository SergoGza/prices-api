---
name: test-validator
description: Ejecuta y valida los 5 tests de integración requeridos para la API de Precios. Activar al crear, modificar o ejecutar tests, o cuando el usuario pida verificar el endpoint.
---

# Validación de Tests - Prices API

## Endpoint bajo test
`GET /api/prices?applicationDate={date}&productId={id}&brandId={id}`

## Especificaciones de los 5 tests requeridos

| Test | applicationDate       | productId | brandId | priceList esperado | price esperado | Motivo                                    |
|------|-----------------------|-----------|---------|-------------------|----------------|-------------------------------------------|
| 1    | 2020-06-14T10:00:00   | 35455     | 1       | 1                 | 35.50          | Solo aplica tarifa 1 (prioridad 0)        |
| 2    | 2020-06-14T16:00:00   | 35455     | 1       | 2                 | 25.45          | Tarifas 1 y 2 solapan, gana tarifa 2 (prioridad 1) |
| 3    | 2020-06-14T21:00:00   | 35455     | 1       | 1                 | 35.50          | Tarifa 2 ya expiró (18:30), solo queda tarifa 1 |
| 4    | 2020-06-15T10:00:00   | 35455     | 1       | 3                 | 30.50          | Tarifas 1 y 3 solapan, gana tarifa 3 (prioridad 1) |
| 5    | 2020-06-16T21:00:00   | 35455     | 1       | 4                 | 38.95          | Tarifas 1 y 4 solapan, gana tarifa 4 (prioridad 1) |

## Campos que debe devolver la respuesta
Cada respuesta JSON debe contener: `productId`, `brandId`, `priceList`, `startDate`, `endDate`, `price`, `currency`.

## Cómo ejecutar
```bash
# Solo los tests de integración
mvn test -Dtest=PriceRestAdapterIT

# Todos los tests
mvn test
```

## Criterios de validación
- Los 5 tests DEBEN pasar. No se admiten tests ignorados o saltados.
- Cada test verifica como mínimo: priceList y price del JSON de respuesta.
- El test usa @SpringBootTest con servidor real (RANDOM_PORT), no MockMvc aislado.
- Si algún test falla, analiza primero la query del repositorio (ORDER BY priority DESC LIMIT 1) antes de buscar errores en otras capas.

## Datos de referencia en la BD (data.sql)
```
ID | BRAND | START               | END                 | PRICE_LIST | PRODUCT | PRIORITY | PRICE | CURR
1  | 1     | 2020-06-14 00:00:00 | 2020-12-31 23:59:59 | 1          | 35455   | 0        | 35.50 | EUR
2  | 1     | 2020-06-14 15:00:00 | 2020-06-14 18:30:00 | 2          | 35455   | 1        | 25.45 | EUR
3  | 1     | 2020-06-15 00:00:00 | 2020-06-15 11:00:00 | 3          | 35455   | 1        | 30.50 | EUR
4  | 1     | 2020-06-15 16:00:00 | 2020-12-31 23:59:59 | 4          | 35455   | 1        | 38.95 | EUR
```

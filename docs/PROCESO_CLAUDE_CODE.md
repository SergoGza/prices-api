# Proceso de Desarrollo con Claude Code

Este documento explica cómo se ha integrado Claude Code en el desarrollo de esta prueba
técnica, siguiendo la petición de la entrevistadora de documentar el proceso y las
decisiones tomadas.

---

## 1. Fase de Planificación (antes de escribir código)

### Claude como sparring de arquitectura

Antes de tocar una sola línea de código, se usó Claude Code en **modo plan** (Shift+Tab x2)
para debatir las decisiones de diseño. El flujo fue:

1. **Lectura del contexto**: Se proporcionaron a Claude los documentos de la prueba técnica,
   el intercambio de correos con la entrevistadora, y un artículo de referencia sobre buenas
   prácticas con Claude Code.

2. **Preguntas de diseño**: Claude formuló preguntas sobre las decisiones clave:
   - ¿Base de datos? → H2 (siguiendo el enunciado)
   - ¿Build tool? → Maven (familiaridad)
   - ¿Arquitectura? → Se explicó la diferencia entre layered y hexagonal
   - ¿Idioma del código? → Inglés para código, español para documentación

3. **Aclaración de arquitectura**: El desarrollador describió su arquitectura habitual
   (Entity @Entity + Service interfaz+impl + Controller) creyendo que era hexagonal.
   Claude explicó la diferencia real y se tomó la decisión consciente de usar hexagonal
   como ejercicio de aprendizaje y demostración de criterio técnico.

### Instrucciones precisas, no vagas

Siguiendo los consejos del artículo de referencia ("La diferencia entre instrucciones
vagas y precisas es la diferencia entre resultados mediocres y resultados de producción"),
se definieron restricciones explícitas antes de empezar:

- "No uses Lombok"
- "No crees Request DTO para 3 query params"
- "No sobredimensiones: 1 entidad, 1 endpoint"
- "Resuelve la prioridad en SQL, no en Java"

---

## 2. CLAUDE.md: el briefing del proyecto

### ¿Qué es?

`CLAUDE.md` es un fichero en la raíz del proyecto que Claude Code lee al inicio de cada
sesión. Funciona como un briefing para un freelance: no le explicas qué es Spring Boot,
le explicas las rarezas de TU proyecto.

### Cómo se construyó

Se construyó de forma colaborativa entre el desarrollador y Claude:
- Claude propuso las secciones basándose en las decisiones ya tomadas
- El desarrollador validó y ajustó el contenido
- Se siguieron los principios: brevedad (~80 instrucciones), concreción, explicar el PORQUÉ

### Contenido clave

- Contexto del proyecto (prueba técnica, qué se valora)
- Peculiaridades de Spring Boot 4 (Jackson 3, starters renombrados)
- Reglas de la arquitectura hexagonal
- Convenciones de naming con sufijos explícitos
- Sección "DO NOT" para evitar sobreingeniería

---

## 3. Skills: automatización de tareas repetitivas

### ¿Qué son los Skills?

Son ficheros SKILL.md que enseñan a Claude tareas específicas del proyecto. Claude lee
solo la cabecera (nombre + descripción) al inicio y carga las instrucciones completas
bajo demanda cuando detecta que son relevantes.

### Skills creados para este proyecto

1. **test-validator**: Contiene la especificación de los 5 tests con los valores esperados.
   Cuando Claude ejecuta o escribe tests, tiene la referencia exacta de qué debe pasar.

2. **code-review**: Checklist de revisión que verifica las fronteras de la arquitectura
   hexagonal (que el dominio no tenga imports de Spring) y las convenciones de código.

### Por qué solo 2 skills

Para un proyecto con 1 entidad y 1 endpoint, crear más skills sería sobreingeniería.
La regla es: "Si te descubres explicándole lo mismo a Claude más de dos veces, eso es
un skill que falta."

---

## 4. Desarrollo por fases

El desarrollo se estructuró en fases independientes, cada una ejecutable como un
prompt separado en Claude Code:

| Fase | Descripción | Estado |
|------|-------------|--------|
| 0 | Scaffolding (git, Maven, H2, estructura) | Completada |
| 1 | CLAUDE.md | Completada |
| 2 | Skills | Completada |
| 3.1 | Capa de dominio | Pendiente |
| 3.2 | Adaptador de persistencia | Pendiente |
| 3.3 | Servicio de aplicación | Pendiente |
| 3.4 | Adaptador REST + errores | Pendiente |
| 3.5 | Tests de integración | Pendiente |
| 3.6 | Pulido | Pendiente |

**¿Por qué fases independientes?**
- Cada fase produce un commit atómico
- Si algo sale mal, se puede volver atrás a la fase anterior
- Claude mantiene el contexto fresco al inicio de cada fase (lee CLAUDE.md)
- Permite revisar el código de cada fase antes de avanzar

---

## 5. Momentos de corrección y redirección

### Aclaración sobre arquitectura
El momento más significativo fue cuando el desarrollador describió su arquitectura de
trabajo como "hexagonal" y Claude identificó que era layered bien estructurada. Esta
aclaración honesta llevó a una decisión informada de usar hexagonal real, con documentación
didáctica incluida para facilitar el aprendizaje.

### Spring Boot 4 vs 3
Se investigó activamente si Spring Boot 4.0.3 era viable. Se confirmó su disponibilidad
(lanzado Feb 2026) y se documentaron las diferencias de API que podrían causar problemas
(Jackson 3, starters renombrados, Hibernate 7.1).

---

## 6. Reflexiones: ventajas y limitaciones

### Ventajas observadas
- **Planificación eficiente**: El modo plan permitió debatir arquitectura antes de escribir
  código, ahorrando rehacer trabajo
- **Consistencia**: Con CLAUDE.md y Skills, Claude mantiene un comportamiento predecible
  a lo largo de múltiples sesiones
- **Documentación como subproducto**: El propio proceso de trabajar con Claude genera
  documentación (CLAUDE.md, Skills, este documento)
- **Sparring técnico**: Claude cuestionó supuestos (como la arquitectura) de forma
  constructiva

### Limitaciones observadas
- **Requiere criterio del desarrollador**: Claude sigue instrucciones, pero el
  desarrollador debe saber qué instrucciones dar. Si le pides algo incorrecto, lo
  hará correctamente incorrecto.
- **Tecnologías muy recientes**: Con Spring Boot 4.0.3 (Feb 2026), el conocimiento
  de Claude puede no estar completamente actualizado sobre APIs nuevas. Se necesita
  verificar contra la documentación oficial.
- **No sustituye la comprensión**: Usar Claude para implementar hexagonal no equivale a
  entender hexagonal. La guía didáctica y las comparativas con layered buscan cerrar esa brecha.

---

## 7. Herramientas y configuración utilizada

- **Claude Code**: CLI de Anthropic (claude-opus-4-6)
- **IDE**: VS Code con extensión de Claude Code
- **Modo plan**: Activado con Shift+Tab para diseño de arquitectura
- **CLAUDE.md**: Configuración de proyecto en la raíz
- **Skills**: 2 skills en `.claude/skills/` (test-validator, code-review)
- **Modelo**: Claude Opus 4.6 (claude-opus-4-6)

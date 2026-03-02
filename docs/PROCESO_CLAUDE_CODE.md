# Proceso de Desarrollo con Claude Code

Este documento explica cómo se integró Claude Code en el desarrollo de esta prueba técnica.

---

## 1. Fase de Planificación (antes de escribir código)

### Claude como sparring de arquitectura

Antes de tocar una sola línea de código, se usó Claude Code en **modo plan** (Opus 4.6)
para debatir las decisiones de diseño. El flujo fue:

1. **Lectura del contexto**: Se proporcionó a Claude el enunciado de la prueba técnica.

2. **Decisiones de diseño debatidas**:
   - ¿Base de datos? → H2 (siguiendo el enunciado, sin Docker)
   - ¿Build tool? → Maven
   - ¿Arquitectura? → Hexagonal (puertos y adaptadores). La lógica de resolución de prioridades
     se delegó a la query SQL (`ORDER BY priority DESC LIMIT 1`) en lugar de
     gestionarla en Java, aprovechando que la BD está optimizada para ello.

---

## 2. CLAUDE.md

Fichero en la raíz del proyecto que Claude lee al inicio de cada sesión.

### Contenido clave

- Contexto del proyecto (prueba técnica, qué se valora)
- Peculiaridades de Spring Boot 4 (Jackson 3, starters renombrados)
- Reglas de la arquitectura hexagonal y convenciones de naming con sufijos explícitos
- Sección "DO NOT" para evitar sobreingeniería (sin Lombok, sin Swagger, sin Request DTO)

---

## 3. Skills: automatización de tareas repetitivas

Los skills son ficheros SKILL.md que Claude carga bajo demanda cuando detecta que
son relevantes para la tarea en curso. Se crearon 2 para este proyecto:

1. **test-validator**: Contiene la especificación de los 5 tests con los valores esperados.
   Permite a Claude verificar que los tests cubren exactamente los casos del enunciado.

2. **code-review**: Checklist que verifica las fronteras de la arquitectura hexagonal
   (que el dominio no tenga imports de Spring) y las convenciones de código.

---

## 4. Desarrollo por fases

El desarrollo se estructuró en fases independientes, cada una como un prompt separado:

| Fase | Descripción |
|------|-------------|
| 0 | Scaffolding (git, Maven, H2, estructura de paquetes) |
| 1 | CLAUDE.md |
| 2 | Skills |
| 3.1 | Capa de dominio |
| 3.2 | Adaptador de persistencia |
| 3.3 | Servicio de aplicación |
| 3.4 | Adaptador REST + manejo de errores |
| 3.5 | Tests de integración |
| 3.6 | Pulido y documentación |

---

## 5. Herramientas y configuración utilizada

- **Modo plan** (Opus 4.6): diseño de arquitectura y planificación de fases
- **Modo agente**: desarrollo de cada fase de implementación
- **CLAUDE.md**: configuración persistente del proyecto leída al inicio de cada sesión
- **Skills**: `test-validator` y `code-review` en `.claude/skills/`
- Cada fase arrancó con contexto limpio, garantizando que Claude leyera el CLAUDE.md
  actualizado sin ruido de sesiones anteriores

# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.0] - 2025-09-07
### Added
- **feat:** Nuevo endpoint REST para generación de planilla de detalle por sede (`/planilla/detalle/facultad/{facultadId}/lectivo/{lectivoId}/geografica/{geograficaId}`).
- **feat:** Cliente Feign `GeograficaClient` y DTOs asociados (`GeograficaDto`, `FacultadDto`, etc.).
- **feat:** Nuevos DTOs para soporte de reportes avanzados (`ClaseChequeraDto`, `DomicilioDto`, `LectivoDto`, `PersonaDto`, `TipoChequeraDto`, y core DTOs).
- **feat:** Utilidad `Tool` para generación de archivos y mejoras en utilidades de serialización JSON (`Jsonifier`).

### Changed
- **refactor:** Lógica de generación de reportes en `ChequerasService` ahora soporta sede (`geograficaId`) y maneja mejor los datos nulos.
- **refactor:** Mejoras en logging y trazabilidad de generación de reportes.
- **chore:** Configuración avanzada de timeouts para Feign en `bootstrap.yml`.

### Fixed
- **fix:** Validaciones adicionales para evitar errores con datos nulos en reportes.

### Docs
- **docs:** Actualización de diagramas y documentación automática para reflejar nuevos endpoints y estructura de DTOs.

## [0.3.0] - 2025-09-05
### Added
- **feat:** Añadido método `jsonify()` y utilitario `Jsonifier` para serialización JSON en DTOs.
- **feat:** Nuevo endpoint de test en `ChequeraSerieClient` (`/lectivo/test/{facultadId}/{lectivoId}`).
- **feat:** Logging detallado de entidades y pagos en generación de reportes.

### Changed
- **refactor:** Simplificación y mejora de la lógica de generación de reportes en `ChequerasService` (uso de streams, agrupamiento y logging).
- **refactor:** Eliminación de dependencias y parámetros no utilizados en servicios y DTOs.
- **chore:** Actualización de dependencias:
  - Spring Boot: `3.5.4` → `3.5.5`
  - SpringDoc OpenAPI: `2.8.9` → `2.8.10`

### Fixed
- **fix:** Corrección de estilos y formato en generación de celdas de fechas en reportes Excel.

### Docs
- **docs:** Revisión de diagramas y documentación automática para reflejar la nueva estructura y utilidades.

## [0.2.0] - 2025-08-26
### Added
- **feat:** Se añade la funcionalidad para generar una planilla de pagos, incluyendo un nuevo endpoint y los clientes Feign necesarios (`ChequeraPagoClient`, `FacultadClient`, `TipoChequeraClient`).
### Changed
- **refactor:** Se estandariza la configuración de todos los clientes Feign para mejorar la legibilidad y prevenir conflictos.
- **chore:** Se actualiza la dependencia de Spring Boot a la versión `3.5.4`.
### Fixed
- **fix:** Se corrigen los permisos de usuario en el `Dockerfile` para el directorio de la aplicación.
- **fix:** Se soluciona un problema en la generación de reportes que mostraba períodos sin datos.
- **fix:** Se corrige la versión de SpringDoc OpenAPI y se añade una entrada faltante en el changelog.
### Docs
- **docs:** Se actualiza la documentación y las versiones de las dependencias en el `README.md`.
- **docs:** Se actualiza el branding y se eliminan diagramas obsoletos en la documentación automática.

---

## [0.1.0] - 2025-08-18
### Added
- **feat:** Se añade la funcionalidad para generar una planilla de pagos, incluyendo un nuevo endpoint y los clientes Feign necesarios (`ChequeraPagoClient`, `FacultadClient`, `TipoChequeraClient`).
### Changed
- **refactor:** Se estandariza la configuración de todos los clientes Feign para mejorar la legibilidad y prevenir conflictos.
- **chore:** Se actualiza la dependencia de Spring Boot a la versión `3.5.4`.
### Fixed
- **fix:** Se corrigen los permisos de usuario en el `Dockerfile` para el directorio de la aplicación.

---

## [00.1-SNAPSHOT] - 2025-05-31

### Changed
- Actualización de documentación y versiones de dependencias
  - Actualización de README.md con versiones actuales de dependencias
  - Eliminación de referencias a tecnologías no utilizadas (OpenPDF, ZXing)
  - Actualización de CHANGELOG.md para reflejar cambios recientes
  - Actualización de pom.xml con versiones verificadas de dependencias
  - Actualización de TipoChequeraDto.java

## [0.0.1-SNAPSHOT] - 2025-04-21

### Added
- Integración con Spring Boot 3.5.0
- Sistema de caché con Caffeine
- Integración con Eureka para registro de servicios
- Documentación con SpringDoc OpenAPI 2.8.9
- Configuración de correo electrónico
- Soporte para múltiples formatos de reportes
- Generación de reportes Excel con Apache POI 5.4.1
- Migración de DTOs a Java con Lombok

### Changed
- Refactorización de endpoints y manejo de planes en reportes de chequeras
  - Eliminación del parámetro geograficaId del endpoint /planilla/detalle
  - Actualización de ChequeraSerieClient para coincidir con cambios en endpoints
  - Mejora en el manejo de planes nulos en generación de reportes
  - Mejora en el formato de carrera para manejar planes nulos
- Migración de DTOs de Kotlin a Java
  - Migración completa de DTOs de Kotlin a Java
  - Adición de anotaciones Lombok para reducir código boilerplate
  - Adición de @JsonFormat para manejo adecuado de fechas
  - Actualización de la configuración de logging
  - Eliminación de dependencia de Kotlin

### Removed
- Eliminación de archivos DTOs en Kotlin
- Eliminación de dependencia de Kotlin
- Eliminación del parámetro geograficaId del endpoint /planilla/detalle

### Fixed
- Manejo de planes nulos en generación de reportes
- Formato de carrera para planes nulos

### Security
- N/A

## [0.0.1-SNAPSHOT] - 2025-03-29

### Added
- Estructura inicial del proyecto
- Configuración básica de Spring Boot
- Configuración de Maven
- Configuración de Docker
- Configuración de GitHub Actions
- Documentación inicial
- Configuración de Jekyll para la documentación
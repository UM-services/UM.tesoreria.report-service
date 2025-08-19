# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

<<<<<<< Updated upstream
## [0.1.0] - 2025-08-18
### Added
- **feat:** Se añade la funcionalidad para generar una planilla de pagos, incluyendo un nuevo endpoint y los clientes Feign necesarios (`ChequeraPagoClient`, `FacultadClient`, `TipoChequeraClient`).
### Changed
- **refactor:** Se estandariza la configuración de todos los clientes Feign para mejorar la legibilidad y prevenir conflictos.
- **chore:** Se actualiza la dependencia de Spring Boot a la versión `3.5.4`.
### Fixed
- **fix:** Se corrigen los permisos de usuario en el `Dockerfile` para el directorio de la aplicación.

---
=======
## [1.0.0] - 2025-08-19

### Added
### Changed
- Refactorización de la lógica de generación de reportes para soportar múltiples productos y periodos.
- Mejor manejo de planes y carreras nulas en la generación de reportes.


---

## [0.0.1-SNAPSHOT] - 2025-05-31

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
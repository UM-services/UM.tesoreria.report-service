# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
y este proyecto adhiere a [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.11.0] - 2026-07-03

### Added
- **feat:** Nuevo módulo contable con arquitectura hexagonal para generar planilla de "Lectivo Total Imputación"
  - Nuevos modelos de dominio: `Cuenta`, `LectivoTotalImputacion`
  - Nuevo puerto de entrada: `GenerateLectivoTotalImputacionReportUseCase`
  - Nuevo puerto de salida: `LectivoTotalImputacionRepository`
  - Nueva capa `application` con servicio fachada (`ContableReportService`) e implementación de caso de uso
  - Nueva capa `infrastructure` con cliente Feign, adaptador, DTOs (`CuentaDto`, `LectivoTotalImputacionDto`), mapper (`ContableMapper`) y controlador REST
  - Nuevo endpoint: `GET /api/tesoreria/report/contable/planilla/lectivo/{lectivoId}`
  - Reporte Excel generado con columnas: Facultad, Tipo Chequera, Geográfica, Producto, Cuenta Contable, Nombre Cuenta

### Changed
- **chore:** Ampliado escaneo de `@EnableFeignClients` en `ReportConfiguration` para incluir todos los paquetes hexagonales (`um.tesoreria.report.hexagonal`)

## [0.10.1] - 2026-07-02

### Changed
- **chore:** Añadido log de depuración en `GeneratePlanillaDetalleVerticalUseCaseImpl` para trazabilidad en lectura de pagos

## [0.10.0] - 2026-06-27

### Added
- **feat:** Nueva planilla de detalle vertical (`generatePlanillaDetalleVertical`)
  - Nuevo `GeneratePlanillaDetalleVerticalUseCase` y su implementación para generar planillas Excel con diseño vertical
  - Nuevo endpoint `GET /planilla/detalle/vertical/facultad/{facultadId}/lectivo/{lectivoId}/geografica/{geograficaId}`
  - Incluye las mismas columnas que la planilla de detalle horizontal: Chequera, DU, Apellido/Nombre, Facultad, Sede, Carrera, Curso, Tipo Chequera, Tipo Arancel, HPUM, Beca, Alternativa, Producto, Cuota, Año, Mes, Importe, Vencimiento, Pago, Medio, Pagado, id MP
  - Optimización de E/S paralela con Virtual Threads y Semaphore (límite 20 concurrencia)
- **feat:** Método `jsonify()` añadido a `ChequeraCuotaPagos` para serialización JSON en debugging

### Changed
- **refactor:** `ChequerasReportService` actualizado para delegar en `GeneratePlanillaDetalleVerticalUseCase`
- **chore:** Banner de aplicación limpiado y simplificado
- **chore:** Actualización de versión a `0.10.0`

## [0.9.0] - 2026-06-27

### Added
- **feat:** Migración a arquitectura hexagonal para el módulo de chequeras
  - Nueva capa `domain` con modelo (`ArancelTipo`, `Carrera`, `ChequeraCuota`, `ChequeraCuotaPagos`, `ChequeraPago`, `ChequeraSerie`, `ClaseChequera`, `CuotaPeriodo`, `Domicilio`, `Facultad`, `Geografica`, `Lectivo`, `Legajo`, `Persona`, `Plan`, `Producto`, `TipoChequera`, `TipoPago`)
  - Nuevos puertos de dominio: 2 interfaces de use case (`GeneratePlanillaDetalleUseCase`, `GeneratePlanillaPagosUseCase`) y 7 repositorios
  - Nueva capa `application` con servicio fachada (`ChequerasReportService`) e implementaciones de use cases
  - Nueva capa `infrastructure` con 7 adaptadores Feign, mapper (`ChequerasMapper`) y controlador REST

### Changed
- **refactor:** Migración de `ChequerasController` → `ChequerasReportController` en paquete hexagonal
- **refactor:** Migración de `ChequerasService` → `ChequerasReportService` + use cases desacoplados
- **refactor:** Migración de todos los DTOs a `hexagonal.chequeras.infrastructure.client.dto`
- **refactor:** Migración de todos los clientes Feign a `hexagonal.chequeras.infrastructure.client`
- **refactor:** ReportConfiguration actualizado para escanear nuevo paquete de clients
- **refactor:** PingController simplificado usando `@RequiredArgsConstructor` de Lombok
- **refactor:** Eliminación de exlusión log4j2 de spring-boot-starter
- **chore:** Actualización de SpringDoc OpenAPI de 3.0.2 a 3.0.3
- **chore:** Eliminación de versión explícita de Lombok en annotation processor paths

## [0.8.1] - 2026-06-27

### Fixed
- **fix:** Corregido formato de zona horaria en serialización JSON de fechas (`Z` → `XX`) en 8 DTOs (`ChequeraCuotaDto`, `ChequeraSerieDto`, `DomicilioDto`, `LectivoDto`, `ChequeraCuotaPagosDto`, `ChequeraPagoDto`, `LegajoDto`, `PlanDto`) para compatibilidad con ISO 8601

### Changed
- **perf:** Optimización de E/S paralela en `ChequerasService.generatePlanillaDetalle()` usando Virtual Threads con Semaphore (límite 20 concurrencia) para mejorar rendimiento en consultas a core-service
- **perf:** Cache de `CellStyle` de fechas para evitar creación redundante por cada celda en ambas planillas
- **perf:** `autoSizeColumn` limitado a las primeras 10 columnas; ancho fijo (~15-16 caracteres) para columnas repetitivas de períodos
- **chore:** Actualización de Spring Boot de 4.0.7 a 4.1.0
- **chore:** Actualización de Spring Cloud de 2025.1.0 a 2025.1.2
- **refactor:** Logs condicionales con `isDebugEnabled()` y uso de `log.error` en lugar de `log.debug` para errores de escritura

## [0.8.0] - 2026-06-11

### Added
- **feat:** Agregadas columnas "HPUM" y "Beca" en la planilla de detalle de chequeras (`generatePlanillaDetalle`)
  - Nueva columna "HPUM": marca con "X" si la chequera tiene habilitado HPUM (campo `hpum` en `ChequeraSerieDto`)
  - Nueva columna "Beca": muestra el porcentaje de beca asociado (`becaPorcentaje` en `ChequeraSerieDto`)
  - Nuevos campos en DTO: `becaResolucion`, `becaFecha`, `becaUserId` para soporte completo de datos de beca
- **chore:** Nueva dependencia `commons-fileupload:1.6.0` para manejo de subida de archivos

### Changed
- **refactor:** Extracción del método `jsonify()` en `ChequeraSerieDto` a la utilidad `Jsonifier.builder(this).build()`
- **chore:** Actualización de Spring Boot de 4.0.5 a 4.0.7
- **chore:** Actualización de GitHub Actions:
  - `actions/checkout@v4` → `v6`
  - `actions/setup-java@v4` → `v5`
  - `actions/cache@v4` → `v5`
  - `actions/upload-pages-artifact@v3` → `v4`
  - `actions/deploy-pages@v4` → `v5`
  - `docker/login-action@v3` → `v4`
  - `docker/metadata-action@v5` → `v6`
  - `docker/setup-buildx-action@v3` → `v4`
  - `docker/build-push-action@v6` → `v7`

### Docs
- **docs:** Actualización de diagramas Mermaid para reflejar nuevas columnas HPUM y Beca
- **docs:** Corrección en pipeline de documentación para incluir diagramas Mermaid en la página generada

## [0.7.0] - 2026-03-22

### Added
- **feat:** Agregada columna "id MP" (ID de Mercado Pago) en la planilla de detalle de chequeras (`generatePlanillaDetalle`)
  - Nueva columna muestra el identificador de Mercado Pago para cada pago
  - Los datos se obtienen del campo `idMercadoPago` en `ChequeraPagoDto`

### Changed
- **refactor:** Uso de `@RequiredArgsConstructor` de Lombok en `ChequerasController` y `ChequerasService`
- **refactor:** Eliminación de constructores explícitos, utilizando generación automática de Lombok

## [0.6.0] - 2026-02-16

### Added
- **feat:** Agregadas columnas de email en la planilla de pagos (`generatePlanillaPagos`)
  - Nueva columna: "e-mail Institucional" - extrae el email institucional del domicilio asociado a la chequera
  - Nueva columna: "e-mail Personal" - extrae el email personal del domicilio asociado a la chequera
  - Los datos se obtienen a través de la relación `chequeraPago.chequeraCuota.chequeraSerie.domicilio`

## [0.5.0] - 2026-02-03
### Changed
- **chore:** Actualización de Spring Boot de 3.5.8 a 4.0.2
- **chore:** Actualización de Java de 24 a 25
- **chore:** Actualización de Spring Cloud de 2025.0.0 a 2025.1.0
- **chore:** Actualización de SpringDoc OpenAPI de 2.8.10 a 3.0.1
- **chore:** Actualización de Apache POI de 5.4.1 a 5.5.1
- **chore:** Actualización de ModelMapper de 3.2.4 a 3.2.6
- **chore:** Actualización de Apache Commons Lang3 de 3.18.0 a 3.20.0
- **chore:** Actualización de GitHub Actions workflow para usar JDK 25
- **chore:** Actualización de Dockerfile para usar JDK/JRE 25

### Removed
- **chore:** Eliminación de configuración executable del Spring Boot Maven Plugin

## [0.4.1] - 2025-10-22
### Changed
- **chore:** Actualización de Spring Boot de 3.5.5 a 3.5.6.

### Fixed
- **fix:** Mejora en la lógica de generación de reportes en `ChequerasService` para manejar cuotas dadas de baja, mostrando "Baja" en lugar del importe cuando corresponde.

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
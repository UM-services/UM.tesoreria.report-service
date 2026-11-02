
# UM.tesoreria.report-service

[![Java](https://img.shields.io/badge/Java-25-blue)](https://www.java.com/)
[![Version](https://img.shields.io/badge/Version-0.15.0-blue)](https://github.com/UM-services/UM.tesoreria.report-service)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.4-brightgreen)](https://spring.io/projects/spring-cloud)
[![SpringDoc OpenAPI](https://img.shields.io/badge/SpringDoc%20OpenAPI-3.1.1-blue)](https://springdoc.org/)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-6.0.0-red)](https://poi.apache.org/)
[![OpenPDF](https://img.shields.io/badge/OpenPDF-3.0.5-red)](https://github.com/LibrePDF/OpenPDF)
[![Lombok](https://img.shields.io/badge/Lombok-1.18.30-pink)](https://projectlombok.org/)

## Estado del Proyecto

[![UM.tesoreria.report-service CI](https://github.com/UM-services/UM.tesoreria.report-service/actions/workflows/maven.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.report-service/actions/workflows/maven.yml)

Servicio de generación de reportes y documentos para UM Tesorería. Este microservicio se encarga de la generación de reportes Excel y PDF, integración con servicios core de Tesorería y la gestión de documentos relacionados.


## Características Principales

- Generación de reportes Excel de chequeras y planillas (incluye columnas HPUM, Beca e ID Mercado Pago)
- Generación de planillas de detalle por sede (geográfica): horizontal y vertical
- Planillas de pagos con información de contacto (emails institucional y personal)
- Generación de planilla de "Lectivo Total Imputación" desde el módulo contable
- Generación de PDF "Estado de Chequera" (dos hojas: cuotas por producto con subtotales y débitos automáticos agrupados por tipo)
- Generación del PDF independiente "Materias inscriptas", con bloques por fuente, estados y tablas por curso y período
- Integración con servicios core de Tesorería (Chequera, Legajo, Lectivo, Geográfica, Contable)
- Endpoints REST para descarga de reportes
- Configuración avanzada con Spring Cloud y Consul
- Documentación automática con SpringDoc OpenAPI
- Arquitectura hexagonal (puertos y adaptadores) para los módulos de chequeras y contable
- Diagramas de arquitectura y flujo de reportes
- Utilidades de serialización JSON para debugging y pruebas


## Tecnologías

- Java 25
- Spring Boot 4.1.2
- Spring Cloud 2025.1.4
- Caffeine (para caché)
- SpringDoc OpenAPI 3.1.1
- Apache POI 6.0.0 (para reportes Excel)
- OpenPDF 3.0.5 (para reportes PDF)
- Lombok (para reducir código boilerplate)

## Documentación

- [Documentación del Proyecto](https://um-services.github.io/UM.tesoreria.report-service/)
- [Wiki del Proyecto](https://github.com/UM-services/UM.tesoreria.report-service/wiki)
- [Documentación de API](https://um-services.github.io/UM.tesoreria.report-service/api-documentation)
- [Guía de Desarrollo](https://um-services.github.io/UM.tesoreria.report-service/development-guide)
- [Manual de Despliegue](https://um-services.github.io/UM.tesoreria.report-service/deployment-guide)
- [Diagrama de Materias inscriptas](docs/diagrams/materias-inscriptas.mmd)

## Desarrollo

### Requisitos Previos

- JDK 25
- Maven 3.8.8 o superior
- Git
- Docker (opcional, para desarrollo con contenedores)

### Configuración Local

```bash
git clone https://github.com/UM-services/UM.tesoreria.report-service.git
cd UM.tesoreria.report-service
mvn clean install
```

### Ejecución

```bash
mvn spring-boot:run
```

El servicio estará disponible en `http://localhost:8080/swagger-ui.html` donde podrás consultar la documentación de la API

### Desarrollo con Docker

```bash
# Construir la imagen
docker build -t um-tesoreria-report-service -f Dockerfile.local .

# Ejecutar el contenedor
docker run -p 8080:8080 um-tesoreria-report-service
```

## Contribución

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

### PDF Materias inscriptas

`GET /api/tesoreria/report/chequeras/materias/facultad/{facultadId}/tipoChequera/{tipoChequeraId}/chequeraSerie/{chequeraSerieId}/alternativa/{alternativaId}`

descarga `materias-inscriptas.pdf`. Consulta dos recursos del core: `chequera/estado/...`
para el encabezado y `materiasInscriptas/{facultadId}/{tipoChequeraId}/{chequeraSerieId}`
para los bloques académicos. La alternativa solo se usa en la consulta del encabezado.
No consulta Guaraní directamente. Conserva el orden de fuentes, cursos, períodos y materias del core.

Cada bloque muestra su fuente. Los estados sin inscripción y no disponible tienen su leyenda.
Una lista vacía muestra que la chequera no es de grado. Los estados desconocidos se muestran como
no disponible y los períodos desconocidos como sin período. Las fuentes desconocidas conservan su nombre.
Las tablas repiten las columnas al continuar en otra página. El encabezado institucional aparece una sola vez.
El PDF Estado de Chequera conserva su generador y su contenido.

Los archivos se generan en `path.reports`, en un directorio existente y con permiso de escritura.
Cada solicitud crea un archivo distinto. Si falla el generador, se elimina el archivo parcial.
El temporal completo se elimina cuando se cierra el stream de descarga, incluso si se interrumpe la transferencia.
El botón de descarga del frontend se implementa en frontend-client, fuera de este cambio.

Las pruebas automáticas usan datos ficticios. `MateriasInscriptasHttpContractTest` usa un core HTTP
simulado y overrides de URL locales; no es un E2E. Las pruebas reales se documentan aparte y no se
incluyen en la suite. No guardar respuestas ni PDFs con datos personales en el repositorio.

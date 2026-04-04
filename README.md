
# UM.tesoreria.report-service

[![Java](https://img.shields.io/badge/Java-25-blue)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.1.0-brightgreen)](https://spring.io/projects/spring-cloud)
[![SpringDoc OpenAPI](https://img.shields.io/badge/SpringDoc%20OpenAPI-3.0.2-blue)](https://springdoc.org/)
[![Apache POI](https://img.shields.io/badge/Apache%20POI-5.5.1-red)](https://poi.apache.org/)
[![Lombok](https://img.shields.io/badge/Lombok-1.18.30-pink)](https://projectlombok.org/)

## Estado del Proyecto

[![UM.tesoreria.report-service CI](https://github.com/UM-services/UM.tesoreria.report-service/actions/workflows/maven.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.report-service/actions/workflows/maven.yml)

Servicio de generación de reportes y documentos para UM Tesorería. Este microservicio se encarga de la generación de reportes Excel, integración con servicios core de Tesorería y la gestión de documentos relacionados.


## Características Principales

- Generación de reportes Excel de chequeras y planillas
- **NUEVO:** Generación de planillas de detalle por sede (geográfica)
- **NUEVO:** Planillas de pagos con información de contacto (emails institucional y personal)
- Integración con servicios core de Tesorería (Chequera, Legajo, Lectivo, Geográfica)
- Endpoints REST para descarga de reportes
- Configuración avanzada con Spring Cloud y Consul
- Documentación automática con SpringDoc OpenAPI
- Diagramas de arquitectura y flujo de reportes
- Utilidades de serialización JSON para debugging y pruebas


## Tecnologías

- Java 25
- Spring Boot 4.0.5
- Spring Cloud 2025.1.0
- Caffeine (para caché)
- SpringDoc OpenAPI 3.0.2
- Apache POI 5.5.1 (para reportes Excel)
- Lombok (para reducir código boilerplate)

## Documentación

- [Documentación del Proyecto](https://um-services.github.io/UM.tesoreria.report-service/)
- [Wiki del Proyecto](https://github.com/UM-services/UM.tesoreria.report-service/wiki)
- [Documentación de API](https://um-services.github.io/UM.tesoreria.report-service/api-documentation)
- [Guía de Desarrollo](https://um-services.github.io/UM.tesoreria.report-service/development-guide)
- [Manual de Despliegue](https://um-services.github.io/UM.tesoreria.report-service/deployment-guide)

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

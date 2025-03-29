# UM.tesoreria.report-service

## Estado del Proyecto

[![UM.tesoreria.report-service CI](https://github.com/UM-services/UM.tesoreria.report-service/actions/workflows/maven.yml/badge.svg)](https://github.com/UM-services/UM.tesoreria.report-service/actions/workflows/maven.yml)

Servicio de generación de reportes y documentos para UM Tesorería. Este microservicio se encarga de la generación de PDFs, facturas electrónicas y otros documentos necesarios para la gestión de tesorería.

## Características Principales

- Generación de documentos PDF utilizando OpenPDF
- Generación de facturas electrónicas
- Códigos QR para verificación de documentos
- Envío automático de documentos por correo electrónico
- Sistema de caché para optimizar el rendimiento
- Integración con otros servicios de tesorería
- Soporte para múltiples formatos de reportes
- Validación de documentos mediante códigos QR

## Tecnologías

- Java 21
- Spring Boot 3.4.4
- Kotlin 2.1.20
- OpenPDF 2.0.3
- ZXing (para códigos QR)
- Spring Cloud 2024.0.1
- Caffeine (para caché)
- SpringDoc OpenAPI 2.8.6

## Documentación

- [Documentación del Proyecto](https://um-services.github.io/UM.tesoreria.report-service/)
- [Wiki del Proyecto](https://github.com/UM-services/UM.tesoreria.report-service/wiki)
- [Documentación de API](https://um-services.github.io/UM.tesoreria.report-service/api-documentation)
- [Guía de Desarrollo](https://um-services.github.io/UM.tesoreria.report-service/development-guide)
- [Manual de Despliegue](https://um-services.github.io/UM.tesoreria.report-service/deployment-guide)

## Desarrollo

### Requisitos Previos

- JDK 21
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

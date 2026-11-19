package um.tesoreria.report.hexagonal.contable.infrastructure.web.controller;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * El core decide si el pedido es válido. Cuando lo rechaza (4xx con cuerpo {@code {status, error, codigo, message}})
 * el report devuelve ese mismo estado y cuerpo, sin generar archivo. Si el core no responde o falla (5xx, sin
 * conexión) devuelve 502 con un cuerpo del mismo formato.
 */
@RestControllerAdvice(assignableTypes = MayorReportController.class)
public class MayorReportExceptionAdvice {

    static final String CUERPO_NO_DISPONIBLE = "{\"status\":502,\"error\":\"Bad Gateway\","
            + "\"codigo\":\"CORE_NO_DISPONIBLE\",\"message\":\"No se pudo consultar el mayor en el core\"}";

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> core(FeignException ex) {
        String cuerpo = ex.contentUTF8();
        if (ex.status() >= 400 && ex.status() < 500 && cuerpo != null && !cuerpo.isBlank()) {
            return ResponseEntity.status(ex.status()).contentType(MediaType.APPLICATION_JSON).body(cuerpo);
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).contentType(MediaType.APPLICATION_JSON)
                .body(CUERPO_NO_DISPONIBLE);
    }
}
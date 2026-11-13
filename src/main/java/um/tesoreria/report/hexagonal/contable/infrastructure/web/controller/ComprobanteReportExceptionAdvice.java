package um.tesoreria.report.hexagonal.contable.infrastructure.web.controller;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce lo que responde el core al pedido de impresión de un comprobante (orden de pago, factura o nota de crédito):
 * 404 si el comprobante no existe, 409 si el core lo rechaza por no ser imprimible con ese formato y 502 si el core no responde o falla.
 * Todos con cuerpo {@code {status, error, codigo, message}}.
 */
@RestControllerAdvice(assignableTypes = ComprobanteReportController.class)
public class ComprobanteReportExceptionAdvice {

    static final String NO_EXISTE = cuerpo(404, "Not Found", "COMPROBANTE_INEXISTENTE",
            "El comprobante no existe");
    static final String NO_SOPORTADO = cuerpo(409, "Conflict", "COMPROBANTE_NO_SOPORTADO",
            "El comprobante no se puede imprimir");
    static final String NO_DISPONIBLE = cuerpo(502, "Bad Gateway", "CORE_NO_DISPONIBLE",
            "No se pudo consultar el comprobante en el core");

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> core(FeignException ex) {
        return switch (ex.status()) {
            case 404 -> respuesta(HttpStatus.NOT_FOUND, NO_EXISTE);
            case 409 -> respuesta(HttpStatus.CONFLICT, NO_SOPORTADO);
            default -> respuesta(HttpStatus.BAD_GATEWAY, NO_DISPONIBLE);
        };
    }

    private static ResponseEntity<String> respuesta(HttpStatus status, String cuerpo) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(cuerpo);
    }

    private static String cuerpo(int status, String error, String codigo, String message) {
        return "{\"status\":" + status + ",\"error\":\"" + error + "\",\"codigo\":\"" + codigo
                + "\",\"message\":\"" + message + "\"}";
    }
}
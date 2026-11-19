package um.tesoreria.report.hexagonal.contable.infrastructure.web.controller;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ComprobanteReportExceptionAdviceTest {

    private final ComprobanteReportExceptionAdvice advice = new ComprobanteReportExceptionAdvice();

    private static Request request() {
        return Request.create(Request.HttpMethod.GET, "/x", Map.of(), null, StandardCharsets.UTF_8, null);
    }

    // protects=un comprobante inexistente se informa 404 con su código; fails_when=se devuelve otro estado o un cuerpo sin codigo; no_claim=no prueba el texto del mensaje
    @Test
    void core_notFoundBecomes404() {
        ResponseEntity<String> respuesta = advice.core(new FeignException.NotFound("x", request(), null, Map.of()));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).contains("\"codigo\":\"COMPROBANTE_INEXISTENTE\"");
    }

    // protects=un comprobante que el core rechaza con 409 se informa 409 COMPROBANTE_NO_SOPORTADO; fails_when=se confunde con un fallo del core o se genera un archivo; no_claim=no prueba qué comprobantes rechaza el core
    @Test
    void core_conflictBecomesUnsupportedVoucher() {
        ResponseEntity<String> respuesta = advice.core(new FeignException.Conflict("x", request(), null, Map.of()));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(respuesta.getBody()).contains("\"codigo\":\"COMPROBANTE_NO_SOPORTADO\"");
    }

    // protects=si el core falla o no responde se informa 502 CORE_NO_DISPONIBLE; fails_when=un 500 del core se devuelve como 500 propio o como otro código; no_claim=no prueba la caída de red (sin respuesta)
    @Test
    void core_serverErrorBecomesBadGateway() {
        ResponseEntity<String> respuesta = advice.core(
                new FeignException.InternalServerError("x", request(), null, Map.of()));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(respuesta.getBody()).contains("\"codigo\":\"CORE_NO_DISPONIBLE\"");
    }
}
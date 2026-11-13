package um.tesoreria.report.hexagonal.contable.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** De {@code GET /proveedorMovimiento/{id}/detalle} sólo interesa {@code tipo}: FACTURA u ORDEN_PAGO. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComprobanteTipoDto {

    private String tipo;
}
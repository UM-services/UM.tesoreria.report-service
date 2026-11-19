package um.tesoreria.report.hexagonal.contable.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.CompraImpresionDto;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.ComprobanteTipoDto;

@FeignClient(name = "tesoreria-core-service", contextId = "compraClient", path = "/api/tesoreria/core/proveedorMovimiento")
public interface CompraClient {

    @GetMapping("/{proveedorMovimientoId}/impresion-compra")
    CompraImpresionDto impresion(@PathVariable("proveedorMovimientoId") Long proveedorMovimientoId);

    /** Sólo se lee {@code tipo} de la respuesta de {@code /detalle}. */
    @GetMapping("/{proveedorMovimientoId}/detalle")
    ComprobanteTipoDto detalle(@PathVariable("proveedorMovimientoId") Long proveedorMovimientoId);
}
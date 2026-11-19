package um.tesoreria.report.hexagonal.contable.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.contable.infrastructure.client.dto.OrdenPagoImpresionDto;

@FeignClient(name = "tesoreria-core-service", contextId = "ordenPagoClient", path = "/api/tesoreria/core/proveedorMovimiento")
public interface OrdenPagoClient {

    @GetMapping("/{proveedorMovimientoId}/impresion")
    OrdenPagoImpresionDto impresion(@PathVariable("proveedorMovimientoId") Long proveedorMovimientoId);
}
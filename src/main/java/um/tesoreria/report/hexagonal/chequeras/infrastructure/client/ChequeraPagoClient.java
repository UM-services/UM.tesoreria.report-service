package um.tesoreria.report.hexagonal.chequeras.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.ChequeraPagoDto;
import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "chequeraPagoClient", path = "/api/tesoreria/core/chequeraPago")
public interface ChequeraPagoClient {

    @GetMapping("/pagos/facultad/{facultadId}/tipoChequera/{tipoChequeraId}/lectivo/{lectivoId}")
    List<ChequeraPagoDto> findAllByFacultadIdAndTipoChequeraIdAndLectivoId(@PathVariable Integer facultadId,
                                                                           @PathVariable Integer tipoChequeraId,
                                                                           @PathVariable Integer lectivoId);

}

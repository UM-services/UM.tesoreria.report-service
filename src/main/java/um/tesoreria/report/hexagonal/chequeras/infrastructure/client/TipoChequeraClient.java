package um.tesoreria.report.hexagonal.chequeras.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.TipoChequeraDto;

@FeignClient(name = "tesoreria-core-service", contextId = "tipoChequeraClient", path = "/api/tesoreria/core/tipoChequera")
public interface TipoChequeraClient {

    @GetMapping("/{tipoChequeraId}")
    TipoChequeraDto findByTipoChequeraId(@PathVariable Integer tipoChequeraId);

}

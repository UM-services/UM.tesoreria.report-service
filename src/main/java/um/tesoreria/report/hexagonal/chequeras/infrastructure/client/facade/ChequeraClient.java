package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.facade;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.ChequeraCuotaPagosDto;
import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/chequera")
public interface ChequeraClient {

    @GetMapping("/cuotas/pagos/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<ChequeraCuotaPagosDto> findAllCuotaPagosByChequera(@PathVariable Integer facultadId,
                                                            @PathVariable Integer tipoChequeraId,
                                                            @PathVariable Long chequeraSerieId,
                                                            @PathVariable Integer alternativaId);
}

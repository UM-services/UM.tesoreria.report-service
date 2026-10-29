package um.tesoreria.report.hexagonal.recibos.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import um.tesoreria.report.hexagonal.recibos.infrastructure.client.dto.FacturacionElectronicaDto;

import java.util.List;

@FeignClient(name = "tesoreria-core-service", contextId = "facturacionElectronicaClient", path = "/api/tesoreria/core/facturacionElectronica")
public interface FacturacionElectronicaClient {

    @GetMapping("/chequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}")
    List<FacturacionElectronicaDto> findAllByChequera(@PathVariable Integer facultadId,
                                                      @PathVariable Integer tipoChequeraId,
                                                      @PathVariable Long chequeraSerieId);

}
package um.tesoreria.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import um.tesoreria.report.kotlin.dto.tesoreria.core.ChequeraCuotaDto;

import java.time.OffsetDateTime;
import java.util.List;

@FeignClient(name = "tesoreria-core-service/api/tesoreria/core/chequeraCuota")
public interface ChequeraCuotaClient {

    @GetMapping("/chequera/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{alternativaId}")
    List<ChequeraCuotaDto> findAllByChequera(@PathVariable Integer facultadId,
                                             @PathVariable Integer tipoChequeraId,
                                             @PathVariable Long chequeraSerieId,
                                             @PathVariable Integer alternativaId);

    @GetMapping("/inconsistencias/{desde}/{hasta}")
    List<ChequeraCuotaDto> findAllInconsistencias(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime desde,
                                                  @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime hasta);

    @GetMapping("/{chequeraCuotaId}")
    ChequeraCuotaDto findByChequeraCuotaId(@PathVariable Long chequeraCuotaId);

    @GetMapping("/unique/{facultadId}/{tipoChequeraId}/{chequeraSerieId}/{productoId}/{alternativaId}/{cuotaId}")
    ChequeraCuotaDto findByUnique(@PathVariable Integer facultadId,
                                  @PathVariable Integer tipoChequeraId,
                                  @PathVariable Long chequeraSerieId,
                                  @PathVariable Integer productoId,
                                  @PathVariable Integer alternativaId,
                                  @PathVariable Integer cuotaId);

}

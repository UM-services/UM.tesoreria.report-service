package um.tesoreria.report.hexagonal.recibos.infrastructure.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.report.hexagonal.recibos.domain.ports.in.ListRecibosUseCase;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tesoreria/report/recibos")
@RequiredArgsConstructor
@Slf4j
public class RecibosReportController {

    private final ListRecibosUseCase listRecibosUseCase;

    @GetMapping("/facultad/{facultadId}/lectivo/{lectivoId}/persona/{personaId}/documento/{documentoId}")
    public ResponseEntity<List<ReciboResponse>> listRecibos(@PathVariable Integer facultadId,
                                                            @PathVariable Integer lectivoId,
                                                            @PathVariable BigDecimal personaId,
                                                            @PathVariable Integer documentoId) {
        log.debug("Processing RecibosReportController.listRecibos");
        return ResponseEntity.ok(listRecibosUseCase.listRecibos(facultadId, lectivoId, personaId, documentoId).stream()
                .map(ReciboResponse::from)
                .toList());
    }
}
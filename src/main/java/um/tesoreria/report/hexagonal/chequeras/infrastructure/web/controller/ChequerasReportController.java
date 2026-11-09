package um.tesoreria.report.hexagonal.chequeras.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import um.tesoreria.report.hexagonal.chequeras.application.service.ChequerasReportService;
import um.tesoreria.report.hexagonal.chequeras.domain.model.DestinoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.web.dto.PlanillaDetalleZipRequest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import static um.tesoreria.report.util.Tool.generateFile;
import static um.tesoreria.report.util.Tool.generateTemporaryFile;

@RestController
@RequestMapping("/api/tesoreria/report/chequeras")
@RequiredArgsConstructor
@Slf4j
public class ChequerasReportController {

    private final ChequerasReportService service;

    @GetMapping("/planilla/detalle/facultad/{facultadId}/lectivo/{lectivoId}/geografica/{geograficaId}")
    public ResponseEntity<Resource> generatePlanillaDetalle(@PathVariable Integer facultadId,
                                                            @PathVariable Integer lectivoId,
                                                            @PathVariable Integer geograficaId) throws FileNotFoundException {
        log.debug("Processing ChequerasReportController.generatePlanillaDetalle");
        return generateFile(service.generatePlanillaDetalle(facultadId, lectivoId, geograficaId), "planilla.xlsx");
    }

    @GetMapping("/planilla/detalle/vertical/facultad/{facultadId}/lectivo/{lectivoId}/geografica/{geograficaId}")
    public ResponseEntity<Resource> generatePlanillaDetalleVertical(@PathVariable Integer facultadId,
                                                                    @PathVariable Integer lectivoId,
                                                                    @PathVariable Integer geograficaId) throws FileNotFoundException {
        log.debug("Processing ChequerasReportController.generatePlanillaDetalleVertical");
        return generateFile(service.generatePlanillaDetalleVertical(facultadId, lectivoId, geograficaId), "planilla.xlsx");
    }

    @PostMapping("/planilla/detalle/zip")
    public ResponseEntity<Resource> generatePlanillaDetalleZip(@Valid @RequestBody PlanillaDetalleZipRequest request)
            throws IOException {
        log.debug("Processing ChequerasReportController.generatePlanillaDetalleZip");
        try {
            var destinos = request.destinos().stream()
                    .map(destino -> new DestinoPlanilla(destino.facultadId(), destino.geograficaId()))
                    .toList();
            String filename = service.generatePlanillaDetalleZip(request.lectivoId(), request.formato(), destinos);
            String headerFilename = "detalle-cuotas-" + request.lectivoId() + "-"
                    + request.formato().name().toLowerCase(Locale.ROOT) + ".zip";
            return generateTemporaryFile(filename, headerFilename, MediaType.parseMediaType("application/zip"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("/planilla/pagos/facultad/{facultadId}/tipoChequera/{tipoChequeraId}/lectivo/{lectivoId}")
    public ResponseEntity<Resource> generatePlanillaPagos(@PathVariable Integer facultadId,
                                                          @PathVariable Integer tipoChequeraId,
                                                          @PathVariable Integer lectivoId) throws FileNotFoundException {
        log.debug("Processing ChequerasReportController.generatePlanillaPagos");
        return generateFile(service.generatePlanillaPagos(facultadId, tipoChequeraId, lectivoId), "pagos.xlsx");
    }

    @GetMapping("/estado/facultad/{facultadId}/tipoChequera/{tipoChequeraId}/chequeraSerie/{chequeraSerieId}/alternativa/{alternativaId}")
    public ResponseEntity<Resource> generateEstadoChequera(@PathVariable Integer facultadId,
                                                           @PathVariable Integer tipoChequeraId,
                                                           @PathVariable Long chequeraSerieId,
                                                           @PathVariable Integer alternativaId) throws FileNotFoundException {
        log.debug("Processing ChequerasReportController.generateEstadoChequera");
        return generateFile(service.generateEstadoChequera(facultadId, tipoChequeraId, chequeraSerieId, alternativaId),
                "estado-chequera.pdf");
    }


    @GetMapping("/materias/facultad/{facultadId}/tipoChequera/{tipoChequeraId}/chequeraSerie/{chequeraSerieId}/alternativa/{alternativaId}")
    public ResponseEntity<Resource> generateMateriasInscriptas(@PathVariable Integer facultadId,
                                                              @PathVariable Integer tipoChequeraId,
                                                              @PathVariable Long chequeraSerieId,
                                                              @PathVariable Integer alternativaId) throws IOException {
        return generateTemporaryFile(service.generateMateriasInscriptas(facultadId, tipoChequeraId, chequeraSerieId, alternativaId),
                "materias-inscriptas.pdf");
    }
}

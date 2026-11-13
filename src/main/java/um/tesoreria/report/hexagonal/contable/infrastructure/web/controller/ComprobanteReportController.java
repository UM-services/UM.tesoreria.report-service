package um.tesoreria.report.hexagonal.contable.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateComprobantePdfUseCase;

@RestController
@RequestMapping("/api/tesoreria/report/contable/comprobante")
@RequiredArgsConstructor
@Slf4j
public class ComprobanteReportController {

    private final GenerateComprobantePdfUseCase useCase;

    @GetMapping("/{proveedorMovimientoId}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long proveedorMovimientoId) {
        log.debug("Processing ComprobanteReportController.pdf");
        byte[] contenido = useCase.generate(proveedorMovimientoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprobante-" + proveedorMovimientoId + ".pdf")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(contenido.length)
                .body(contenido);
    }
}
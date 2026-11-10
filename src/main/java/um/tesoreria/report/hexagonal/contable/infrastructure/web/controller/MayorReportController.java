package um.tesoreria.report.hexagonal.contable.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSeleccion;
import um.tesoreria.report.hexagonal.contable.domain.ports.in.GenerateMayorExcelUseCase;
import um.tesoreria.report.hexagonal.contable.infrastructure.web.dto.MayorExcelRequest;

@RestController
@RequestMapping("/api/tesoreria/report/contable/mayor")
@RequiredArgsConstructor
@Slf4j
public class MayorReportController {

    static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final GenerateMayorExcelUseCase useCase;

    @PostMapping("/excel")
    public ResponseEntity<byte[]> excel(@RequestBody MayorExcelRequest request) {
        log.debug("Processing MayorReportController.excel");
        byte[] contenido = useCase.generate(new MayorSeleccion(request.desde(), request.hasta(),
                request.cuentas(), request.cuentaDesde(), request.cuentaHasta()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mayor.xlsx")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(XLSX)
                .contentLength(contenido.length)
                .body(contenido);
    }
}
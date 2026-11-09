package um.tesoreria.report.hexagonal.chequeras.infrastructure.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import um.tesoreria.report.hexagonal.chequeras.application.service.ChequerasReportService;
import um.tesoreria.report.hexagonal.chequeras.domain.model.DestinoPlanilla;
import um.tesoreria.report.hexagonal.chequeras.domain.model.FormatoPlanilla;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChequerasReportControllerZipTest {

    private static final String RUTA = "/api/tesoreria/report/chequeras/planilla/detalle/zip";

    // protects=ruta, body, content type, nombre de descarga y borrado del ZIP temporal; fails_when=cambia la ruta,
    // pierde un destino o deja el archivo; no_claim=no prueba la generación real de los XLSX.
    @Test
    void descargaElZipConElNombreYContentTypeEsperados(@TempDir Path directorio) throws Exception {
        ChequerasReportService service = mock(ChequerasReportService.class);
        byte[] bytes = "PK-fixture".getBytes(StandardCharsets.US_ASCII);
        Path archivo = directorio.resolve("detalle.zip");
        Files.write(archivo, bytes);
        when(service.generatePlanillaDetalleZip(eq(38), eq(FormatoPlanilla.HORIZONTAL),
                eq(List.of(new DestinoPlanilla(6, 1), new DestinoPlanilla(6, 2)))))
                .thenReturn(archivo.toString());
        var mvc = MockMvcBuilders.standaloneSetup(new ChequerasReportController(service)).build();

        mvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lectivoId": 38,
                                  "formato": "HORIZONTAL",
                                  "destinos": [
                                    { "facultadId": 6, "geograficaId": 1 },
                                    { "facultadId": 6, "geograficaId": 2 }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=detalle-cuotas-38-horizontal.zip"))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(content().contentType(MediaType.parseMediaType("application/zip")))
                .andExpect(content().bytes(bytes));

        verify(service).generatePlanillaDetalleZip(38, FormatoPlanilla.HORIZONTAL,
                List.of(new DestinoPlanilla(6, 1), new DestinoPlanilla(6, 2)));
        assertThat(archivo).doesNotExist();
    }

    // protects=una selección vacía se rechaza con 400 sin llamar al servicio; fails_when=genera un ZIP vacío;
    // no_claim=no valida el texto del error.
    @Test
    void rechazaUnaSeleccionVacia() throws Exception {
        ChequerasReportService service = mock(ChequerasReportService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ChequerasReportController(service)).build();

        mvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "lectivoId": 38, "formato": "HORIZONTAL", "destinos": [] }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // protects=un formato inválido se rechaza con 400; fails_when=acepta cualquier texto; no_claim=no valida el
    // mensaje de error.
    @Test
    void rechazaUnFormatoInvalido() throws Exception {
        ChequerasReportService service = mock(ChequerasReportService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ChequerasReportController(service)).build();

        mvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "lectivoId": 38, "formato": "DIAGONAL", "destinos": [ { "facultadId": 6, "geograficaId": 1 } ] }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    // protects=más de 50 destinos se rechaza con 400; fails_when=acepta un pedido sin límite; no_claim=no mide
    // tiempos reales de generación.
    @Test
    void rechazaMasDeCincuentaDestinos() throws Exception {
        ChequerasReportService service = mock(ChequerasReportService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ChequerasReportController(service)).build();
        String destinos = IntStream.rangeClosed(1, 51)
                .mapToObj(id -> "{ \"facultadId\": 6, \"geograficaId\": " + id + " }")
                .collect(Collectors.joining(","));

        mvc.perform(post(RUTA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"lectivoId\": 38, \"formato\": \"HORIZONTAL\", \"destinos\": ["
                                + destinos + "] }"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}

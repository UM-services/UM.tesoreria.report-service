package um.tesoreria.report.hexagonal.chequeras.infrastructure.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import um.tesoreria.report.hexagonal.chequeras.application.service.ChequerasReportService;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChequerasMateriasControllerTest {
    // protects: ruta, claves, bytes y nombre de descarga; fails_when: cambia la ruta o descarga otro archivo; no_claim: servicio simulado, no prueba core ni PDF.
    @Test
    void downloadsTheSeparatePdf(@TempDir Path dir) throws Exception {
        var service = mock(ChequerasReportService.class);
        byte[] bytes = "%PDF-fixture".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        Path file = dir.resolve("fixture.pdf");
        Files.write(file, bytes);
        when(service.generateMateriasInscriptas(1, 2, 12345L, 1)).thenReturn(file.toString());
        var mvc = MockMvcBuilders.standaloneSetup(new ChequerasReportController(service)).build();
        mvc.perform(get("/api/tesoreria/report/chequeras/materias/facultad/1/tipoChequera/2/chequeraSerie/12345/alternativa/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=materias-inscriptas.pdf"))
                .andExpect(header().string("Cache-Control", "no-cache, no-store, must-revalidate"))
                .andExpect(content().bytes(bytes));
        verify(service).generateMateriasInscriptas(1, 2, 12345L, 1);
        org.assertj.core.api.Assertions.assertThat(file).doesNotExist();
    }
}

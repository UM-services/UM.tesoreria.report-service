package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.adapter;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import um.tesoreria.report.hexagonal.chequeras.EstadoChequeraFixture;
import um.tesoreria.report.hexagonal.chequeras.MateriasInscriptasFixture;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.web.controller.ChequerasReportController;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Integración HTTP con core simulado. Nunca se presenta como E2E. */
@SpringBootTest(properties = {"spring.cloud.consul.enabled=false", "spring.cloud.discovery.enabled=false"})
class MateriasInscriptasHttpContractTest {
    private static final List<String> REQUESTS = new CopyOnWriteArrayList<>();
    private static final HttpServer CORE = startCore();
    @TempDir static Path reports;
    @Autowired private ChequerasReportController controller;

    private static HttpServer startCore() {
        try {
            var server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            var json = JsonMapper.builder().build();
            String encabezado = json.writeValueAsString(EstadoChequeraFixture.referencia());
            String materias = json.writeValueAsString(MateriasInscriptasFixture.dtos());
            server.createContext("/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                REQUESTS.add(path);
                String body;
                int status;
                if (path.equals("/api/tesoreria/core/chequera/estado/1/2/12345/1")) { body = encabezado; status = 200; }
                else if (path.equals("/api/tesoreria/core/materiasInscriptas/1/2/12345")) { body = materias; status = 200; }
                else { body = "{}"; status = 404; }
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, bytes.length);
                try (var out = exchange.getResponseBody()) { out.write(bytes); }
            });
            server.start();
            return server;
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String url = "http://localhost:" + CORE.getAddress().getPort();
        registry.add("spring.cloud.openfeign.client.config.[tesoreria-core-service/api/tesoreria/core/chequera].url",
                () -> url + "/api/tesoreria/core/chequera");
        registry.add("spring.cloud.openfeign.client.config.materiasInscriptasClient.url", () -> url);
        registry.add("path.reports", () -> reports.toString());
    }

    @AfterAll static void stopCore() { CORE.stop(0); }

    // protects: controller→servicio→Feign consulta las dos rutas reales del contrato y descarga sus materias; fails_when: path incorrecto o respuesta no se mapea; no_claim: core simulado, sin Consul ni bases reales.
    @Test
    void downloadsAPdfThroughTheRealFeignAdapters() throws Exception {
        var result = MockMvcBuilders.standaloneSetup(controller).build().perform(get(
                "/api/tesoreria/report/chequeras/materias/facultad/1/tipoChequera/2/chequeraSerie/12345/alternativa/1"))
                .andExpect(status().isOk()).andReturn();
        try (var pdf = new PdfReader(result.getResponse().getContentAsByteArray())) {
            assertThat(new PdfTextExtractor(pdf).getTextFromPage(1)).contains("Materias inscriptas", "SIUM", "CALCULO II");
        }
        assertThat(REQUESTS).containsExactly("/api/tesoreria/core/chequera/estado/1/2/12345/1",
                "/api/tesoreria/core/materiasInscriptas/1/2/12345");
    }
}

package um.tesoreria.report.hexagonal.chequeras.infrastructure.client.mapper;

import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import um.tesoreria.report.hexagonal.chequeras.MateriasInscriptasFixture;
import um.tesoreria.report.hexagonal.chequeras.domain.model.*;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.client.dto.MateriasEstadoDto;
import um.tesoreria.report.hexagonal.chequeras.infrastructure.pdf.OpenPdfMateriasInscriptasPdfGenerator;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChequerasMapperMateriasTest {
    private final ChequerasMapper mapper = new ChequerasMapper();

    // protects: todos los datos y el orden de la fixture llegan al dominio; fails_when: se pierde un campo o un grupo; no_claim: no verifica transporte Feign.
    @Test
    void mapsTheFullContract() throws IOException {
        var dtos = MateriasInscriptasFixture.dtos();
        var bloques = mapper.toMaterias(dtos);
        assertThat(bloques).hasSize(1);
        var bloque = bloques.getFirst();
        assertThat(bloque.fuente()).isEqualTo("SIUM");
        assertThat(bloque.estado()).isEqualTo(EstadoMaterias.OK);
        assertThat(bloque.cursoInscripcion()).isEqualTo(5);
        assertThat(bloque.plan()).isEqualTo("Plan 2016");
        assertThat(bloque.carrera()).isEqualTo("Ingeniería en Informática");
        assertThat(bloque.observaciones()).isEqualTo("Texto libre de la inscripción");
        assertThat(bloque.cursos()).extracting(CursoMaterias::curso).containsExactly(1, 2, 4, 5);
        assertThat(bloque.cursos().getLast().periodos()).extracting(PeriodoMaterias::periodo)
                .containsExactly(PeriodoCursada.ANUAL, PeriodoCursada.PRIMER_SEMESTRE, PeriodoCursada.SEGUNDO_SEMESTRE);
        assertThat(bloque.cursos().getFirst().periodos().getFirst().materias())
                .containsExactly(new MateriaInscripta("0004", "CALCULO II"));
    }

    // protects: listas ausentes se normalizan sin NPE; fails_when: null se propaga o se inventa un bloque; no_claim: no dice que un cuerpo HTTP vacío sea válido.
    @Test
    void mapsNullAndEmptyLists() {
        assertThat(mapper.toMaterias(null)).isEmpty();
        assertThat(mapper.toMaterias(List.of())).isEmpty();
        assertThat(mapper.toMaterias(List.of(new MateriasEstadoDto("SIUM", "SIN_INSCRIPCION", null,
                null, null, null, null))).getFirst().cursos()).isEmpty();
    }

    // protects: valores nuevos se deserializan y representan con fallbacks; fails_when: enum rechaza JSON, fuente cambia o PDF falla; no_claim: no valida semántica de futuros valores.
    @Test
    void toleratesUnknownSourceStateAndPeriodThroughThePdf() throws IOException {
        String json = """
                [{"fuente":"OTRA","estado":"NUEVO"},
                 {"fuente":"GUARANI","estado":"OK","cursos":[{"curso":null,
                  "periodos":[{"periodo":"FUTURO","materias":[{"materiaId":"X001","nombre":null}]}]}]}]
                """;
        List<MateriasEstadoDto> dtos = JsonMapper.builder().build().readValue(json, new TypeReference<>() {});
        var bloques = mapper.toMaterias(dtos);
        assertThat(bloques.getFirst().fuente()).isEqualTo("OTRA");
        assertThat(bloques.getFirst().estado()).isEqualTo(EstadoMaterias.NO_DISPONIBLE);
        assertThat(bloques.getLast().cursos().getFirst().periodos().getFirst().periodo()).isEqualTo(PeriodoCursada.SIN_PERIODO);
        var out = new ByteArrayOutputStream();
        new OpenPdfMateriasInscriptasPdfGenerator().generate(MateriasInscriptasFixture.reporte(bloques), out);
        try (PdfReader pdf = new PdfReader(out.toByteArray())) {
            assertThat(new PdfTextExtractor(pdf).getTextFromPage(1)).contains("OTRA",
                    "No se pudieron consultar las materias en OTRA.", "Guaraní", "Sin período", "X001");
        }
    }
}

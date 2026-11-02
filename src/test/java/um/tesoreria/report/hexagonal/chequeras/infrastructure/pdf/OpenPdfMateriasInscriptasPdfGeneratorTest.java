package um.tesoreria.report.hexagonal.chequeras.infrastructure.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import um.tesoreria.report.hexagonal.chequeras.MateriasInscriptasFixture;
import um.tesoreria.report.hexagonal.chequeras.domain.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfMateriasInscriptasPdfGeneratorTest {
    private final OpenPdfMateriasInscriptasPdfGenerator generator = new OpenPdfMateriasInscriptasPdfGenerator();

    // protects: encabezado y las 13 materias del contrato en sus grupos y orden; fails_when: se omiten o reordenan; no_claim: no consulta servicios reales.
    @Test
    void rendersTheContractInReceivedOrder() throws IOException {
        var reporte = MateriasInscriptasFixture.referencia();
        try (PdfReader pdf = render(reporte)) {
            String text = allText(pdf);
            assertThat(text).contains("Materias inscriptas", "Facultad de Ingeniería", "MUÑOZ, Ana Ejemplo",
                    "Chequera: 1/2/12345", "Lectivo 2026 - 2027", "SIUM", "Curso: 5",
                    "Carrera: Plan 2016/Ingeniería en Informática", "Observaciones: Texto libre de la inscripción");
            List<String> expected = new ArrayList<>();
            for (var curso : reporte.materias().getFirst().cursos()) {
                expected.add("Curso " + curso.curso());
                for (var periodo : curso.periodos()) {
                    expected.add(periodo.periodo().etiqueta());
                    for (var materia : periodo.materias()) {
                        expected.add(materia.materiaId());
                        expected.add(materia.nombre());
                    }
                }
            }
            int offset = 0;
            int cantidad = 0;
            for (String value : expected) {
                int position = text.indexOf(value, offset);
                assertThat(position).as("Texto siguiente: %s", value).isGreaterThanOrEqualTo(offset);
                offset = position + value.length();
            }
            for (var curso : reporte.materias().getFirst().cursos())
                for (var periodo : curso.periodos()) cantidad += periodo.materias().size();
            assertThat(cantidad).isEqualTo(13);
            assertThat(text).doesNotContain("Estado de Chequera", "Subtotal", "Adhesión");
        }
    }

    // protects: lista vacía conserva el encabezado y explica no grado; fails_when: PDF vacío o leyenda ausente; no_claim: no determina la clase en el core.
    @Test
    void rendersAnEmptyList() throws IOException {
        try (PdfReader pdf = render(MateriasInscriptasFixture.reporte(List.of()))) {
            assertThat(allText(pdf)).contains("Materias inscriptas", "Chequera: 1/2/12345",
                    "Esta chequera no es de grado: no corresponde informar materias.");
        }
    }

    // protects: materias null se normaliza como lista vacía; fails_when: NPE o falta la leyenda; no_claim: no valida compatibilidad HTTP con un core viejo.
    @Test
    void rendersANullList() throws IOException {
        try (PdfReader pdf = render(MateriasInscriptasFixture.reporte(null))) {
            assertThat(allText(pdf)).contains("Esta chequera no es de grado: no corresponde informar materias.");
        }
    }

    // protects: cada fuente no disponible tiene su leyenda exacta; fails_when: se oculta o confunde la fuente; no_claim: no simula una caída de red real.
    @Test
    void rendersUnavailableSources() throws IOException {
        try (PdfReader pdf = render(MateriasInscriptasFixture.reporte(List.of(
                MateriasInscriptasFixture.estado("SIUM", EstadoMaterias.NO_DISPONIBLE),
                MateriasInscriptasFixture.estado("GUARANI", EstadoMaterias.NO_DISPONIBLE))))) {
            assertThat(allText(pdf)).contains("SIUM", "Guaraní", "No se pudieron consultar las materias en la facultad.",
                    "No se pudieron consultar las materias en Guaraní.").doesNotContain("Esta chequera no es de grado");
        }
    }

    // protects: un bloque sin inscripción sigue visible; fails_when: se omite o muestra no grado; no_claim: no verifica las inscripciones de la base.
    @Test
    void rendersSourcesWithoutEnrollment() throws IOException {
        try (PdfReader pdf = render(MateriasInscriptasFixture.reporte(List.of(
                MateriasInscriptasFixture.estado("GUARANI", EstadoMaterias.SIN_INSCRIPCION))))) {
            assertThat(allText(pdf)).contains("Guaraní", "Sin inscripción a materias en el lectivo.")
                    .doesNotContain("Esta chequera no es de grado");
        }
    }

    // protects: sin curso, sin período, curso cero y nombre nulo tienen texto; fails_when: se pierde una materia o aparece null; no_claim: no valida agrupación del core.
    @Test
    void rendersMissingAcademicData() throws IOException {
        var bloque = new MateriasEstado("SIUM", EstadoMaterias.OK, 0, null, null, null,
                List.of(new CursoMaterias(null, List.of(new PeriodoMaterias(PeriodoCursada.SIN_PERIODO,
                        List.of(new MateriaInscripta("X001", null)))))));
        try (PdfReader pdf = render(MateriasInscriptasFixture.reporte(List.of(bloque)))) {
            assertThat(allText(pdf)).contains("Curso: Sin cursar", "Sin curso asignado", "Sin período", "X001", "—")
                    .doesNotContain("null");
        }
    }

    // protects: la tabla continúa con columnas y la materia siguiente, sin duplicar filas; fails_when: falta encabezado o hay salto de materia; no_claim: no prueba volumen real de Guaraní.
    @Test
    void repeatsOnlyColumnHeadersOnContinuationPages() throws IOException {
        try (PdfReader pdf = render(MateriasInscriptasFixture.reporte(List.of(MateriasInscriptasFixture.volumen(60))))) {
            assertThat(pdf.getNumberOfPages()).isGreaterThan(1);
            int expected = 1;
            for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
                String text = new PdfTextExtractor(pdf).getTextFromPage(page);
                if (page > 1) {
                    var lines = text.strip().lines().toList();
                    assertThat(lines.getFirst().strip()).isEqualTo("Código Materia");
                    assertThat(lines.get(1).strip()).isEqualTo("M%03d Materia de prueba %03d".formatted(expected, expected));
                    assertThat(text).doesNotContain("Materias inscriptas");
                }
                var rows = java.util.regex.Pattern.compile("M(\\d{3}) Materia de prueba \\d{3}").matcher(text);
                while (rows.find()) assertThat(Integer.parseInt(rows.group(1))).isEqualTo(expected++);
            }
            assertThat(expected).isEqualTo(61);
        }
    }

    // protects: un fallo previo a la primera página conserva la causa; fails_when: close la reemplaza por no pages; no_claim: no simula todos los errores de OpenPDF.
    @Test
    void preservesTheOriginalRenderingFailure() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> generator.generate(
                new MateriasInscriptas(null, List.of()), new ByteArrayOutputStream()))
                .isInstanceOf(NullPointerException.class);
    }

    private PdfReader render(MateriasInscriptas reporte) throws IOException {
        var out = new ByteArrayOutputStream();
        generator.generate(reporte, out);
        return new PdfReader(out.toByteArray());
    }

    private String allText(PdfReader pdf) throws IOException {
        StringBuilder text = new StringBuilder();
        for (int page = 1; page <= pdf.getNumberOfPages(); page++)
            text.append(new PdfTextExtractor(pdf).getTextFromPage(page)).append('\n');
        return text.toString();
    }
}

package um.tesoreria.report.hexagonal.chequeras.infrastructure.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import um.tesoreria.report.hexagonal.chequeras.EstadoChequeraFixture;
import um.tesoreria.report.hexagonal.chequeras.domain.model.EstadoChequera;
import um.tesoreria.report.hexagonal.chequeras.domain.model.CuotaEstado;
import um.tesoreria.report.hexagonal.chequeras.domain.model.ProductoEstado;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfEstadoChequeraPdfGeneratorTest {

    private final OpenPdfEstadoChequeraPdfGenerator generator = new OpenPdfEstadoChequeraPdfGenerator();

    @Test
    void generate_producesTwoPagesForTheReferenceChequera() throws IOException {
        PdfReader pdf = render(EstadoChequeraFixture.referencia());

        assertThat(pdf.getNumberOfPages()).isEqualTo(2);
    }

    @Test
    void generate_firstPageHasTheHeaderAndTheProductsInTheReceivedOrder() throws IOException {
        PdfReader pdf = render(EstadoChequeraFixture.referencia());

        String page1 = text(pdf, 1);
        assertThat(page1)
                .contains("Estado de Chequera", "Hoja: 1", "Facultad de Ingeniería", "UNIVERSIDAD DE MENDOZA",
                        "Titular: (12345678) MUÑOZ, Ana Ejemplo", "Tipo Chequera: Matrícula y Arancel",
                        "Tipo Arancel: Ciclo Completo", "Ciclo Lectivo: Lectivo 2026 - 2027",
                        "Porcentaje de beca: 15%", "Tipo Impresion: Rapipago", "Chequera: 1/2/12345",
                        "NO VALIDO COMO COMPROBANTE DE PAGO", "Alternativa: 1");
        assertThat(page1.indexOf("Producto: Matrícula")).isNotNegative()
                .isLessThan(page1.indexOf("Producto: Arancel"));
    }

    @Test
    void generate_firstPageShowsCuotasSubtotalsAndPayments() throws IOException {
        PdfReader pdf = render(EstadoChequeraFixture.referencia());

        String page1 = text(pdf, 1);
        assertThat(page1)
                .contains("Primer vencimiento", "A Pagar", "Fecha Pago", "Pagado")
                .contains("Matrícula: 1/2", "Arancel Mensual: 12/12")
                .contains("Periodo: 6/2026 (0)")
                .contains("D2026062301_30000000000", "MercadoPago")
                .contains("Subtotal Producto: 375,000.00", "Subtotal Pagado: 182,000.00", "Subtotal Deuda: 193,000.00")
                .contains("Subtotal Producto: 3,243,000.00", "Subtotal Pagado: 2,471,000.00", "Subtotal Deuda: 772,000.00");
    }

    @Test
    void generate_secondPageRepeatsTheHeaderAndListsTheDebits() throws IOException {
        PdfReader pdf = render(EstadoChequeraFixture.referencia());

        String page2 = text(pdf, 2);
        assertThat(page2)
                .contains("Hoja: 2", "Estado de Chequera", "Titular: (12345678) MUÑOZ, Ana Ejemplo",
                        "Tipo Impresion: Rapipago", "Chequera: 1/2/12345")
                .contains("Adhesión de chequera al Débito Automático")
                .contains(EstadoChequeraFixture.CBU, "0011000100", "04/06/2026 00:00");
    }

    @Test
    void generate_numbersTheDebitPageWithThePageItReallyLandsOn() throws IOException {
        // Con muchas cuotas el contenido de la hoja 1 desborda; la hoja de débitos ya no es la 2.
        PdfReader pdf = render(EstadoChequeraFixture.referencia(60));

        int pages = pdf.getNumberOfPages();
        assertThat(pages).isGreaterThan(2);
        assertThat(text(pdf, pages)).contains("Hoja: " + pages, "Adhesión de chequera al Débito Automático");
    }

    @Test
    void generate_toleratesMissingData() throws IOException {
        // Sin porcentaje de beca, con textos nulos, sin débitos y con una cuota impaga sin fechas.
        EstadoChequera sinDatos = new EstadoChequera(15, null, 2, null, 12345L, null, null, null, null, null,
                null, null, 1,
                List.of(new ProductoEstado(1, null, null, null, null, null,
                        List.of(new CuotaEstado(1, 3, 2026, null, null, null, null, null, null)))),
                null);

        PdfReader pdf = render(sinDatos);

        assertThat(pdf.getNumberOfPages()).isEqualTo(2);
        assertThat(text(pdf, 1)).contains("Porcentaje de beca: 0%", "Hoja: 1");
        assertThat(text(pdf, 2)).contains("Hoja: 2", "Adhesión de chequera al Débito Automático");
    }

    @Test
    void generate_showsTheBecaPercentageWithoutTrailingZeros() throws IOException {
        EstadoChequera base = EstadoChequeraFixture.referencia();
        EstadoChequera conBeca = new EstadoChequera(base.facultadId(), base.facultadNombre(), base.tipoChequeraId(),
                base.tipoChequeraNombre(), base.chequeraSerieId(), base.personaId(), base.personaApellido(),
                base.personaNombre(), base.arancelTipoDescripcion(), base.lectivoNombre(), new BigDecimal("0.5000"),
                base.tipoImpresionNombre(), base.alternativaId(), base.productos(), base.debitos());

        assertThat(text(render(conBeca), 1)).contains("Porcentaje de beca: 50%");
    }

    private PdfReader render(EstadoChequera estado) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        generator.generate(estado, out);
        return new PdfReader(out.toByteArray());
    }

    private String text(PdfReader pdf, int page) throws IOException {
        return new PdfTextExtractor(pdf).getTextFromPage(page);
    }
}
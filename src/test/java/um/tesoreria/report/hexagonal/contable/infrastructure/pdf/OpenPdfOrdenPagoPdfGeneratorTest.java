package um.tesoreria.report.hexagonal.contable.infrastructure.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import um.tesoreria.report.hexagonal.contable.domain.model.OrdenPago;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfOrdenPagoPdfGeneratorTest {

    private final OpenPdfOrdenPagoPdfGenerator generator = new OpenPdfOrdenPagoPdfGenerator();

    private static OffsetDateTime fecha(int anio, int mes, int dia) {
        return OffsetDateTime.of(anio, mes, dia, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    private static OrdenPago orden(List<OrdenPago.Comprobante> comprobantes) {
        return new OrdenPago(10L, 15, 3684L, fecha(2026, 11, 2), null, "Compra de insumos", "ACME SA", "Calle 1 234",
                "30-12345678-9", comprobantes, new BigDecimal("560190.17"),
                List.of(new OrdenPago.Valor("Transferencia", "ACME SA", 77L, fecha(2026, 11, 2),
                        new BigDecimal("560190.17"))),
                new BigDecimal("560190.17"),
                List.of(new OrdenPago.Traza(new BigDecimal("2110101"), "Proveedores", new BigDecimal("560190.17"), BigDecimal.ZERO),
                        new OrdenPago.Traza(new BigDecimal("1110101"), "Banco Nación", BigDecimal.ZERO, new BigDecimal("560190.17"))),
                new BigDecimal("560190.17"), new BigDecimal("560190.17"));
    }

    private static List<OrdenPago.Comprobante> unComprobante() {
        return List.of(new OrdenPago.Comprobante("Factura A", 1, 2595L, fecha(2026, 10, 15), new BigDecimal("560190.17")));
    }

    private static String texto(byte[] pdf) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        try {
            StringBuilder sb = new StringBuilder();
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                sb.append(extractor.getTextFromPage(i)).append('\n');
            }
            return sb.toString();
        } finally {
            reader.close();
        }
    }

    private static int paginas(byte[] pdf) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        try {
            return reader.getNumberOfPages();
        } finally {
            reader.close();
        }
    }

    // protects=el archivo es un PDF válido con el número sin ceros, la fecha, el proveedor, los tres bloques y las firmas; fails_when=falta un bloque, el número sale con ceros o se pierde un dato de la cabecera; no_claim=no prueba la posición ni el aspecto
    @Test
    void generate_writesHeaderBlocksAndSignatures() throws IOException {
        byte[] pdf = generator.generate(orden(unComprobante()));

        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        String texto = texto(pdf);
        assertThat(texto).contains("Orden de Pago", "Nº: 15 - 3684", "Fecha: 02/11/2026", "ACME SA",
                "30-12345678-9", "Calle 1 234", "Compra de insumos",
                "Comprobantes", "Factura A", "1 - 2595", "15/10/2026",
                "Valores", "Transferencia", "Traza contable", "2110101 Proveedores", "1110101 Banco Nación",
                "Total a Pagar", "Firma de Secretaría", "Firma del Rector");
    }

    // protects=los importes salen con formato es-AR tal como llegan del core, incluido el total a pagar; fails_when=se usa otro separador, se recalcula un total o se pierden los decimales; no_claim=no prueba que el core calcule bien
    @Test
    void generate_formatsAmountsInSpanishArgentina() throws IOException {
        String texto = texto(generator.generate(orden(unComprobante())));

        assertThat(texto).contains("560.190,17");
        assertThat(texto).doesNotContain("560190.17");
    }

    // protects=sin comprobantes, valores ni traza se genera igual el PDF, sin la palabra null ni errores; fails_when=una lista vacía o un dato nulo corta la generación o imprime null; no_claim=no prueba qué muestra el sistema anterior en ese caso
    @Test
    void generate_withEmptyListsAndNullFieldsStillBuildsThePdf() throws IOException {
        OrdenPago vacia = new OrdenPago(10L, null, null, null, null, null, null, null, null,
                List.of(), null, List.of(), null, List.of(), null, null);

        byte[] pdf = generator.generate(vacia);

        String texto = texto(pdf);
        assertThat(texto).contains("Orden de Pago", "Comprobantes", "Valores", "Traza contable");
        assertThat(texto).doesNotContain("null");
    }

    // protects=una orden anulada lo dice en el PDF con su fecha; fails_when=se imprime una orden anulada como si estuviera vigente; no_claim=no define si la reimpresión de anuladas debe permitirse
    @Test
    void generate_marksCancelledOrders() throws IOException {
        OrdenPago base = orden(unComprobante());
        OrdenPago anulada = new OrdenPago(base.proveedorMovimientoId(), base.prefijo(), base.numeroComprobante(),
                base.fecha(), fecha(2026, 11, 5), base.concepto(), base.razonSocial(), base.domicilio(), base.cuit(),
                base.comprobantes(), base.totalComprobantes(), base.valores(), base.totalValores(), base.traza(),
                base.totalDebe(), base.totalHaber());

        assertThat(texto(generator.generate(anulada))).contains("ANULADA el 05/11/2026");
    }

    // protects=con muchos comprobantes el PDF continúa en otra hoja, numera las páginas y conserva las firmas; fails_when=se corta el contenido, se pierde la numeración o desaparecen las firmas; no_claim=no prueba la cantidad exacta de hojas
    @Test
    void generate_continuesOnNextPageWhenThereAreManyRows() throws IOException {
        List<OrdenPago.Comprobante> muchos = new ArrayList<>();
        for (int i = 1; i <= 90; i++) {
            muchos.add(new OrdenPago.Comprobante("Factura A", 1, (long) i, fecha(2026, 10, 1), new BigDecimal("10.00")));
        }

        byte[] pdf = generator.generate(orden(muchos));

        assertThat(paginas(pdf)).isGreaterThan(1);
        String texto = texto(pdf);
        assertThat(texto).contains("Página 1", "Página 2", "1 - 90", "Firma del Rector");
    }

    // protects=el número se escribe prefijo - número sin ceros y queda vacío si falta alguno; fails_when=se imprime con ceros, con null o con un guion suelto; no_claim=no prueba el formato de otros comprobantes
    @Test
    void numero_isPrefixDashNumberWithoutPadding() {
        assertThat(OpenPdfOrdenPagoPdfGenerator.numero(15, 3684L)).isEqualTo("15 - 3684");
        assertThat(OpenPdfOrdenPagoPdfGenerator.numero(null, 3684L)).isEmpty();
        assertThat(OpenPdfOrdenPagoPdfGenerator.numero(15, null)).isEmpty();
    }
}
package um.tesoreria.report.hexagonal.contable.infrastructure.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorCuenta;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorMovimiento;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSaldoInicial;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenPdfMayorPdfGeneratorTest {

    private final OpenPdfMayorPdfGenerator generator = new OpenPdfMayorPdfGenerator();

    private static OffsetDateTime fecha(int anio, int mes, int dia) {
        return OffsetDateTime.of(anio, mes, dia, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    private static MayorMovimiento movimiento(OffsetDateTime fecha, String ejercicio, String debe, String haber,
                                              String saldo) {
        return new MayorMovimiento(fecha, 7, 1, ejercicio, "Factura", "Proveedor SA", "Compra", null, null,
                debe == null ? null : new BigDecimal(debe), haber == null ? null : new BigDecimal(haber),
                new BigDecimal(saldo), "15/84", null, null, null);
    }

    private static MayorCuenta cuenta(String numero, String nombre, List<MayorMovimiento> movimientos) {
        return new MayorCuenta(new BigDecimal(numero), nombre,
                new MayorSaldoInicial(new BigDecimal("1000.50"), null, new BigDecimal("1000.50")),
                movimientos, new BigDecimal("1234.50"), new BigDecimal("30.00"), new BigDecimal("2205.00"));
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

    // protects=el PDF es válido y trae título, cuenta con su nombre, período, saldo inicial, asiento «dd/MM/yy-orden», la orden de pago del valor en OP (ordenPagoValor), totales y saldo final con formato es-AR; fails_when=falta algún dato o se pierde el formato de importes; no_claim=no prueba el aspecto visual ni la posición de las columnas
    @Test
    void escribeLosDatosDelCoreConFormatoArgentino() throws IOException {
        Mayor mayor = new Mayor(fecha(2026, 3, 1), fecha(2026, 4, 30), List.of(cuenta("10101010001", "Caja Mendoza",
                List.of(movimiento(fecha(2026, 3, 5), "2025/2026", "1234.50", null, "2235.00"),
                        movimiento(fecha(2026, 4, 2), "2026/2027", null, "30.00", "2205.00")))));

        byte[] pdf = generator.generate(mayor);

        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
        String t = texto(pdf);
        assertThat(t).contains("Mayor con Valores", "Caja Mendoza", "10101010001", "01/03/2026", "30/04/2026",
                "Saldo Inicial", "05/03/26-7", "15/84", "Totales", "Saldo final",
                "1.000,50", "1.234,50", "2.205,00");
    }

    // protects=sin movimientos o sin cuentas el PDF igual se genera con el aviso correspondiente; fails_when=falla con listas vacías o nulas; no_claim=no prueba qué cuentas debería traer el core
    @Test
    void conListasVaciasIndicaQueNoHayDatos() throws IOException {
        String sinMovimientos = texto(generator.generate(new Mayor(fecha(2026, 1, 1), fecha(2026, 1, 31),
                List.of(cuenta("10101010001", "Caja", List.of())))));
        String sinCuentas = texto(generator.generate(new Mayor(fecha(2026, 1, 1), fecha(2026, 1, 31), null)));

        assertThat(sinMovimientos).contains(OpenPdfMayorPdfGenerator.SIN_MOVIMIENTOS);
        assertThat(sinCuentas).contains(OpenPdfMayorPdfGenerator.SIN_CUENTAS);
    }

    // protects=cada cuenta empieza en su propia hoja; fails_when=dos cuentas comparten hoja; no_claim=no fija cuántas hojas ocupa cada una
    @Test
    void cadaCuentaVaEnSuPropiaHoja() throws IOException {
        byte[] pdf = generator.generate(new Mayor(fecha(2026, 3, 1), fecha(2026, 3, 31), List.of(
                cuenta("10101010001", "Caja Mendoza", List.of(movimiento(fecha(2026, 3, 5), "2025/2026", "1.00", null, "1.00"))),
                cuenta("10101010002", "Caja San Rafael", List.of(movimiento(fecha(2026, 3, 6), "2025/2026", "2.00", null, "2.00"))))));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(2);
        } finally {
            reader.close();
        }
    }

    // protects=un mayor largo ocupa varias hojas, con el encabezado repetido y el número de página; fails_when=todo queda en una hoja o se pierde el pie; no_claim=no fija cuántas filas entran por hoja
    @Test
    void unMayorLargoOcupaVariasPaginas() throws IOException {
        List<MayorMovimiento> muchos = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            muchos.add(movimiento(fecha(2026, 3, 1), "2025/2026", "1.00", null, "1.00"));
        }
        byte[] pdf = generator.generate(new Mayor(fecha(2026, 3, 1), fecha(2026, 3, 31),
                List.of(cuenta("10101010001", "Caja", muchos))));

        PdfReader reader = new PdfReader(pdf);
        try {
            assertThat(reader.getNumberOfPages()).isGreaterThan(1);
        } finally {
            reader.close();
        }
        String t = texto(pdf);
        assertThat(t).contains("Página 1", "Página 2");
        assertThat(t.split("Concepto", -1).length - 1).isGreaterThan(1); // encabezado repetido
    }
}
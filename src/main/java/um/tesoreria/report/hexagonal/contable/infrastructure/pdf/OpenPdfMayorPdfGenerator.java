package um.tesoreria.report.hexagonal.contable.infrastructure.pdf;

import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.Mayor;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorCuenta;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorMovimiento;
import um.tesoreria.report.hexagonal.contable.domain.model.MayorSaldoInicial;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.MayorPdfGenerator;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * PDF del mayor con OpenPDF, con el aspecto del listado «Mayor con Valores» del sistema anterior: A4 apaisado, sin
 * grillas, una cuenta por hoja (o más si tiene muchos movimientos) con fecha de emisión, título, cuenta, período y
 * saldo inicial arriba; movimientos con el saldo en negrita; totales y saldo final al pie de la cuenta.
 * Escribe los importes tal como llegan del core, con formato es-AR, y no recalcula nada. No muestra el ejercicio de
 * cada línea (el asiento puede repetirse entre ejercicios si el período abarca más de uno).
 */
@Component
@Slf4j
public class OpenPdfMayorPdfGenerator implements MayorPdfGenerator {

    static final String TITULO = "Mayor con Valores";
    static final String SALDO_INICIAL = "Saldo Inicial";
    static final String TOTALES = "Totales";
    static final String SALDO_FINAL = "Saldo final";
    static final String SIN_MOVIMIENTOS = "Sin movimientos en el período.";
    static final String SIN_CUENTAS = "No hay cuentas para mostrar.";
    static final String[] ENCABEZADOS = {"Asiento", "Comp", "OP", "Valor", "Numero", "Importe", "Debe",
            "Haber", "Saldo", "Concepto"};
    // Anchos relativos de las 10 columnas (suman 100).
    private static final float[] ANCHOS = {9.5f, 7.5f, 6f, 9f, 8f, 9f, 9.5f, 9.5f, 10f, 22f};
    private static final int COL_DEBE = 6;
    private static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_ASIENTO = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final Locale ES_AR = Locale.forLanguageTag("es-AR");

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font F_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font F_TEXTO_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font F_ENCABEZADO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font F_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font F_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font F_PIE = FontFactory.getFont(FontFactory.HELVETICA, 8);

    @Override
    public byte[] generate(Mayor mayor) {
        log.debug("Processing OpenPdfMayorPdfGenerator.generate");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 28, 40);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, output);
            writer.setPageEvent(new NumeroDePagina());
            document.open();

            List<MayorCuenta> cuentas = mayor.cuentas() == null ? List.of() : mayor.cuentas();
            if (cuentas.isEmpty()) {
                document.add(encabezadoGeneral());
                document.add(new Paragraph(SIN_CUENTAS, F_TEXTO));
            }
            boolean primera = true;
            for (MayorCuenta cuenta : cuentas) {
                if (!primera) {
                    document.newPage(); // una cuenta por hoja
                }
                primera = false;
                document.add(encabezadoGeneral());
                document.add(datosDeCuenta(mayor, cuenta));
                document.add(tabla(cuenta));
            }
            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del mayor", e);
        }
    }

    /** Fecha de emisión a la derecha y título centrado. */
    private static Paragraph encabezadoGeneral() {
        Paragraph emision = new Paragraph(FECHA_LARGA.format(LocalDate.now()), F_TEXTO_NEGRITA);
        emision.setAlignment(Element.ALIGN_RIGHT);
        Paragraph titulo = new Paragraph(TITULO, F_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(10);
        Paragraph bloque = new Paragraph();
        bloque.add(emision);
        bloque.add(titulo);
        return bloque;
    }

    /** Cuenta a la izquierda; período y saldo inicial a la derecha. */
    private static PdfPTable datosDeCuenta(Mayor mayor, MayorCuenta cuenta) {
        PdfPTable tabla = new PdfPTable(new float[] {55, 45});
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(8);

        Phrase laCuenta = new Phrase();
        laCuenta.add(new Chunk("Cuenta: ", F_TEXTO));
        laCuenta.add(new Chunk(nz(cuenta.nombre()), F_TEXTO_NEGRITA));
        laCuenta.add(new Chunk(" (" + numero(cuenta) + ")", F_TEXTO));
        tabla.addCell(sinBorde(laCuenta, Element.ALIGN_LEFT));

        Phrase periodo = new Phrase();
        periodo.add(new Chunk("Período : ", F_TEXTO));
        periodo.add(new Chunk(fecha(mayor.desde()) + " - " + fecha(mayor.hasta()), F_TEXTO_NEGRITA));
        tabla.addCell(sinBorde(periodo, Element.ALIGN_RIGHT));

        tabla.addCell(sinBorde(new Phrase("", F_TEXTO), Element.ALIGN_LEFT));
        MayorSaldoInicial inicial = cuenta.saldoInicial();
        Phrase saldoInicial = new Phrase();
        saldoInicial.add(new Chunk(SALDO_INICIAL + " : ", F_TEXTO));
        saldoInicial.add(new Chunk(importeTexto(inicial == null ? null : inicial.saldo()), F_TEXTO_NEGRITA));
        tabla.addCell(sinBorde(saldoInicial, Element.ALIGN_RIGHT));
        return tabla;
    }

    private PdfPTable tabla(MayorCuenta cuenta) {
        PdfPTable tabla = new PdfPTable(ANCHOS);
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1); // se repite el encabezado al cortar la cuenta entre hojas
        for (int i = 0; i < ENCABEZADOS.length; i++) {
            PdfPCell cell = celda(ENCABEZADOS[i], F_ENCABEZADO, i >= COL_DEBE && i <= COL_DEBE + 2
                    ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT, Rectangle.BOTTOM);
            tabla.addCell(cell);
        }

        List<MayorMovimiento> movimientos = cuenta.movimientos() == null ? List.of() : cuenta.movimientos();
        if (movimientos.isEmpty()) {
            PdfPCell vacio = celda(SIN_MOVIMIENTOS, F_CELDA, Element.ALIGN_LEFT, Rectangle.NO_BORDER);
            vacio.setColspan(ENCABEZADOS.length);
            tabla.addCell(vacio);
        }
        for (MayorMovimiento m : movimientos) {
            tabla.addCell(texto(asiento(m)));
            tabla.addCell(texto(nz(m.comprobante())));
            tabla.addCell(texto(nz(m.ordenPagoValor()))); // «prefijo/número» de la orden de pago del valor (ej. 15/84)
            tabla.addCell(texto(nz(m.valor())));
            tabla.addCell(texto(m.numeroValor() == null ? "" : m.numeroValor().toString()));
            tabla.addCell(importe(m.importeValor(), F_CELDA, Rectangle.NO_BORDER));
            tabla.addCell(importe(m.debe(), F_CELDA, Rectangle.NO_BORDER));
            tabla.addCell(importe(m.haber(), F_CELDA, Rectangle.NO_BORDER));
            tabla.addCell(importe(m.saldo(), F_NEGRITA, Rectangle.NO_BORDER));
            tabla.addCell(texto(nz(m.concepto())));
        }

        tabla.addCell(etiqueta(TOTALES, COL_DEBE, Rectangle.TOP));
        tabla.addCell(importe(cuenta.totalDebe(), F_NEGRITA, Rectangle.TOP));
        tabla.addCell(importe(cuenta.totalHaber(), F_NEGRITA, Rectangle.TOP));
        tabla.addCell(etiqueta("", 2, Rectangle.TOP));

        tabla.addCell(etiqueta(SALDO_FINAL, COL_DEBE + 2, Rectangle.NO_BORDER));
        tabla.addCell(importe(cuenta.saldoFinal(), F_NEGRITA, Rectangle.NO_BORDER));
        tabla.addCell(etiqueta("", 1, Rectangle.NO_BORDER));
        return tabla;
    }

    private static PdfPCell texto(String valor) {
        return celda(valor, F_CELDA, Element.ALIGN_LEFT, Rectangle.NO_BORDER);
    }

    private static PdfPCell etiqueta(String texto, int columnas, int borde) {
        PdfPCell cell = celda(texto, F_NEGRITA, Element.ALIGN_LEFT, borde);
        cell.setColspan(columnas);
        return cell;
    }

    private static PdfPCell importe(BigDecimal valor, Font fuente, int borde) {
        return celda(importeTexto(valor), fuente, Element.ALIGN_RIGHT, borde);
    }

    private static PdfPCell sinBorde(Phrase frase, int alineacion) {
        PdfPCell cell = new PdfPCell(frase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alineacion);
        cell.setPadding(1);
        return cell;
    }

    private static PdfPCell celda(String texto, Font fuente, int alineacion, int borde) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuente));
        cell.setHorizontalAlignment(alineacion);
        cell.setBorder(borde);
        cell.setBorderWidth(0.5f);
        cell.setPadding(1.5f);
        return cell;
    }

    /** «dd/MM/yy-orden», como el asiento del listado anterior. */
    private static String asiento(MayorMovimiento m) {
        String fecha = m.fechaContable() == null ? ""
                : FECHA_ASIENTO.format(m.fechaContable().withOffsetSameInstant(ZoneOffset.UTC));
        return m.ordenContable() == null ? fecha : fecha + "-" + m.ordenContable();
    }

    static String importeTexto(BigDecimal valor) {
        if (valor == null) {
            return "";
        }
        return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(ES_AR)).format(valor);
    }

    private static String numero(MayorCuenta cuenta) {
        return cuenta.numeroCuenta() == null ? "" : cuenta.numeroCuenta().toPlainString();
    }

    private static String fecha(OffsetDateTime fecha) {
        return fecha == null ? "" : FECHA_LARGA.format(fecha.withOffsetSameInstant(ZoneOffset.UTC));
    }

    private static String nz(String texto) {
        return texto == null ? "" : texto;
    }

    /** Número de página centrado al pie de cada hoja. */
    private static final class NumeroDePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Página " + writer.getPageNumber(), F_PIE),
                    (document.left() + document.right()) / 2, document.bottom() - 20, 0);
        }
    }
}
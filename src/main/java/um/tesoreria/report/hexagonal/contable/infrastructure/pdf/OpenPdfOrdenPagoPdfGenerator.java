package um.tesoreria.report.hexagonal.contable.infrastructure.pdf;

import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
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
import um.tesoreria.report.hexagonal.contable.domain.model.OrdenPago;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.OrdenPagoPdfGenerator;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * PDF de la orden de pago con OpenPDF, con el aspecto de la impresión del sistema anterior: A4 vertical, logo de la
 * UM, número y fecha, datos del proveedor, comprobantes que cancela, valores, traza contable compacta, total a pagar y
 * firmas de Secretaría y Rector. Escribe los importes y totales tal como llegan del core, con formato es-AR, y no
 * recalcula nada. Las tablas repiten su encabezado si continúan en otra hoja.
 */
@Component
@Slf4j
public class OpenPdfOrdenPagoPdfGenerator implements OrdenPagoPdfGenerator {

    static final String LOGO_UM = "/images/marca_um.png";
    static final String TITULO = "Orden de Pago";
    static final String COMPROBANTES = "Comprobantes";
    static final String VALORES = "Valores";
    static final String TRAZA = "Traza contable";
    static final String TOTAL = "Total";
    static final String TOTAL_A_PAGAR = "Total a Pagar";
    static final String FIRMA_SECRETARIA = "Firma de Secretaría";
    static final String FIRMA_RECTOR = "Firma del Rector";

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale ES_AR = Locale.forLanguageTag("es-AR");
    private static final Color GRIS = new Color(0xE6, 0xE6, 0xE6);
    private static final Color ROJO = new Color(0xB0, 0x1E, 0x1E);

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font F_NUMERO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font F_SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font F_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font F_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font F_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font F_ENCABEZADO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font F_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font F_ANULADA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.NORMAL, ROJO);
    private static final Font F_PIE = FontFactory.getFont(FontFactory.HELVETICA, 8);

    @Override
    public byte[] generate(OrdenPago orden) {
        log.debug("Processing OpenPdfOrdenPagoPdfGenerator.generate");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 30, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, output);
            writer.setPageEvent(new NumeroDePagina());
            document.open();

            document.add(encabezado(orden));
            document.add(titulo(orden));
            document.add(proveedor(orden));
            document.add(comprobantes(orden));
            document.add(valores(orden));
            document.add(traza(orden));
            document.add(totalAPagar(orden));
            document.add(firmas());
            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF de la orden de pago", e);
        }
    }

    /** Logo a la izquierda; número de la orden y fecha a la derecha. */
    private static PdfPTable encabezado(OrdenPago orden) throws Exception {
        PdfPTable tabla = new PdfPTable(new float[] {50, 50});
        tabla.setWidthPercentage(100);

        PdfPCell logo = sinBorde(new Phrase(""), Element.ALIGN_LEFT);
        byte[] bytes = logo();
        if (bytes != null) {
            Image imagen = Image.getInstance(bytes);
            imagen.scalePercent(60);
            logo = new PdfPCell(imagen);
            logo.setBorder(Rectangle.NO_BORDER);
        }
        tabla.addCell(logo);

        PdfPCell derecha = new PdfPCell();
        derecha.setBorder(Rectangle.NO_BORDER);
        derecha.addElement(alineado("Nº: " + numero(orden.prefijo(), orden.numeroComprobante()), F_NUMERO,
                Element.ALIGN_RIGHT));
        derecha.addElement(alineado("Fecha: " + fecha(orden.fecha()), F_TEXTO, Element.ALIGN_RIGHT));
        tabla.addCell(derecha);
        tabla.setSpacingAfter(6);
        return tabla;
    }

    private static Paragraph titulo(OrdenPago orden) {
        Paragraph titulo = new Paragraph();
        Paragraph principal = new Paragraph(TITULO, F_TITULO);
        principal.setAlignment(Element.ALIGN_CENTER);
        titulo.add(principal);
        if (orden.fechaAnulacion() != null) {
            Paragraph anulada = new Paragraph("ANULADA el " + fecha(orden.fechaAnulacion()), F_ANULADA);
            anulada.setAlignment(Element.ALIGN_CENTER);
            titulo.add(anulada);
        }
        titulo.setSpacingAfter(10);
        return titulo;
    }

    /** Señor/es y CUIT en la primera fila; domicilio y concepto debajo, a todo el ancho. */
    private static PdfPTable proveedor(OrdenPago orden) {
        PdfPTable tabla = new PdfPTable(new float[] {68, 32});
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(10);
        tabla.addCell(sinBorde(etiquetaValor("Señor/es: ", orden.razonSocial()), Element.ALIGN_LEFT));
        tabla.addCell(sinBorde(etiquetaValor("CUIT: ", orden.cuit()), Element.ALIGN_RIGHT));
        tabla.addCell(completa(etiquetaValor("Domicilio: ", orden.domicilio())));
        tabla.addCell(completa(etiquetaValor("Concepto: ", orden.concepto())));
        return tabla;
    }

    private static PdfPTable comprobantes(OrdenPago orden) {
        PdfPTable tabla = tabla(new float[] {42, 20, 16, 22}, COMPROBANTES,
                "Comprobante", "Número", "Fecha", "Importe");
        for (OrdenPago.Comprobante c : lista(orden.comprobantes())) {
            tabla.addCell(celda(c.descripcion(), Element.ALIGN_LEFT));
            tabla.addCell(celda(numero(c.prefijo(), c.numeroComprobante()), Element.ALIGN_LEFT));
            tabla.addCell(celda(fecha(c.fecha()), Element.ALIGN_LEFT));
            tabla.addCell(celda(importe(c.importe()), Element.ALIGN_RIGHT));
        }
        pieTotal(tabla, 4, 3, TOTAL, orden.totalComprobantes());
        return tabla;
    }

    private static PdfPTable valores(OrdenPago orden) {
        PdfPTable tabla = tabla(new float[] {26, 30, 14, 13, 17}, VALORES,
                "Valor", "Titular", "Número", "Fecha", "Importe");
        for (OrdenPago.Valor v : lista(orden.valores())) {
            tabla.addCell(celda(v.concepto(), Element.ALIGN_LEFT));
            tabla.addCell(celda(v.titular(), Element.ALIGN_LEFT));
            tabla.addCell(celda(v.numero() == null ? "" : String.valueOf(v.numero()), Element.ALIGN_LEFT));
            tabla.addCell(celda(fecha(v.fecha()), Element.ALIGN_LEFT));
            tabla.addCell(celda(importe(v.importe()), Element.ALIGN_RIGHT));
        }
        pieTotal(tabla, 5, 4, TOTAL, orden.totalValores());
        return tabla;
    }

    private static PdfPTable traza(OrdenPago orden) {
        PdfPTable tabla = tabla(new float[] {60, 20, 20}, TRAZA, "Cuenta", "Debe", "Haber");
        for (OrdenPago.Traza t : lista(orden.traza())) {
            tabla.addCell(celda(cuenta(t), Element.ALIGN_LEFT));
            tabla.addCell(celda(importeSiHay(t.debe()), Element.ALIGN_RIGHT));
            tabla.addCell(celda(importeSiHay(t.haber()), Element.ALIGN_RIGHT));
        }
        PdfPCell total = celdaNegrita(TOTAL, Element.ALIGN_RIGHT);
        tabla.addCell(total);
        PdfPCell debe = celdaNegrita(importe(orden.totalDebe()), Element.ALIGN_RIGHT);
        tabla.addCell(debe);
        PdfPCell haber = celdaNegrita(importe(orden.totalHaber()), Element.ALIGN_RIGHT);
        tabla.addCell(haber);
        return tabla;
    }

    /** Total a pagar destacado, a la derecha. */
    private static PdfPTable totalAPagar(OrdenPago orden) {
        PdfPTable tabla = new PdfPTable(new float[] {55, 45});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(4);
        tabla.setSpacingAfter(30);
        tabla.addCell(sinBorde(new Phrase(""), Element.ALIGN_LEFT));
        PdfPCell caja = new PdfPCell(new Phrase(TOTAL_A_PAGAR + ":  " + importe(orden.totalValores()), F_TOTAL));
        caja.setHorizontalAlignment(Element.ALIGN_RIGHT);
        caja.setBackgroundColor(GRIS);
        caja.setPadding(6);
        caja.setBorderWidth(0.8f);
        tabla.addCell(caja);
        return tabla;
    }

    /** Firmas de Secretaría y Rector; el bloque no se parte entre hojas. */
    private static PdfPTable firmas() {
        PdfPTable tabla = new PdfPTable(new float[] {40, 20, 40});
        tabla.setWidthPercentage(100);
        tabla.setKeepTogether(true);
        tabla.setSpacingBefore(40);
        for (String etiqueta : new String[] {FIRMA_SECRETARIA, null, FIRMA_RECTOR}) {
            PdfPCell cell = new PdfPCell(new Phrase(etiqueta == null ? "" : etiqueta, F_TEXTO));
            cell.setBorder(etiqueta == null ? Rectangle.NO_BORDER : Rectangle.TOP);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(4);
            tabla.addCell(cell);
        }
        return tabla;
    }

    // ---- helpers de tabla y formato ----

    /** Tabla con título de sección, encabezado gris repetible y sin grilla. */
    private static PdfPTable tabla(float[] anchos, String seccion, String... encabezados) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(10);
        PdfPCell titulo = new PdfPCell(new Phrase(seccion, F_SECCION));
        titulo.setColspan(anchos.length);
        titulo.setBorder(Rectangle.NO_BORDER);
        titulo.setPaddingBottom(3);
        tabla.addCell(titulo);
        for (int i = 0; i < encabezados.length; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(encabezados[i], F_ENCABEZADO));
            cell.setBackgroundColor(GRIS);
            cell.setBorder(Rectangle.BOTTOM);
            cell.setBorderWidth(0.5f);
            cell.setPadding(3);
            cell.setHorizontalAlignment(esNumerica(anchos.length, i) ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);
            tabla.addCell(cell);
        }
        tabla.setHeaderRows(2); // título de sección + encabezado
        return tabla;
    }

    /** La última columna es siempre de importes; en la traza también la del medio (debe). */
    private static boolean esNumerica(int columnas, int indice) {
        return indice == columnas - 1 || (columnas == 3 && indice == 1);
    }

    /** Fila de total: etiqueta a la derecha ocupando las columnas previas al importe. */
    private static void pieTotal(PdfPTable tabla, int columnas, int antesDelImporte, String etiqueta, BigDecimal total) {
        PdfPCell texto = celdaNegrita(etiqueta, Element.ALIGN_RIGHT);
        texto.setColspan(antesDelImporte);
        tabla.addCell(texto);
        PdfPCell valor = celdaNegrita(importe(total), Element.ALIGN_RIGHT);
        tabla.addCell(valor);
    }

    private static PdfPCell celda(String texto, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(nz(texto), F_CELDA));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3);
        cell.setHorizontalAlignment(alineacion);
        return cell;
    }

    private static PdfPCell celdaNegrita(String texto, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(nz(texto), F_NEGRITA));
        cell.setBorder(Rectangle.TOP);
        cell.setBorderWidthTop(0.5f);
        cell.setPadding(3);
        cell.setHorizontalAlignment(alineacion);
        return cell;
    }

    private static PdfPCell sinBorde(Phrase frase, int alineacion) {
        PdfPCell cell = new PdfPCell(frase);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alineacion);
        cell.setPaddingBottom(3);
        return cell;
    }

    private static PdfPCell completa(Phrase frase) {
        PdfPCell cell = sinBorde(frase, Element.ALIGN_LEFT);
        cell.setColspan(2);
        return cell;
    }

    private static Phrase etiquetaValor(String etiqueta, String valor) {
        Phrase frase = new Phrase();
        frase.add(new Chunk(etiqueta, F_TEXTO));
        frase.add(new Chunk(nz(valor), F_NEGRITA));
        return frase;
    }

    private static Paragraph alineado(String texto, Font font, int alineacion) {
        Paragraph p = new Paragraph(texto, font);
        p.setAlignment(alineacion);
        return p;
    }

    private static String cuenta(OrdenPago.Traza traza) {
        String numero = traza.numeroCuenta() == null ? "" : traza.numeroCuenta().toPlainString();
        return (numero + " " + nz(traza.nombreCuenta())).trim();
    }

    /** «15 - 3684», sin ceros a la izquierda; vacío si falta alguno de los dos. */
    static String numero(Integer prefijo, Long numero) {
        return prefijo == null || numero == null ? "" : prefijo + " - " + numero;
    }

    private static String fecha(OffsetDateTime fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    /** Importe es-AR con dos decimales (`560.190,17`); vacío si no viene. */
    static String importe(BigDecimal importe) {
        if (importe == null) {
            return "";
        }
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(ES_AR));
        return formato.format(importe);
    }

    /** En la traza una cuenta está en el debe o en el haber: el lado en cero queda en blanco. */
    private static String importeSiHay(BigDecimal importe) {
        return importe == null || importe.signum() == 0 ? "" : importe(importe);
    }

    private static String nz(String texto) {
        return texto == null ? "" : texto;
    }

    private static <T> List<T> lista(List<T> items) {
        return items == null ? List.of() : items;
    }

    private static byte[] logo() {
        try (InputStream in = OpenPdfOrdenPagoPdfGenerator.class.getResourceAsStream(LOGO_UM)) {
            return in == null ? null : in.readAllBytes();
        } catch (Exception e) {
            log.warn("No se pudo leer el logo {}", LOGO_UM);
            return null;
        }
    }

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
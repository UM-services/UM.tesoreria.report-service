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
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.contable.domain.model.Compra;
import um.tesoreria.report.hexagonal.contable.domain.ports.out.CompraPdfGenerator;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF de factura o nota de crédito de compras («Ingreso de Comprobante de Compras» del sistema anterior): A4 vertical,
 * logo, tipo y número, fecha, proveedor y total, concepto, detalle de artículos y asiento contable. Escribe los
 * importes tal como llegan, con su signo, en formato es-AR; no recalcula nada.
 */
@Component
@Slf4j
public class OpenPdfCompraPdfGenerator implements CompraPdfGenerator {

    static final String LOGO_UM = "/images/marca_um.png";
    static final String TITULO = "Ingreso de Comprobante de Compras";

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color GRIS = new Color(0xE6, 0xE6, 0xE6);
    private static final Color ROJO = new Color(0xB0, 0x1E, 0x1E);

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font F_NUMERO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font F_SECCION = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font F_TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font F_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font F_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font F_ENCABEZADO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font F_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font F_ANULADO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.NORMAL, ROJO);

    @Override
    public byte[] generate(Compra compra) {
        log.debug("Processing OpenPdfCompraPdfGenerator.generate");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 30, 40);
        try {
            PdfWriter.getInstance(document, output);
            document.open();
            document.add(encabezado(compra));
            document.add(titulo(compra));
            document.add(proveedor(compra));
            document.add(detalle(compra));
            document.add(asiento(compra));
            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF del comprobante de compras", e);
        }
    }

    /** Logo a la izquierda; tipo y número y fecha a la derecha. */
    private static PdfPTable encabezado(Compra compra) throws Exception {
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
        derecha.addElement(alineado((nz(compra.comprobanteDescripcion()) + " " + nz(compra.numero())).trim(),
                F_NUMERO, Element.ALIGN_RIGHT));
        derecha.addElement(alineado("Fecha: " + fecha(compra.fecha()), F_TEXTO, Element.ALIGN_RIGHT));
        tabla.addCell(derecha);
        tabla.setSpacingAfter(6);
        return tabla;
    }

    private static Paragraph titulo(Compra compra) {
        Paragraph titulo = new Paragraph();
        titulo.add(alineado(TITULO, F_TITULO, Element.ALIGN_CENTER));
        if (compra.fechaAnulacion() != null) {
            titulo.add(alineado("ANULADO el " + fecha(compra.fechaAnulacion()), F_ANULADO, Element.ALIGN_CENTER));
        }
        titulo.setSpacingAfter(10);
        return titulo;
    }

    /** Proveedor y CUIT a la izquierda, total grande a la derecha; el concepto debajo, a todo el ancho. */
    private static PdfPTable proveedor(Compra compra) {
        PdfPTable tabla = new PdfPTable(new float[] {62, 38});
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(10);

        PdfPCell izquierda = new PdfPCell();
        izquierda.setBorder(Rectangle.NO_BORDER);
        izquierda.addElement(etiquetaValor("Proveedor: ", compra.razonSocial()));
        izquierda.addElement(etiquetaValor("CUIT: ", compra.cuit()));
        tabla.addCell(izquierda);

        PdfPCell total = new PdfPCell(new Phrase("Total:  " + importe(compra.importe()), F_TOTAL));
        total.setHorizontalAlignment(Element.ALIGN_RIGHT);
        total.setBackgroundColor(GRIS);
        total.setPadding(6);
        total.setBorderWidth(0.8f);
        tabla.addCell(total);

        PdfPCell concepto = sinBorde(etiquetaValor("Concepto: ", compra.concepto()), Element.ALIGN_LEFT);
        concepto.setColspan(2);
        tabla.addCell(concepto);
        return tabla;
    }

    private static PdfPTable detalle(Compra compra) {
        PdfPTable tabla = tabla(new float[] {52, 12, 18, 18}, "Detalle", "Detalle", "Cantidad", "Precio", "Subtotal");
        for (Compra.Detalle d : lista(compra.detalle())) {
            tabla.addCell(celda(descripcion(d), Element.ALIGN_LEFT));
            tabla.addCell(celda(cantidad(d), Element.ALIGN_RIGHT));
            tabla.addCell(celda(importe(d.precioUnitario()), Element.ALIGN_RIGHT));
            tabla.addCell(celda(importe(d.precioFinal()), Element.ALIGN_RIGHT));
        }
        PdfPCell texto = celdaNegrita("Total", Element.ALIGN_RIGHT);
        texto.setColspan(3);
        tabla.addCell(texto);
        tabla.addCell(celdaNegrita(importe(compra.importe()), Element.ALIGN_RIGHT));
        return tabla;
    }

    private static PdfPTable asiento(Compra compra) {
        String seccion = "Asiento: " + fecha(compra.fechaContable())
                + (compra.ordenContable() == null ? "" : " - " + compra.ordenContable());
        PdfPTable tabla = tabla(new float[] {60, 20, 20}, seccion.trim(), "Cuenta", "Debe", "Haber");
        for (Compra.Asiento a : lista(compra.asiento())) {
            tabla.addCell(celda(cuenta(a), Element.ALIGN_LEFT));
            tabla.addCell(celda(importeSiHay(a.debe()), Element.ALIGN_RIGHT));
            tabla.addCell(celda(importeSiHay(a.haber()), Element.ALIGN_RIGHT));
        }
        tabla.addCell(celdaNegrita("Total", Element.ALIGN_RIGHT));
        tabla.addCell(celdaNegrita(importe(compra.totalDebe()), Element.ALIGN_RIGHT));
        tabla.addCell(celdaNegrita(importe(compra.totalHaber()), Element.ALIGN_RIGHT));
        return tabla;
    }

    // ---- helpers ----

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
            cell.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            tabla.addCell(cell);
        }
        tabla.setHeaderRows(2); // título de sección + encabezado, se repiten si sigue en otra hoja
        return tabla;
    }

    /** Nombre del artículo; si no tiene, el texto libre del renglón. */
    private static String descripcion(Compra.Detalle d) {
        String articulo = nz(d.articulo()).trim();
        return articulo.isEmpty() ? nz(d.concepto()) : articulo;
    }

    private static String cantidad(Compra.Detalle d) {
        return d.cantidad() == null ? "" : importe(d.cantidad());
    }

    private static String cuenta(Compra.Asiento a) {
        String numero = a.numeroCuenta() == null ? "" : a.numeroCuenta().toPlainString();
        return (numero + " " + nz(a.nombreCuenta())).trim();
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

    private static String fecha(OffsetDateTime fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    /** Mismo formato es-AR que la orden de pago; conserva el signo. */
    private static String importe(java.math.BigDecimal importe) {
        return OpenPdfOrdenPagoPdfGenerator.importe(importe);
    }

    /** En el asiento una cuenta está en el debe o en el haber: el lado en cero queda en blanco. */
    private static String importeSiHay(java.math.BigDecimal importe) {
        return importe == null || importe.signum() == 0 ? "" : importe(importe);
    }

    private static String nz(String texto) {
        return texto == null ? "" : texto;
    }

    private static <T> List<T> lista(List<T> items) {
        return items == null ? List.of() : items;
    }

    private static byte[] logo() {
        try (InputStream in = OpenPdfCompraPdfGenerator.class.getResourceAsStream(LOGO_UM)) {
            return in == null ? null : in.readAllBytes();
        } catch (Exception e) {
            log.warn("No se pudo leer el logo {}", LOGO_UM);
            return null;
        }
    }
}
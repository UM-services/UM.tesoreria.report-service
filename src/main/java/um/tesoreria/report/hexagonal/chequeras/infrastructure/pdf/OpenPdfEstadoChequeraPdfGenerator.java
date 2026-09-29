package um.tesoreria.report.hexagonal.chequeras.infrastructure.pdf;

import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.EstadoChequera;
import um.tesoreria.report.hexagonal.chequeras.domain.model.CuotaEstado;
import um.tesoreria.report.hexagonal.chequeras.domain.model.DebitoEstado;
import um.tesoreria.report.hexagonal.chequeras.domain.model.ProductoEstado;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.EstadoChequeraPdfGenerator;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Dibuja el PDF "Estado de Chequera" con OpenPDF.
 * <ul>
 *   <li>Hoja 1: encabezado, alternativa y un bloque por producto (cuotas, subtotales).</li>
 *   <li>Hoja 2: el mismo encabezado y la adhesión al débito automático.</li>
 * </ul>
 * Los logos se cargan del classpath ({@code /images/marca_um.png} y {@code /images/marca_etec.png}),
 * no de una ruta relativa al directorio de trabajo, para que funcionen igual dentro del jar/contenedor.
 */
@Component
public class OpenPdfEstadoChequeraPdfGenerator implements EstadoChequeraPdfGenerator {

    static final String LOGO_UM = "/images/marca_um.png";
    static final String LOGO_ETEC = "/images/marca_etec.png";
    private static final int FACULTAD_ETEC_ID = 15;

    // Paleta: acento institucional, gris para etiquetas, gris claro para líneas finas y un
    // fondo muy suave para las filas alternadas.
    private static final Color COLOR_ACENTO = new Color(30, 58, 95);
    private static final Color COLOR_ETIQUETA = new Color(110, 110, 110);
    private static final Color COLOR_LINEA = new Color(210, 210, 215);
    private static final Color COLOR_FILA_ALTERNA = new Color(247, 247, 250);

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String SIN_DATO = "—";

    @Override
    public void generate(EstadoChequera estado, OutputStream out) {
        DecimalFormat importes = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        byte[] logo = loadLogo(estado.facultadId());

        Document document = new Document(new Rectangle(PageSize.A4));
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.setMargins(40, 25, 18, 14);
            document.open();

            // --- Hoja 1: encabezado + cuotas por producto ---
            writeEncabezado(document, estado, logo, 1);

            Paragraph alternativa = new Paragraph("Alternativa: " + numero(estado.alternativaId()),
                    new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_ACENTO));
            alternativa.setAlignment(Element.ALIGN_CENTER);
            document.add(alternativa);
            document.add(espacio(5));

            for (ProductoEstado producto : estado.productos()) {
                writeProducto(document, producto, importes);
            }

            // --- Hoja 2: mismo encabezado + adhesión al débito automático ---
            document.newPage();
            writeEncabezado(document, estado, logo, writer.getPageNumber());
            writeDebitos(document, estado.debitos(), importes);

            document.close();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo dibujar el estado de chequera", e);
        }
    }

    // ------------------------------------------------------------------ encabezado

    private void writeEncabezado(Document document, EstadoChequera estado, byte[] logo, int hoja) throws IOException {
        PdfPTable headerTable = new PdfPTable(new float[]{1, 1});
        headerTable.setWidthPercentage(100);

        Image image = Image.getInstance(logo);
        image.scalePercent(80);
        PdfPCell cell = new PdfPCell(image);
        cell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cell);

        cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(derecha("UNIVERSIDAD DE MENDOZA", new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_ACENTO)));
        cell.addElement(derecha(texto(estado.facultadNombre()), new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_ACENTO)));
        cell.addElement(derecha("Hoja: " + hoja, new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_ETIQUETA)));
        headerTable.addCell(cell);
        document.add(headerTable);

        document.add(espacio(4));

        Paragraph titulo = new Paragraph("Estado de Chequera", new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_ACENTO));
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);
        document.add(espacio(6));

        document.add(datoEncabezado("Titular: (" + plano(estado.personaId()) + ") ",
                texto(estado.personaApellido()) + ", " + texto(estado.personaNombre())));
        document.add(datoEncabezado("Tipo Chequera: ", texto(estado.tipoChequeraNombre())));
        document.add(datoEncabezado("Tipo Arancel: ", texto(estado.arancelTipoDescripcion())));
        document.add(datoEncabezado("Ciclo Lectivo: ", texto(estado.lectivoNombre())));
        document.add(datoEncabezado("Porcentaje de beca: ", porcentaje(estado.becaPorcentaje())));
        document.add(datoEncabezado("Tipo Impresion: ", texto(estado.tipoImpresionNombre())));

        Paragraph chequera = datoEncabezado("Chequera: ",
                numero(estado.facultadId()) + "/" + numero(estado.tipoChequeraId()) + "/" + numero(estado.chequeraSerieId()));
        chequera.setAlignment(Element.ALIGN_RIGHT);
        document.add(chequera);

        document.add(espacio(5));
        Paragraph leyenda = new Paragraph("NO VALIDO COMO COMPROBANTE DE PAGO",
                new Font(Font.HELVETICA, 9, Font.BOLDITALIC, COLOR_ETIQUETA));
        leyenda.setAlignment(Element.ALIGN_CENTER);
        document.add(leyenda);
        document.add(espacio(5));
    }

    private Paragraph datoEncabezado(String etiqueta, String valor) {
        Paragraph paragraph = new Paragraph(new Phrase(etiqueta, new Font(Font.HELVETICA, 11, Font.NORMAL, COLOR_ETIQUETA)));
        paragraph.add(new Phrase(valor, new Font(Font.HELVETICA, 11, Font.BOLD)));
        return paragraph;
    }

    // ------------------------------------------------------------------ hoja 1: productos

    private void writeProducto(Document document, ProductoEstado producto, DecimalFormat importes) {
        BigDecimal total = cero(producto.total());
        BigDecimal pagado = cero(producto.pagado());

        // "Producto: X" a la izquierda y "Subtotal Producto: $Y" a la derecha, en la misma fila.
        PdfPTable productoHeader = new PdfPTable(new float[]{1, 1});
        productoHeader.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase("Producto: " + texto(producto.nombre()),
                new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_ACENTO)));
        cell.setBorder(Rectangle.NO_BORDER);
        productoHeader.addCell(cell);
        cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(subtotal("Subtotal Producto: ", importes.format(total)));
        productoHeader.addCell(cell);
        document.add(productoHeader);
        document.add(espacio(3));

        PdfPTable table = new PdfPTable(new float[]{1.8f, 1.4f, 1.25f, 1.1f, 1.1f, 1.55f});
        table.setWidthPercentage(100);

        Font fontColHeader = new Font(Font.HELVETICA, 7, Font.BOLD, COLOR_ETIQUETA);
        String[] headers = {"Cuota", "Período", "Primer vencimiento", "A Pagar", "Fecha Pago", "Pagado"};
        int[] alineacion = {Element.ALIGN_LEFT, Element.ALIGN_LEFT, Element.ALIGN_LEFT,
                Element.ALIGN_RIGHT, Element.ALIGN_CENTER, Element.ALIGN_RIGHT};
        for (int h = 0; h < headers.length; h++) {
            PdfPCell header = new PdfPCell(new Phrase(headers[h], fontColHeader));
            header.setHorizontalAlignment(alineacion[h]);
            header.setVerticalAlignment(Element.ALIGN_TOP);
            header.setBorder(Rectangle.BOTTOM);
            header.setBorderColor(COLOR_LINEA);
            header.setPaddingBottom(2f);
            table.addCell(header);
        }

        List<CuotaEstado> cuotas = producto.cuotas();
        for (int i = 0; i < cuotas.size(); i++) {
            writeCuota(table, producto, cuotas.get(i), i, cuotas.size(), importes);
        }
        document.add(table);

        document.add(subtotal("Subtotal Pagado: ", importes.format(pagado)));
        document.add(subtotal("Subtotal Deuda: ", importes.format(total.subtract(pagado))));

        // Espacio entre el cierre de este producto y el siguiente (o la hoja 2)
        document.add(espacio(6));
    }

    private void writeCuota(PdfPTable table, ProductoEstado producto, CuotaEstado cuota, int index, int count,
                            DecimalFormat importes) {
        // Última fila: además del borde superior, borde inferior para cerrar el bloque del producto.
        int borde = (index == count - 1) ? (Rectangle.TOP | Rectangle.BOTTOM) : Rectangle.TOP;
        // Filas alternadas con un fondo muy suave.
        Color fondo = (index % 2 == 0) ? COLOR_FILA_ALTERNA : Color.WHITE;
        Font bold8 = new Font(Font.HELVETICA, 8, Font.BOLD);

        // 1: "<título>: <cuota>/<total de cuotas>"
        table.addCell(celda(new PdfPCell(new Phrase(
                        texto(producto.tituloCuota()) + ": " + numero(cuota.cuotaId()) + "/" + numero(producto.totalCuotas()), bold8)),
                borde, fondo, Element.ALIGN_LEFT));

        // 2: "Periodo: mes/año (orden del pago)". Todas las celdas se arman con un Phrase (no con
        // addElement(Paragraph)) para que el texto de toda la fila quede a la misma altura.
        String orden = numero(cuota.ordenPago());
        Phrase periodo = new Phrase();
        periodo.add(new Chunk("Periodo: ", new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_ETIQUETA)));
        periodo.add(new Chunk(numero(cuota.mes()) + "/" + numero(cuota.anho()) + " (" + orden + ")", bold8));
        table.addCell(celda(new PdfPCell(periodo), borde, fondo, Element.ALIGN_LEFT));

        // 3: primer vencimiento (fecha contractual, no un instante de pago)
        table.addCell(celda(new PdfPCell(new Phrase(fecha(cuota.primerVencimiento(), SIN_DATO), bold8)),
                borde, fondo, Element.ALIGN_LEFT));

        // 4: A Pagar
        table.addCell(celda(new PdfPCell(new Phrase(importes.format(cero(cuota.importe())), bold8)),
                borde, fondo, Element.ALIGN_RIGHT));

        // 5: Fecha Pago (solo la fecha, centrada bajo su encabezado)
        table.addCell(celda(new PdfPCell(new Phrase(fecha(cuota.fechaPago(), ""), bold8)),
                borde, fondo, Element.ALIGN_CENTER));

        // 6: Pagado, con la referencia del pago (archivo del banco / "MercadoPago") debajo
        Phrase pagado = new Phrase();
        pagado.add(new Chunk(cuota.importePagado() != null ? importes.format(cuota.importePagado()) : SIN_DATO, bold8));
        if (cuota.referenciaPago() != null && !cuota.referenciaPago().isEmpty()) {
            pagado.add(Chunk.NEWLINE);
            pagado.add(new Chunk(cuota.referenciaPago(), new Font(Font.HELVETICA, 6, Font.NORMAL, COLOR_ETIQUETA)));
        }
        table.addCell(celda(new PdfPCell(pagado), borde, fondo, Element.ALIGN_RIGHT));
    }

    private PdfPCell celda(PdfPCell cell, int borde, Color fondo, int alineacionHorizontal) {
        cell.setHorizontalAlignment(alineacionHorizontal);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setBorder(borde);
        cell.setBorderColor(COLOR_LINEA);
        cell.setBackgroundColor(fondo);
        cell.setPadding(3f);
        return cell;
    }

    private Paragraph subtotal(String etiqueta, String valor) {
        Paragraph paragraph = new Paragraph(new Phrase(etiqueta, new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_ETIQUETA)));
        paragraph.add(new Phrase(valor, new Font(Font.HELVETICA, 9, Font.BOLD)));
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        return paragraph;
    }

    // ------------------------------------------------------------------ hoja 2: débito automático

    private void writeDebitos(Document document, List<DebitoEstado> debitos, DecimalFormat importes) {
        document.add(new Paragraph("Adhesión de chequera al Débito Automático",
                new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_ACENTO)));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(new float[]{0.6f, 1.2f, 1.3f, 2.2f, 1.3f, 0.7f, 1.7f});
        table.setWidthPercentage(100);

        String[] headers = {"Cuo", "Importe", "Fecha Vto", "CBU", "Envío al Banco", "Rech", "Motivo de Rechazo"};
        for (String h : headers) {
            PdfPCell header = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 7, Font.BOLD, COLOR_ETIQUETA)));
            header.setBorder(Rectangle.BOTTOM);
            header.setBorderColor(COLOR_LINEA);
            header.setPaddingBottom(3f);
            table.addCell(header);
        }

        for (int i = 0; i < debitos.size(); i++) {
            DebitoEstado debito = debitos.get(i);
            Color fondo = (i % 2 == 0) ? COLOR_FILA_ALTERNA : Color.WHITE;
            String[] valores = {
                    numero(debito.cuotaId()),
                    importes.format(cero(debito.importe())),
                    fecha(debito.fechaVencimiento(), ""),
                    texto(debito.cbu()),
                    debito.fechaEnvio() != null ? debito.fechaEnvio().format(FECHA_HORA) : "",
                    debito.rechazado() ? "*" : "",
                    texto(debito.motivoRechazo())
            };
            for (String valor : valores) {
                PdfPCell cell = new PdfPCell(new Phrase(valor, new Font(Font.HELVETICA, 8)));
                cell.setBorder(Rectangle.TOP);
                cell.setBorderColor(COLOR_LINEA);
                cell.setBackgroundColor(fondo);
                cell.setPadding(4f);
                table.addCell(cell);
            }
        }
        document.add(table);
    }

    // ------------------------------------------------------------------ utilidades

    private static byte[] loadLogo(Integer facultadId) {
        String resource = (facultadId != null && facultadId == FACULTAD_ETEC_ID) ? LOGO_ETEC : LOGO_UM;
        try (InputStream in = OpenPdfEstadoChequeraPdfGenerator.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el logo en el classpath: " + resource);
            }
            return in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el logo " + resource, e);
        }
    }

    private static Paragraph derecha(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        return paragraph;
    }

    private static Paragraph espacio(float size) {
        return new Paragraph(" ", new Font(Font.HELVETICA, size));
    }

    private static String texto(String value) {
        return value == null ? "" : value;
    }

    private static String numero(Number value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String plano(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    private static BigDecimal cero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String fecha(LocalDate value, String cuandoFalta) {
        return value == null ? cuandoFalta : value.format(FECHA);
    }

    private static String porcentaje(BigDecimal fraccion) {
        return cero(fraccion).movePointRight(2).stripTrailingZeros().toPlainString() + "%";
    }
}
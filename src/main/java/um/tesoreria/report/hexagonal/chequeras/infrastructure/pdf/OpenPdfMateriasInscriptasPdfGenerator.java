package um.tesoreria.report.hexagonal.chequeras.infrastructure.pdf;

import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import um.tesoreria.report.hexagonal.chequeras.domain.model.*;
import um.tesoreria.report.hexagonal.chequeras.domain.ports.out.MateriasInscriptasPdfGenerator;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/** PDF independiente. Conserva el orden del core y repite solo las columnas al continuar una tabla. */
@Component
public class OpenPdfMateriasInscriptasPdfGenerator implements MateriasInscriptasPdfGenerator {
    private static final Color ACENTO = new Color(30, 58, 95);
    private static final Color ETIQUETA = new Color(110, 110, 110);
    private static final Color LINEA = new Color(210, 210, 215);
    private static final Color FONDO = new Color(247, 247, 250);

    @Override
    public void generate(MateriasInscriptas reporte, OutputStream out) {
        Document document = new Document(PageSize.A4, 40, 25, 18, 25);
        RuntimeException failure = null;
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            writeEncabezado(document, reporte.encabezado());
            if (reporte.materias().isEmpty()) {
                addText(document, paragraph("Esta chequera no es de grado: no corresponde informar materias.", 11, Font.NORMAL));
            }
            for (MateriasEstado bloque : reporte.materias()) {
                writeBloque(document, bloque);
            }
        } catch (IOException e) {
            failure = new UncheckedIOException("No se pudo dibujar el PDF de materias inscriptas", e);
            throw failure;
        } catch (RuntimeException e) {
            failure = e;
            throw e;
        } finally {
            try {
                document.close();
            } catch (RuntimeException closeFailure) {
                if (failure == null) throw closeFailure;
                failure.addSuppressed(closeFailure);
            }
        }
    }

    private void writeEncabezado(Document document, EstadoChequera estado) throws IOException {
        PdfPTable header = new PdfPTable(new float[]{1, 1});
        header.setWidthPercentage(100);
        String logo = Integer.valueOf(15).equals(estado.facultadId())
                ? OpenPdfEstadoChequeraPdfGenerator.LOGO_ETEC : OpenPdfEstadoChequeraPdfGenerator.LOGO_UM;
        try (var input = getClass().getResourceAsStream(logo)) {
            if (input == null) throw new IOException("No se encontró el logo institucional");
            Image image = Image.getInstance(input.readAllBytes());
            image.scalePercent(80);
            PdfPCell cell = new PdfPCell(image);
            cell.setBorder(Rectangle.NO_BORDER);
            header.addCell(cell);
        }
        PdfPCell institucion = new PdfPCell();
        institucion.setBorder(Rectangle.NO_BORDER);
        Paragraph universidad = paragraph("UNIVERSIDAD DE MENDOZA", 16, Font.BOLD);
        universidad.setAlignment(Element.ALIGN_RIGHT);
        institucion.addElement(universidad);
        Paragraph facultad = paragraph(texto(estado.facultadNombre()), 14, Font.BOLD);
        facultad.setAlignment(Element.ALIGN_RIGHT);
        institucion.addElement(facultad);
        Paragraph hoja = new Paragraph("Hoja: 1", new Font(Font.HELVETICA, 9, Font.NORMAL, ETIQUETA));
        hoja.setAlignment(Element.ALIGN_RIGHT);
        institucion.addElement(hoja);
        header.addCell(institucion);
        document.add(header);
        Paragraph titulo = paragraph("Materias inscriptas", 16, Font.BOLD);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingBefore(6);
        titulo.setSpacingAfter(8);
        addText(document, titulo);

        // Mismo lenguaje visual que Estado de Chequera: etiqueta pequeña y valor en tarjeta.
        PdfPTable datos = new PdfPTable(new float[]{1, 1});
        datos.setWidthPercentage(100);
        String documento = estado.personaId() == null ? "—" : estado.personaId().toPlainString();
        datos.addCell(tarjeta("Titular", "(" + documento + ") " + texto(estado.personaApellido())
                + ", " + texto(estado.personaNombre())));
        datos.addCell(tarjeta("Ciclo lectivo", texto(estado.lectivoNombre())));
        document.add(datos);

        Paragraph chequera = new Paragraph(new Phrase("Chequera: ",
                new Font(Font.HELVETICA, 10, Font.NORMAL, ETIQUETA)));
        chequera.add(new Phrase(texto(estado.facultadId()) + "/" + texto(estado.tipoChequeraId())
                + "/" + texto(estado.chequeraSerieId()), new Font(Font.HELVETICA, 10, Font.BOLD, ACENTO)));
        chequera.setAlignment(Element.ALIGN_RIGHT);
        chequera.setSpacingBefore(4);
        chequera.setSpacingAfter(8);
        addText(document, chequera);
    }

    private PdfPCell tarjeta(String etiqueta, String valor) {
        Paragraph contenido = new Paragraph(etiqueta.toUpperCase(java.util.Locale.ROOT),
                new Font(Font.HELVETICA, 7, Font.BOLD, ETIQUETA));
        contenido.add(Chunk.NEWLINE);
        contenido.add(new Chunk(valor, new Font(Font.HELVETICA, 11, Font.BOLD)));
        PdfPCell interior = new PdfPCell();
        interior.addElement(contenido);
        interior.setBackgroundColor(FONDO);
        interior.setBorder(Rectangle.NO_BORDER);
        interior.setPadding(6);
        PdfPTable tarjeta = new PdfPTable(1);
        tarjeta.setWidthPercentage(100);
        tarjeta.addCell(interior);
        PdfPCell exterior = new PdfPCell();
        exterior.addElement(tarjeta);
        exterior.setBorder(Rectangle.NO_BORDER);
        exterior.setPadding(3);
        return exterior;
    }

    private void writeBloque(Document document, MateriasEstado bloque) {
        PdfPTable fuente = new PdfPTable(1);
        fuente.setWidthPercentage(100);
        fuente.setSpacingBefore(8);
        fuente.setSpacingAfter(5);
        PdfPCell titulo = new PdfPCell(new Phrase(texto(bloque.etiquetaFuente()),
                new Font(Font.HELVETICA, 11, Font.BOLD, ACENTO)));
        titulo.setBorder(Rectangle.BOTTOM);
        titulo.setBorderColor(ACENTO);
        titulo.setPaddingLeft(0);
        titulo.setPaddingBottom(5);
        fuente.addCell(titulo);
        document.add(fuente);
        if (bloque.estado() != EstadoMaterias.OK) {
            String origen = "SIUM".equals(bloque.fuente()) ? "la facultad" : texto(bloque.etiquetaFuente());
            String leyenda = bloque.estado() == EstadoMaterias.SIN_INSCRIPCION
                    ? "Sin inscripción a materias en el lectivo."
                    : "No se pudieron consultar las materias en " + origen + ".";
            addText(document, paragraph(leyenda, 11, Font.NORMAL));
            return;
        }
        String curso = Integer.valueOf(0).equals(bloque.cursoInscripcion())
                ? "Sin cursar" : texto(bloque.cursoInscripcion());
        addText(document, paragraph("Carrera: " + texto(bloque.plan()) + "/" + texto(bloque.carrera())
                + " · Curso: " + curso, 9, Font.NORMAL));
        Paragraph observaciones = new Paragraph("Observaciones: " + texto(bloque.observaciones()),
                new Font(Font.HELVETICA, 8, Font.NORMAL, ETIQUETA));
        observaciones.setSpacingBefore(2);
        observaciones.setSpacingAfter(3);
        addText(document, observaciones);
        for (CursoMaterias grupo : bloque.cursos()) {
            Paragraph subtitulo = paragraph(grupo.curso() == null ? "Sin curso asignado" : "Curso " + grupo.curso(), 10, Font.BOLD);
            subtitulo.setSpacingBefore(8);
            addText(document, subtitulo);
            for (PeriodoMaterias periodo : grupo.periodos()) {
                Paragraph etiqueta = new Paragraph(periodo.periodo().etiqueta(),
                        new Font(Font.HELVETICA, 8, Font.BOLD, ETIQUETA));
                etiqueta.setSpacingBefore(3);
                etiqueta.setSpacingAfter(3);
                addText(document, etiqueta);
                PdfPTable table = new PdfPTable(new float[]{1, 5});
                table.setWidthPercentage(100);
                table.setHeaderRows(1);
                table.setSplitLate(false);
                table.addCell(celda("Código", true, false));
                table.addCell(celda("Materia", true, false));
                for (int i = 0; i < periodo.materias().size(); i++) {
                    MateriaInscripta materia = periodo.materias().get(i);
                    table.addCell(celda(texto(materia.materiaId()), false, i % 2 == 0));
                    table.addCell(celda(texto(materia.nombre()), false, i % 2 == 0));
                }
                document.add(table);
            }
        }
    }

    // OpenPDF emite los Paragraph en otra capa que las tablas. Una sola capa conserva
    // el orden de lectura del PDF, también para extracción de texto y accesibilidad.
    private void addText(Document document, Paragraph paragraph) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(paragraph.getSpacingBefore());
        table.setSpacingAfter(paragraph.getSpacingAfter());
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0);
        cell.addElement(paragraph);
        table.addCell(cell);
        document.add(table);
    }

    private PdfPCell celda(String text, boolean header, boolean alterna) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, header ? 8 : 9,
                header ? Font.BOLD : Font.NORMAL, header ? ETIQUETA : Color.BLACK)));
        cell.setPadding(3);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(LINEA);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        if (alterna) cell.setBackgroundColor(FONDO);
        return cell;
    }

    private Paragraph paragraph(String text, int size, int style) {
        return new Paragraph(text, new Font(Font.HELVETICA, size, style, style == Font.BOLD ? ACENTO : Color.BLACK));
    }

    private String texto(Object value) { return value == null ? "—" : value.toString(); }
}

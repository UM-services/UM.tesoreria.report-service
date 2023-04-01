package ar.edu.um.tesoreria.factura.rest.service.facade;

import ar.edu.um.tesoreria.factura.rest.kotlin.model.*;
import ar.edu.um.tesoreria.factura.rest.service.ChequeraCuotaService;
import ar.edu.um.tesoreria.factura.rest.service.ChequeraPagoService;
import ar.edu.um.tesoreria.factura.rest.service.ChequeraSerieService;
import ar.edu.um.tesoreria.factura.rest.service.FacturacionElectronicaService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
@Slf4j
public class ReciboService {

    @Autowired
    private Environment environment;

    @Autowired
    private FacturacionElectronicaService facturacionElectronicaService;

    @Autowired
    private ChequeraPagoService chequeraPagoService;

    @Autowired
    private ChequeraCuotaService chequeraCuotaService;

    @Autowired
    private ChequeraSerieService chequeraSerieService;

    private void createQRImage(File qrFile, String qrCodeText, int size, String fileType)
            throws WriterException, IOException {
        // Create the ByteMatrix for the QR-Code that encodes the given String
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, size, size, hintMap);
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        ImageIO.write(image, fileType, qrFile);
    }

    public String generatePdf(Long facturacionElectronicaId) {

        Image imageQr = null;
        FacturacionElectronica facturacionElectronica = facturacionElectronicaService.findByFacturacionElectronicaId(facturacionElectronicaId);
        Comprobante comprobante = facturacionElectronica.getComprobante();
        ChequeraPago chequeraPago = chequeraPagoService.findByChequeraPagoId(facturacionElectronica.getChequeraPagoId());
        ChequeraCuota chequeraCuota = chequeraCuotaService.findByUnique(chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId(), chequeraPago.getProductoId(), chequeraPago.getAlternativaId(), chequeraPago.getCuotaId());
        ChequeraSerie chequeraSerie = chequeraSerieService.findByUnique(chequeraPago.getFacultadId(), chequeraPago.getTipoChequeraId(), chequeraPago.getChequeraSerieId());

        String path = environment.getProperty("path.facturas");
        String empresaCuit = "30-51859446-6";

        try {
            String url = "https://www.afip.gob.ar/fe/qr/?p=";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ver", 1);
            jsonObject.put("fecha", DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    .format(facturacionElectronica.getFechaRecibo()));
            jsonObject.put("cuit", Long.parseLong(empresaCuit.replaceAll("\\-", "")));
            jsonObject.put("ptoVta", comprobante.getPuntoVenta());
            jsonObject.put("tipoCmp", comprobante.getComprobanteAfipId());
            jsonObject.put("nroCmp", facturacionElectronica.getNumeroComprobante());
            jsonObject.put("importe", facturacionElectronica.getImporte());
            jsonObject.put("moneda", "PES");
            jsonObject.put("ctz", 1);
            jsonObject.put("tipoDocRec", facturacionElectronica.getTipoDocumento());
            jsonObject.put("nroDocRec", facturacionElectronica.getPersonaId());
            jsonObject.put("tipoCodAut", "E");
            jsonObject.put("codAut", new BigDecimal(facturacionElectronica.getCae()));
            String datos = new String(Base64.getEncoder().encode(jsonObject.toString().getBytes()));
            String fileType = "png";
            String filePath = path + facturacionElectronica.getCae() + "." + fileType;
            int size = 150;
            File qrFile = new File(filePath);
            createQRImage(qrFile, url + datos, size, fileType);
            imageQr = Image.getInstance(filePath);
        } catch (BadElementException e) {
            log.debug("Sin Imagen");
        } catch (WriterException e) {
            log.debug("Sin Imagen");
        } catch (IOException e) {
            log.debug("Sin Imagen");
        }

        Integer copias = 2;
        ComprobanteAfip comprobanteAfip = comprobante.getComprobanteAfip();

        String[] titulo_copias = {"ORIGINAL", "DUPLICADO"};

        String filename = "";
        List<String> filenames = new ArrayList<>();
        for (int copia = 0; copia < copias; copia++) {
            filenames.add(filename = path + facturacionElectronicaId + "." + titulo_copias[copia].toLowerCase() + ".pdf");

            makePage(filename, titulo_copias[copia], comprobante, facturacionElectronica, chequeraCuota, chequeraPago, chequeraSerie, imageQr);
        }

        try {
            mergePdf(filename = path + facturacionElectronicaId + ".pdf", filenames);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filename;
    }

    private void mergePdf(String filename, List<String> filenames) throws DocumentException, IOException {
        OutputStream outputStream = new FileOutputStream(new File(filename));
        Document document = new Document();
        PdfWriter pdfWriter = PdfWriter.getInstance(document, outputStream);
        document.open();
        PdfContentByte pdfContentByte = pdfWriter.getDirectContent();
        for (String name : filenames) {
            PdfReader pdfReader = new PdfReader(new FileInputStream(new File(name)));
            for (int pagina = 0; pagina < pdfReader.getNumberOfPages(); ) {
                document.newPage();
                PdfImportedPage page = pdfWriter.getImportedPage(pdfReader, ++pagina);
                pdfContentByte.addTemplate(page, 0, 0);
            }
        }
        outputStream.flush();
        document.close();
        outputStream.close();
    }

    private void makePage(String filename, String titulo, Comprobante comprobante,
                          FacturacionElectronica facturacionElectronica, ChequeraCuota chequeraCuota, ChequeraPago chequeraPago, ChequeraSerie chequeraSerie, Image imageQr) {
        PdfPTable table = null;
        PdfPCell cell = null;

        Document document = new Document(new Rectangle(PageSize.A4));
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.setMargins(20, 20, 20, 20);
            document.open();

            table = new PdfPTable(1);
            table.setWidthPercentage(100);
            Paragraph paragraph = new Paragraph(titulo, new Font(Font.HELVETICA, 16, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell = new PdfPCell();
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{48, 4, 48});
            cell = new PdfPCell();
            paragraph = new Paragraph("Universidad de Mendoza", new Font(Font.HELVETICA, 14, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(10);
            cell.addElement(paragraph);
            cell.addElement(new Paragraph(" ", new Font(Font.HELVETICA, 6, Font.NORMAL)));
            paragraph = new Paragraph(new Phrase("Razón Social: ", new Font(Font.HELVETICA, 9, Font.NORMAL)));
            paragraph.add(new Phrase("Universidad de Mendoza", new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(10);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Domicilio: ", new Font(Font.HELVETICA, 9, Font.NORMAL)));
            paragraph.add(new Phrase("Boulogne Sur Mer 683 - Mendoza - 4202017", new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(10);
            cell.addElement(paragraph);
            paragraph = new Paragraph(
                    new Phrase("Condición frente al IVA: ", new Font(Font.HELVETICA, 9, Font.NORMAL)));
            paragraph.add(new Phrase("IVA Sujeto Exento", new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(10);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph(comprobante.getLetraComprobante(), new Font(Font.HELVETICA, 24, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Cod: ", new Font(Font.HELVETICA, 6, Font.NORMAL)));
            paragraph.add(
                    new Phrase(comprobante.getComprobanteAfipId().toString(), new Font(Font.HELVETICA, 6, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph(comprobante.getComprobanteAfip().getLabel(), new Font(Font.HELVETICA, 14, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Punto de Venta: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(new DecimalFormat("0000").format(comprobante.getPuntoVenta()),
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.add(new Phrase("          Comprobante Nro: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(new DecimalFormat("00000000").format(facturacionElectronica.getNumeroComprobante()),
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Fecha de Emisión: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(facturacionElectronica.getFechaRecibo()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("CUIT: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("30-51859446-6", new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Ingresos Brutos: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("", new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Inicio Actividades: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("", new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(1);
            table.setWidthPercentage(100);
            cell = new PdfPCell();
            paragraph = new Paragraph(new Phrase("Cliente: ", new Font(Font.HELVETICA, 10, Font.NORMAL)));
            paragraph.add(new Phrase(facturacionElectronica.getApellido() + ", " + facturacionElectronica.getNombre(), new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Domicilio: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("", new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Documento: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(facturacionElectronica.getPersonaId().toString(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.add(new Phrase("                          IVA: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            String[] condiciones = {"Responsable Inscripto", "Consumidor Final", "Monotributista",
                    "Responsable No Inscripto", "Exento", "Exportación"};
            paragraph
                    .add(new Phrase("Consumidor Final", new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            paragraph = new Paragraph(new Phrase("Condición de venta: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase("Contado",
                    new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            paragraph.setIndentationLeft(20);
            cell.addElement(paragraph);
            cell.addElement(new Paragraph(" ", new Font(Font.HELVETICA, 6, Font.BOLD)));
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{20, 50, 7, 12, 12});
            cell = new PdfPCell();
            paragraph = new Paragraph("Código", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph("Artículo", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph("Cantidad", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph("Precio Unitario", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);
            cell = new PdfPCell();
            paragraph = new Paragraph("Subtotal", new Font(Font.HELVETICA, 8, Font.BOLD));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            Integer lineas = 24;

            lineas--;
            table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{20, 50, 7, 12, 12});

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            String codigo = MessageFormat.format("{0}.{1}.{2}.{3}.{4}.{5}", chequeraCuota.getFacultadId(), chequeraCuota.getTipoChequeraId(), chequeraCuota.getChequeraSerieId(), chequeraCuota.getProductoId(), chequeraCuota.getAlternativaId(), chequeraCuota.getCuotaId());
            paragraph = new Paragraph(codigo, new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Tipo: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(chequeraCuota.getProducto().getNombre(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(String.valueOf(1),
                    new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(
                    new DecimalFormat("#,##0.00").format(chequeraPago.getImporte()),
                    new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(
                    new DecimalFormat("#,##0.00").format(chequeraPago.getImporte()),
                    new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Facultad
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Unidad Académica: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(chequeraCuota.getFacultad().getNombre(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Sede
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Sede: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(chequeraCuota.getTipoChequera().getGeografica().getNombre(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Tipo de Chequera
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Chequera: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(chequeraCuota.getTipoChequera().getNombre(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            // Arancel Tipo
            lineas--;

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph(new Phrase("Tipo de Arancel: ", new Font(Font.HELVETICA, 8, Font.NORMAL)));
            paragraph.add(new Phrase(chequeraSerie.getArancelTipo().getDescripcion(), new Font(Font.HELVETICA, 8, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            paragraph = new Paragraph("", new Font(Font.HELVETICA, 8, Font.NORMAL));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);

            document.add(table);

            for (int i = 0; i < lineas; i++) {
                table = new PdfPTable(1);
                table.setWidthPercentage(100);
                cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                cell.addElement(new Paragraph("  ", new Font(Font.COURIER, 8, Font.NORMAL)));
                table.addCell(cell);
                document.add(table);
            }

            table = new PdfPTable(1);
            table.setWidthPercentage(100);
            paragraph = new Paragraph(new Phrase("Observaciones: ", new Font(Font.COURIER, 10, Font.BOLD)));
            String observaciones = "";
            paragraph.add(new Phrase(observaciones, new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            cell = new PdfPCell();
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            table = new PdfPTable(1);
            table.setWidthPercentage(100);
            cell = new PdfPCell();
            paragraph = new Paragraph(new Phrase("Importe Total: $ ", new Font(Font.COURIER, 10, Font.BOLD)));
            paragraph.add(new Phrase(new DecimalFormat("#,##0.00").format(facturacionElectronica.getImporte().abs()),
                    new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(paragraph);
            table.addCell(cell);
            document.add(table);

            // Datos CAE
            float[] columnCAE = {1, 3};
            PdfPTable tableCAE = new PdfPTable(columnCAE);
            tableCAE.setWidthPercentage(100);

            // Agrega código QR
            cell = new PdfPCell();
            cell.addElement(imageQr);
            cell.setBorder(Rectangle.NO_BORDER);
            tableCAE.addCell(cell);
            //

            paragraph = new Paragraph("CAE Nro: ", new Font(Font.COURIER, 10, Font.NORMAL));
            paragraph.add(new Phrase(facturacionElectronica.getCae(), new Font(Font.HELVETICA, 10, Font.BOLD)));
            paragraph.add(new Phrase("\n", new Font(Font.COURIER, 10, Font.NORMAL)));
            paragraph.add(new Phrase("Vencimiento CAE: ", new Font(Font.COURIER, 10, Font.NORMAL)));
            paragraph.add(new Phrase(facturacionElectronica.getFechaVencimientoCae()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), new Font(Font.HELVETICA, 10, Font.BOLD)));
            cell = new PdfPCell(paragraph);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setLeading(0, 1.5f);
            tableCAE.addCell(cell);
            document.add(tableCAE);
            document.close();
        } catch (Exception ex) {
            log.debug(ex.getMessage());
        }

    }

}


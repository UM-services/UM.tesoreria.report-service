package um.tesoreria.report.hexagonal.recibos.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import um.tesoreria.report.hexagonal.recibos.domain.model.Recibo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReciboResponse(
        Long facturacionElectronicaId,
        Integer facultadId,
        Integer tipoChequeraId,
        Long chequeraSerieId,
        Long chequeraPagoId,
        Integer productoId,
        String producto,
        Integer anho,
        Integer mes,
        Integer comprobanteId,
        Long numeroComprobante,
        BigDecimal importe,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXX", timezone = "UTC")
        OffsetDateTime fechaRecibo,
        String cae,
        String pdfUrl) {

    /** Ruta (vía gateway) del PDF que genera tesoreria-sender-service. */
    static final String PDF_PATH = "/api/tesoreria/sender/recibo/pdf/";

    public static ReciboResponse from(Recibo recibo) {
        return new ReciboResponse(recibo.facturacionElectronicaId(), recibo.facultadId(), recibo.tipoChequeraId(),
                recibo.chequeraSerieId(), recibo.chequeraPagoId(), recibo.productoId(), recibo.producto(), recibo.anho(),
                recibo.mes(), recibo.comprobanteId(),
                recibo.numeroComprobante(), recibo.importe(), recibo.fechaRecibo(), recibo.cae(),
                PDF_PATH + recibo.facturacionElectronicaId());
    }
}
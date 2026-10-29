package um.tesoreria.report.hexagonal.recibos.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Recibo de pago emitido (facturación electrónica) de una chequera.
 */
public record Recibo(
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
        OffsetDateTime fechaRecibo,
        String cae) {
}
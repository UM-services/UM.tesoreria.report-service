package um.tesoreria.report.hexagonal.chequeras.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Estado de una chequera tal como se muestra en el PDF "Estado de Chequera": datos del titular,
 * cuotas agrupadas por producto (con sus subtotales oficiales) y adhesión al débito automático.
 * <p>
 * Es un modelo de lectura ya armado por {@code core-service}; este servicio solo lo dibuja.
 * Las fechas de pago, de vencimiento del débito y de envío llegan como fecha/hora en UTC y
 * {@code primerVencimiento} como fecha calendario contractual (sin huso horario).
 */
public record EstadoChequera(
        Integer facultadId,
        String facultadNombre,
        Integer tipoChequeraId,
        String tipoChequeraNombre,
        Long chequeraSerieId,
        BigDecimal personaId,
        String personaApellido,
        String personaNombre,
        String arancelTipoDescripcion,
        String lectivoNombre,
        BigDecimal becaPorcentaje,
        String tipoImpresionNombre,
        Integer alternativaId,
        List<ProductoEstado> productos,
        List<DebitoEstado> debitos) {

    public EstadoChequera {
        productos = productos == null ? List.of() : List.copyOf(productos);
        debitos = debitos == null ? List.of() : List.copyOf(debitos);
    }
}
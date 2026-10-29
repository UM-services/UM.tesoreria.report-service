package um.tesoreria.report.hexagonal.recibos.domain.ports.out;

import um.tesoreria.report.hexagonal.recibos.domain.model.Recibo;

import java.math.BigDecimal;
import java.util.List;

public interface RecibosRepository {

    /** Clave de una chequera. */
    record ChequeraRef(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
    }

    List<ChequeraRef> findChequeras(BigDecimal personaId, Integer documentoId, Integer lectivoId);

    List<Recibo> findRecibosByChequera(ChequeraRef chequera);
}
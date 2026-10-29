package um.tesoreria.report.hexagonal.recibos.domain.ports.in;

import um.tesoreria.report.hexagonal.recibos.domain.model.Recibo;

import java.math.BigDecimal;
import java.util.List;

public interface ListRecibosUseCase {

    /**
     * Lista los recibos emitidos (con CAE) de las chequeras de la persona en la facultad y el lectivo,
     * del más reciente al más antiguo. Devuelve lista vacía si no hay ninguno.
     */
    List<Recibo> listRecibos(Integer facultadId, Integer lectivoId, BigDecimal personaId, Integer documentoId);
}
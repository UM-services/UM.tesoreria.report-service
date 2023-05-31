package um.tesoreria.factura.rest.exception;

import java.text.MessageFormat;

public class ChequeraCuotaException extends RuntimeException {
    public ChequeraCuotaException(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer productoId, Integer alternativaId, Integer cuotaId) {
        super(MessageFormat.format("Cannot find ChequeraCuota {0}/{1}/{2}/{3}/{4}/{5}", facultadId, tipoChequeraId, chequeraSerieId, productoId, alternativaId, cuotaId));
    }

}

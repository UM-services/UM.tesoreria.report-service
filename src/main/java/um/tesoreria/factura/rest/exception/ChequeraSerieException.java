package um.tesoreria.factura.rest.exception;

import java.text.MessageFormat;

public class ChequeraSerieException extends RuntimeException {
    public ChequeraSerieException(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        super(MessageFormat.format("Cannot find ChequeraSerie ", facultadId, tipoChequeraId, chequeraSerieId));
    }
}

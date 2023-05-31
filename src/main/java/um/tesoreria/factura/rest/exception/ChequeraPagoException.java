package um.tesoreria.factura.rest.exception;

import java.text.MessageFormat;

public class ChequeraPagoException extends RuntimeException {
    public ChequeraPagoException(Long chequeraPagoId) {
        super(MessageFormat.format("Cannot find ChequeraPago {0}", chequeraPagoId));
    }

}

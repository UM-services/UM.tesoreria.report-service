package um.tesoreria.factura.rest.exception;

import java.text.MessageFormat;

public class ChequeraFacturacionElectronicaException extends RuntimeException {

    public ChequeraFacturacionElectronicaException(Long chequeraId) {
        super(MessageFormat.format("Cannot find ChequeraFacultadElectronica (chequeraId) -> ", chequeraId));
    }
}

package um.tesoreria.factura.rest.service;

import um.tesoreria.factura.rest.exception.ChequeraPagoException;
import um.tesoreria.factura.rest.kotlin.model.ChequeraPago;
import um.tesoreria.factura.rest.repository.ChequeraPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChequeraPagoService {

    @Autowired
    private ChequeraPagoRepository repository;

    public ChequeraPago findByChequeraPagoId(Long chequeraPagoId) {
        return repository.findByChequeraPagoId(chequeraPagoId).orElseThrow(() -> new ChequeraPagoException(chequeraPagoId));
    }

}

package um.tesoreria.factura.rest.service;

import um.tesoreria.factura.rest.exception.ChequeraFacturacionElectronicaException;
import um.tesoreria.factura.rest.kotlin.model.ChequeraFacturacionElectronica;
import um.tesoreria.factura.rest.repository.ChequeraFacturacionElectronicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChequeraFacturacionElectronicaService {

    @Autowired
    private ChequeraFacturacionElectronicaRepository repository;

    public ChequeraFacturacionElectronica findByChequeraId(Long chequeraId) {
        return repository.findByChequeraId(chequeraId).orElseThrow(() -> new ChequeraFacturacionElectronicaException(chequeraId));
    }
}

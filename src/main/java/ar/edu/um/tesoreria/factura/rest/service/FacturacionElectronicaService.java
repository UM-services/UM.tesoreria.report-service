package ar.edu.um.tesoreria.factura.rest.service;

import ar.edu.um.tesoreria.factura.rest.exception.FacturacionElectronicaException;
import ar.edu.um.tesoreria.factura.rest.kotlin.model.FacturacionElectronica;
import ar.edu.um.tesoreria.factura.rest.repository.FacturacionElectronicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FacturacionElectronicaService {

    @Autowired
    private FacturacionElectronicaRepository repository;

    public FacturacionElectronica findByFacturacionElectronicaId(Long facturacionElectronicaId) {
        return repository.findByFacturacionElectronicaId(facturacionElectronicaId).orElseThrow(() -> new FacturacionElectronicaException(facturacionElectronicaId));
    }
}

package um.tesoreria.factura.rest.service;

import um.tesoreria.factura.rest.exception.ChequeraSerieException;
import um.tesoreria.factura.rest.kotlin.model.ChequeraSerie;
import um.tesoreria.factura.rest.repository.ChequeraSerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChequeraSerieService {

    @Autowired
    private ChequeraSerieRepository repository;

    public ChequeraSerie findByUnique(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId) {
        return repository.findByFacultadIdAndTipoChequeraIdAndChequeraSerieId(facultadId, tipoChequeraId, chequeraSerieId).orElseThrow(() -> new ChequeraSerieException(facultadId, tipoChequeraId, chequeraSerieId));
    }

}

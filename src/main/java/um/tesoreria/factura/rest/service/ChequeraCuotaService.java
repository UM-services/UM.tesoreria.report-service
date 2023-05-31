package um.tesoreria.factura.rest.service;

import um.tesoreria.factura.rest.exception.ChequeraCuotaException;
import um.tesoreria.factura.rest.kotlin.model.ChequeraCuota;
import um.tesoreria.factura.rest.repository.ChequeraCuotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChequeraCuotaService {

    @Autowired
    private ChequeraCuotaRepository repository;

    public ChequeraCuota findByUnique(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer productoId, Integer alternativaId, Integer cuotaId) {
        return repository.findByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndProductoIdAndAlternativaIdAndCuotaId(facultadId, tipoChequeraId, chequeraSerieId, productoId, alternativaId, cuotaId).orElseThrow(() -> new ChequeraCuotaException(facultadId, tipoChequeraId, chequeraSerieId, productoId, alternativaId, cuotaId));
    }

}

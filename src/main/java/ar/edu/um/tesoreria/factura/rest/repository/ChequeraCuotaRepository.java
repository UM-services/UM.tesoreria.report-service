package ar.edu.um.tesoreria.factura.rest.repository;

import ar.edu.um.tesoreria.factura.rest.kotlin.model.ChequeraCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChequeraCuotaRepository extends JpaRepository<ChequeraCuota, Long> {
    public Optional<ChequeraCuota> findByFacultadIdAndTipoChequeraIdAndChequeraSerieIdAndProductoIdAndAlternativaIdAndCuotaId(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId, Integer productoId, Integer alternativaId, Integer cuotaId);

}

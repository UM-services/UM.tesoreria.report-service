package ar.edu.um.tesoreria.factura.rest.repository;

import ar.edu.um.tesoreria.factura.rest.kotlin.model.ChequeraSerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChequeraSerieRepository extends JpaRepository<ChequeraSerie, Long> {

    Optional<ChequeraSerie> findByFacultadIdAndTipoChequeraIdAndChequeraSerieId(Integer facultadId, Integer tipoChequeraId, Long chequeraSerieId);

}

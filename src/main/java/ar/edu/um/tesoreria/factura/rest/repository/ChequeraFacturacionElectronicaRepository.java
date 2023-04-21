package ar.edu.um.tesoreria.factura.rest.repository;

import ar.edu.um.tesoreria.factura.rest.kotlin.model.ChequeraFacturacionElectronica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChequeraFacturacionElectronicaRepository extends JpaRepository<ChequeraFacturacionElectronica, Long> {

    public Optional<ChequeraFacturacionElectronica> findByChequeraId(Long chequeraId);
}

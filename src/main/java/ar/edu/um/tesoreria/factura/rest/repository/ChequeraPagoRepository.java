package ar.edu.um.tesoreria.factura.rest.repository;

import ar.edu.um.tesoreria.factura.rest.kotlin.model.ChequeraPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChequeraPagoRepository extends JpaRepository<ChequeraPago, Long> {
    public Optional<ChequeraPago> findByChequeraPagoId(Long chequeraPagoId);

}
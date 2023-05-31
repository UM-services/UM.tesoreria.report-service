package um.tesoreria.factura.rest.repository;

import um.tesoreria.factura.rest.kotlin.model.ChequeraPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import um.tesoreria.factura.rest.kotlin.model.ChequeraPago;

import java.util.Optional;

@Repository
public interface ChequeraPagoRepository extends JpaRepository<ChequeraPago, Long> {
    public Optional<ChequeraPago> findByChequeraPagoId(Long chequeraPagoId);

}
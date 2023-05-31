package um.tesoreria.factura.rest.repository;

import um.tesoreria.factura.rest.kotlin.model.FacturacionElectronica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import um.tesoreria.factura.rest.kotlin.model.FacturacionElectronica;

import java.util.Optional;

@Repository
public interface FacturacionElectronicaRepository extends JpaRepository<FacturacionElectronica, Long> {

    public Optional<FacturacionElectronica> findByFacturacionElectronicaId(Long facturacionElectronicaId);

    public Optional<FacturacionElectronica> findTopByEnviadaAndRetriesLessThanOrderByFacturacionElectronicaId(Byte enviada, Integer retries);
}

package ar.edu.um.tesoreria.factura.rest.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@PropertySource("classpath:config/factura.properties")
public class FacturaConfiguration {

}

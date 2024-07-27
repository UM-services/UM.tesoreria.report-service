package um.tesoreria.report.configuration;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "um.tesoreria.report.client")
@PropertySource("classpath:config/reports.properties")
public class ReportConfiguration {

}

package um.tesoreria.report.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tesoreria/report/ping")
@RequiredArgsConstructor
public class PingController {

    private final Environment environment;

    @GetMapping("/")
    public ResponseEntity<String> ping() {
        var port = environment.getProperty("local.server.port");
        return ResponseEntity.ok("Running on port: " + port);
    }
    
}

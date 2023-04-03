package ar.edu.um.tesoreria.factura.rest.controller.facade;

import ar.edu.um.tesoreria.factura.rest.service.facade.ReciboService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@RestController
@RequestMapping("/recibo")
public class ReciboController {

    @Autowired
    private ReciboService service;

    @GetMapping("/pdf/{facturacionElectronicaId}")
    public ResponseEntity<Resource> makePdf(@PathVariable Long facturacionElectronicaId) throws FileNotFoundException {
        String filename = service.generatePdf(facturacionElectronicaId, null, null);
        File file = new File(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo.pdf");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }

    @GetMapping("/send/{facturacionElectronicaId}")
    public ResponseEntity<String> send(@PathVariable Long facturacionElectronicaId) throws MessagingException {
        return new ResponseEntity<String>(service.send(facturacionElectronicaId, null), HttpStatus.OK);
    }

    @GetMapping("/sendNext")
    public ResponseEntity<String> sendNext() {
        return new ResponseEntity<String>(service.sendNext(), HttpStatus.OK);
    }

}

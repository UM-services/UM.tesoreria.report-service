package um.tesoreria.report.util;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Tool {

    public static ResponseEntity<Resource> generateFile(String filename, String headerFilename) throws FileNotFoundException {
        File file = new File(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + headerFilename);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers).contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }


    /** Solo para reportes con un archivo propio por solicitud. Spring cierra el stream al descargar. */
    public static ResponseEntity<Resource> generateTemporaryFile(String filename, String headerFilename) throws IOException {
        ResponseEntity<Resource> response = generateFile(filename, headerFilename);
        Resource resource = new InputStreamResource(new FilterInputStream(response.getBody().getInputStream()) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    Files.deleteIfExists(Path.of(filename));
                }
            }
        });
        return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(resource);
    }
}

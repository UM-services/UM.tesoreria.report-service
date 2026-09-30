package um.tesoreria.report.hexagonal.chequeras.domain.ports.out;

import um.tesoreria.report.hexagonal.chequeras.domain.model.EstadoChequera;

import java.io.OutputStream;

public interface EstadoChequeraPdfGenerator {

    /**
     * Dibuja el estado de la chequera como PDF y lo escribe en {@code out}. No cierra el stream.
     */
    void generate(EstadoChequera estadoChequera, OutputStream out);
}
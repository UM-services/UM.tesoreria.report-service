package um.tesoreria.report.hexagonal.chequeras.domain.model;

public enum EstadoMaterias {
    OK, SIN_INSCRIPCION, NO_DISPONIBLE;

    public static EstadoMaterias desde(String valor) {
        if (valor == null) return NO_DISPONIBLE;
        try {
            return valueOf(valor);
        } catch (IllegalArgumentException e) {
            return NO_DISPONIBLE;
        }
    }
}

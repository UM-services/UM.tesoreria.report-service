package um.tesoreria.report.hexagonal.chequeras.domain.model;

public enum PeriodoCursada {
    ANUAL("Anual"), PRIMER_SEMESTRE("1er Semestre"),
    SEGUNDO_SEMESTRE("2do Semestre"), SIN_PERIODO("Sin período");

    private final String etiqueta;

    PeriodoCursada(String etiqueta) { this.etiqueta = etiqueta; }

    public String etiqueta() { return etiqueta; }

    public static PeriodoCursada desde(String valor) {
        if (valor == null) return SIN_PERIODO;
        try {
            return valueOf(valor);
        } catch (IllegalArgumentException e) {
            return SIN_PERIODO;
        }
    }
}

package co.edu.iudigital.tecnomovil.reporte;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Funciones puras que convierten resultados en texto para la consola.
 * Ninguna imprime: todas devuelven un {@code String}. La impresión (efecto
 * secundario) queda concentrada en {@code Main}.
 */
public final class FormateadorReporte {

    private static final Locale ES_CO = Locale.forLanguageTag("es-CO");
    private static final int ANCHO_LINEA = 78;

    private FormateadorReporte() {
    }

    /** Título de sección enmarcado. */
    public static String titulo(String texto) {
        String linea = "═".repeat(ANCHO_LINEA);
        return "%n%s%n  %s%n%s".formatted(linea, texto, linea);
    }

    /** Número entero con separador de miles colombiano (1.000.000). */
    public static String numero(long valor) {
        return String.format(ES_CO, "%,d", valor);
    }

    /** Número decimal con la cantidad de decimales indicada. */
    public static String decimal(double valor, int decimales) {
        return String.format(ES_CO, "%,." + decimales + "f", valor);
    }

    /** Porcentaje a partir de una fracción (0.85 → "85,0 %"). */
    public static String porcentaje(double fraccion) {
        return decimal(fraccion * 100, 1) + " %";
    }

    /** Barra horizontal proporcional a {@code valor / maximo}. */
    public static String barra(long valor, long maximo, int ancho) {
        int llenos = maximo == 0 ? 0 : (int) Math.round((double) valor * ancho / maximo);
        return "█".repeat(llenos) + "·".repeat(Math.max(0, ancho - llenos));
    }

    /**
     * Tabla de conteos con barra proporcional. Es una función de orden superior:
     * recibe cómo convertir cada clave en texto ({@code etiqueta}).
     */
    public static <K> String tablaConteo(List<Map.Entry<K, Long>> filas,
                                         Function<K, String> etiqueta,
                                         int anchoEtiqueta) {
        if (filas.isEmpty()) {
            return "  (sin datos)";
        }
        long maximo = filas.stream().mapToLong(Map.Entry::getValue).max().orElse(0);
        String formato = "  %-" + anchoEtiqueta + "s %12s  %s";
        return filas.stream()
                .map(f -> formato.formatted(
                        etiqueta.apply(f.getKey()), numero(f.getValue()), barra(f.getValue(), maximo, 30)))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    /** Tabla de conteos usando {@code toString} de la clave como etiqueta. */
    public static <K> String tablaConteo(List<Map.Entry<K, Long>> filas, int anchoEtiqueta) {
        return tablaConteo(filas, String::valueOf, anchoEtiqueta);
    }

    /** Etiqueta de una hora del día: 7 → "07:00 - 07:59". */
    public static String rangoHora(int hora) {
        return "%02d:00 - %02d:59".formatted(hora, hora);
    }
}

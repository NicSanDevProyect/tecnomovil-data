package co.edu.iudigital.tecnomovil.procesamiento;

import co.edu.iudigital.tecnomovil.modelo.Registro;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tarea (d): patrones de viaje por usuario.
 * Para cada usuario se genera la lista de estaciones visitadas en el orden en
 * que aparecen en los registros.
 */
public final class PatronesViaje {

    private PatronesViaje() {
    }

    /**
     * Función pura: usuario → estaciones visitadas en orden.
     *
     * <p>{@code groupingBy} con un colector no concurrente respeta el orden de
     * encuentro del stream, incluso con {@code parallelStream()}: cada hilo agrupa
     * un tramo de la lista y los resultados parciales se combinan en orden.
     * Por eso la secuencia de estaciones es la misma en ambos modos.</p>
     */
    public static Map<String, List<String>> porUsuario(Collection<Registro> registros, boolean paralelo) {
        return Operaciones.flujo(registros, paralelo)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Registro::idUsuario,
                                Collectors.mapping(Registro::estacion, Collectors.toUnmodifiableList())),
                        Map::copyOf));
    }

    /** Función pura: promedio de estaciones visitadas por usuario. */
    public static double promedioEstacionesPorUsuario(Map<String, List<String>> patrones) {
        return patrones.values().stream().mapToInt(List::size).average().orElse(0);
    }

    /** Función pura: texto legible de un patrón, por ejemplo "A → B → C". */
    public static String describir(List<String> patron) {
        return String.join(" → ", patron);
    }
}

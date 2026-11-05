package co.edu.iudigital.tecnomovil.procesamiento;

import co.edu.iudigital.tecnomovil.modelo.Registro;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Tarea (e): cálculo del tiempo promedio entre estaciones.
 *
 * <p>Para cada usuario se recorren sus registros en orden cronológico y se toman
 * los pares consecutivos ENTRADA → SALIDA de una misma ruta: cada par es un
 * {@link Tramo} y su duración es el tiempo que tomó moverse entre esas dos
 * estaciones.</p>
 */
public final class TiemposEntreEstaciones {

    /** Desplazamiento de un usuario entre dos estaciones consecutivas. */
    public record Tramo(String origen, String destino, String ruta, long segundos) {

        /** Clave legible del tramo, por ejemplo "Plaza Mayor → Terminal Sur". */
        public String clave() {
            return origen + " → " + destino;
        }
    }

    private TiemposEntreEstaciones() {
    }

    /**
     * Función pura: todos los tramos del día.
     * Pipeline: groupingBy(usuario) → por cada usuario, pares consecutivos → Tramo.
     */
    public static List<Tramo> tramos(Collection<Registro> registros, boolean paralelo) {
        Map<String, List<Registro>> porUsuario = Operaciones.flujo(registros, paralelo)
                .collect(Collectors.groupingBy(Registro::idUsuario));
        return Operaciones.flujo(porUsuario.values(), paralelo)
                .flatMap(TiemposEntreEstaciones::tramosDeUsuario)
                .sorted(Comparator.comparing(Tramo::clave)
                        .thenComparing(Tramo::ruta)
                        .thenComparingLong(Tramo::segundos))
                .toList();
    }

    /**
     * Función pura: pares (i, i+1) de los registros de un usuario, en orden
     * cronológico. Se ordena una <b>copia</b>; la lista recibida no se modifica.
     */
    static Stream<Tramo> tramosDeUsuario(List<Registro> registrosUsuario) {
        List<Registro> ordenados = registrosUsuario.stream()
                .sorted(Comparator.comparing(Registro::timestamp))
                .toList();
        return IntStream.range(0, ordenados.size() - 1)
                .mapToObj(i -> crearTramo(ordenados.get(i), ordenados.get(i + 1)))
                .flatMap(Optional::stream);
    }

    /** Función pura: un par forma tramo si es ENTRADA → SALIDA en la misma ruta. */
    static Optional<Tramo> crearTramo(Registro a, Registro b) {
        boolean esViaje = a.esEntrada() && b.esSalida() && a.ruta().equals(b.ruta());
        return esViaje
                ? Optional.of(new Tramo(a.estacion(), b.estacion(), a.ruta(),
                Duration.between(a.timestamp(), b.timestamp()).toSeconds()))
                : Optional.empty();
    }

    /** Función pura: tiempo promedio general, en minutos. */
    public static double promedioGeneralMinutos(List<Tramo> tramos) {
        return tramos.stream().mapToLong(Tramo::segundos).average().orElse(0) / 60.0;
    }

    /** Función pura: tramo ("A → B") → tiempo promedio en minutos. */
    public static Map<String, Double> promedioPorTramo(List<Tramo> tramos, boolean paralelo) {
        return Operaciones.flujo(tramos, paralelo)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Tramo::clave,
                                Collectors.collectingAndThen(Collectors.averagingLong(Tramo::segundos), s -> s / 60.0)),
                        Map::copyOf));
    }

    /** Función pura: ruta → tiempo promedio de viaje en minutos. */
    public static Map<String, Double> promedioPorRuta(List<Tramo> tramos, boolean paralelo) {
        return Operaciones.flujo(tramos, paralelo)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Tramo::ruta,
                                Collectors.collectingAndThen(Collectors.averagingLong(Tramo::segundos), s -> s / 60.0)),
                        Map::copyOf));
    }
}

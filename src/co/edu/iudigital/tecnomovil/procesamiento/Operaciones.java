package co.edu.iudigital.tecnomovil.procesamiento;

import co.edu.iudigital.tecnomovil.modelo.Registro;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Operaciones genéricas de orden superior reutilizadas por todas las tareas.
 *
 * <p>Ninguna operación modifica la colección recibida ni variables externas:
 * solo leen los datos y construyen resultados nuevos e inmodificables.</p>
 */
public final class Operaciones {

    private Operaciones() {
    }

    /**
     * Abre un stream secuencial o paralelo sobre los registros. Como todas las
     * operaciones posteriores son puras, el resultado es el mismo en ambos modos.
     */
    public static <T> Stream<T> flujo(Collection<T> datos, boolean paralelo) {
        return paralelo ? datos.parallelStream() : datos.stream();
    }

    /**
     * Función de orden superior: cuenta los registros que cumplen {@code filtro},
     * agrupados por la clave que devuelve {@code clasificador}.
     *
     * @param filtro       función {@code Registro -> boolean} (qué registros cuentan)
     * @param clasificador función {@code Registro -> K} (por qué se agrupa)
     * @return mapa inmodificable clave → cantidad
     */
    public static <K> Map<K, Long> contarPor(Collection<Registro> registros,
                                             Predicate<Registro> filtro,
                                             Function<Registro, K> clasificador,
                                             boolean paralelo) {
        return flujo(registros, paralelo)
                .filter(filtro)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(clasificador, Collectors.counting()),
                        Map::copyOf));
    }

    /**
     * Ordena un conteo de mayor a menor (en empate, por clave ascendente para que
     * el resultado sea estable) y lo devuelve como lista inmodificable.
     */
    public static <K extends Comparable<K>> List<Map.Entry<K, Long>> ordenarDescendente(Map<K, Long> conteo) {
        return conteo.entrySet().stream()
                .sorted(Map.Entry.<K, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(e -> Map.entry(e.getKey(), e.getValue()))
                .toList();
    }

    /** Los {@code n} primeros elementos del conteo ordenado de mayor a menor. */
    public static <K extends Comparable<K>> List<Map.Entry<K, Long>> top(Map<K, Long> conteo, int n) {
        return ordenarDescendente(conteo).stream().limit(n).toList();
    }
}

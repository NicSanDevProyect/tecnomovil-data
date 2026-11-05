package co.edu.iudigital.tecnomovil.procesamiento;

import co.edu.iudigital.tecnomovil.modelo.Registro;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Tarea (f): detección de sobrecarga en rutas.
 *
 * <p>La ocupación de una ruta es la demanda de su hora más cargada (ingresos en
 * esa hora) dividida entre su capacidad horaria simulada. Si la ocupación cumple
 * el criterio recibido como {@link Predicate}, la ruta se marca como CRÍTICA.</p>
 */
public final class SobrecargaRutas {

    /** Umbral por defecto: una ruta es crítica si supera el 90 % de su capacidad. */
    public static final double UMBRAL_POR_DEFECTO = 0.90;

    /** Estado de ocupación de una ruta en su hora pico. */
    public record EstadoRuta(String ruta, int horaPico, long demandaPico, long capacidad,
                             double ocupacion, boolean critica) {
    }

    private SobrecargaRutas() {
    }

    /**
     * Función de orden superior: devuelve el criterio "la ocupación supera el umbral".
     * Ejemplo: {@code superaUmbral(0.9)} devuelve {@code o -> o > 0.9}.
     */
    public static Predicate<Double> superaUmbral(double umbral) {
        return ocupacion -> ocupacion > umbral;
    }

    /**
     * Función pura de orden superior: evalúa todas las rutas.
     *
     * @param capacidadHoraria función ruta → capacidad (pasajeros por hora)
     * @param esCritica        criterio que decide si una ocupación es crítica
     * @return estados ordenados de mayor a menor ocupación (lista inmodificable)
     */
    public static List<EstadoRuta> evaluar(Collection<Registro> registros,
                                           Function<String, Long> capacidadHoraria,
                                           Predicate<Double> esCritica,
                                           boolean paralelo) {
        Map<String, Map<Integer, Long>> demanda = Operaciones.flujo(registros, paralelo)
                .filter(Registro::esEntrada)
                .collect(Collectors.groupingBy(Registro::ruta,
                        Collectors.groupingBy(Registro::hora, Collectors.counting())));
        return demanda.entrySet().stream()
                .map(e -> estado(e.getKey(), e.getValue(), capacidadHoraria, esCritica))
                .sorted(Comparator.comparingDouble(EstadoRuta::ocupacion).reversed()
                        .thenComparing(EstadoRuta::ruta))
                .toList();
    }

    /** Función pura: calcula el estado de una ruta a partir de su demanda por hora. */
    static EstadoRuta estado(String ruta, Map<Integer, Long> demandaPorHora,
                             Function<String, Long> capacidadHoraria, Predicate<Double> esCritica) {
        Map.Entry<Integer, Long> pico = demandaPorHora.entrySet().stream()
                .max(Map.Entry.<Integer, Long>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .orElseThrow();
        long capacidad = capacidadHoraria.apply(ruta);
        double ocupacion = capacidad == 0 ? Double.POSITIVE_INFINITY : (double) pico.getValue() / capacidad;
        return new EstadoRuta(ruta, pico.getKey(), pico.getValue(), capacidad, ocupacion, esCritica.test(ocupacion));
    }

    /** Función pura: solo las rutas marcadas como críticas. */
    public static List<EstadoRuta> criticas(List<EstadoRuta> estados) {
        return estados.stream().filter(EstadoRuta::critica).toList();
    }
}

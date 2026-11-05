package co.edu.iudigital.tecnomovil.procesamiento;

import co.edu.iudigital.tecnomovil.modelo.Registro;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tarea (b): identificación de horas pico.
 * Agrupa todas las validaciones (entradas y salidas) por hora del día y
 * clasifica cada hora como PICO, NORMAL o VALLE.
 */
public final class HorasPico {

    /** Clasificación de una franja horaria según su flujo. */
    public enum Franja { PICO, NORMAL, VALLE }

    /** Una hora es PICO si su flujo supera en 30 % el promedio de las horas con operación. */
    public static final double FACTOR_PICO = 1.3;

    /** Una hora es VALLE si su flujo es menor al 60 % del promedio. */
    public static final double FACTOR_VALLE = 0.6;

    private HorasPico() {
    }

    /** Función pura: hora (0–23) → número de validaciones en esa hora. */
    public static Map<Integer, Long> flujoPorHora(Collection<Registro> registros, boolean paralelo) {
        return Operaciones.contarPor(registros, r -> true, Registro::hora, paralelo);
    }

    /** Las {@code n} horas de mayor flujo, de mayor a menor. */
    public static List<Map.Entry<Integer, Long>> horasPico(Map<Integer, Long> flujo, int n) {
        return Operaciones.top(flujo, n);
    }

    /**
     * Función pura: clasifica cada hora comparando su flujo contra el promedio.
     * La regla de clasificación es una función ({@link #clasificador(double)})
     * que se pasa a {@code toMap} como parámetro.
     */
    public static Map<Integer, Franja> clasificar(Map<Integer, Long> flujo) {
        double promedio = flujo.values().stream().mapToLong(Long::longValue).average().orElse(0);
        Function<Long, Franja> regla = clasificador(promedio);
        return flujo.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> regla.apply(e.getValue())));
    }

    /** Función de orden superior: construye y devuelve la regla de clasificación. */
    public static Function<Long, Franja> clasificador(double promedio) {
        return valor -> valor >= promedio * FACTOR_PICO ? Franja.PICO
                : valor <= promedio * FACTOR_VALLE ? Franja.VALLE
                : Franja.NORMAL;
    }
}

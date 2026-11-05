package co.edu.iudigital.tecnomovil.rendimiento;

import co.edu.iudigital.tecnomovil.modelo.Registro;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.LongStream;

/**
 * Mide el tiempo de las tareas en modo secuencial y paralelo.
 *
 * <p>Cada tarea se representa como una función {@code (registros, paralelo) -> resultado},
 * de modo que el medidor no conoce ninguna tarea en particular: recibe el
 * comportamiento como parámetro (función de orden superior).</p>
 */
public final class Medidor {

    /** Tarea medible: nombre y la función que la ejecuta. */
    public record Tarea(String nombre, BiFunction<List<Registro>, Boolean, ?> ejecucion) {
    }

    /** Resultado de comparar una tarea en ambos modos. */
    public record Comparacion(String tarea, double msSecuencial, double msParalelo, boolean mismoResultado) {
        /** Cuántas veces más rápido es el modo paralelo. */
        public double aceleracion() {
            return msParalelo == 0 ? 0 : msSecuencial / msParalelo;
        }
    }

    private Medidor() {
    }

    /** Tiempo promedio en milisegundos de {@code repeticiones} ejecuciones (tras un calentamiento). */
    public static double promedioMs(Supplier<?> accion, int repeticiones) {
        accion.get(); // calentamiento: el compilador JIT optimiza el código antes de medir
        return LongStream.range(0, repeticiones)
                .map(i -> {
                    long inicio = System.nanoTime();
                    accion.get();
                    return System.nanoTime() - inicio;
                })
                .average()
                .orElse(0) / 1_000_000.0;
    }

    /** Compara la tarea en modo secuencial y paralelo, y verifica que el resultado coincida. */
    public static Comparacion comparar(Tarea tarea, List<Registro> registros, int repeticiones) {
        double secuencial = promedioMs(() -> tarea.ejecucion().apply(registros, false), repeticiones);
        double paralelo = promedioMs(() -> tarea.ejecucion().apply(registros, true), repeticiones);
        boolean igual = Objects.equals(tarea.ejecucion().apply(registros, false),
                tarea.ejecucion().apply(registros, true));
        return new Comparacion(tarea.nombre(), secuencial, paralelo, igual);
    }
}

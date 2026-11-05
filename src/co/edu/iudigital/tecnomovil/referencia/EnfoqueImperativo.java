package co.edu.iudigital.tecnomovil.referencia;

import co.edu.iudigital.tecnomovil.modelo.Registro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Versión <b>imperativa</b> de referencia, escrita al estilo del sistema actual de
 * TecnoValle, solo para compararla con la solución funcional.
 *
 * <p>No se usa en el procesamiento real. Sirve para mostrar en el informe los
 * problemas que plantea el caso de estudio: estado mutable compartido y conteos
 * erróneos por condiciones de carrera.</p>
 */
public final class EnfoqueImperativo {

    private EnfoqueImperativo() {
    }

    /**
     * Tarea (a) al estilo imperativo: ciclo {@code for}, condicional y un
     * {@code HashMap} mutable que se va modificando paso a paso.
     */
    public static Map<String, Long> afluenciaPorEstacion(List<Registro> registros) {
        Map<String, Long> conteo = new HashMap<>();
        for (Registro r : registros) {
            if (r.esEntrada()) {
                Long actual = conteo.get(r.estacion());
                if (actual == null) {
                    conteo.put(r.estacion(), 1L);
                } else {
                    conteo.put(r.estacion(), actual + 1);
                }
            }
        }
        return conteo;
    }

    /**
     * <b>Antipatrón:</b> varios hilos incrementan una misma variable compartida.
     * La operación {@code contador[0]++} no es atómica (leer, sumar, escribir), por
     * lo que los hilos se pisan entre sí y se pierden incrementos: es la
     * "condición de carrera" que describe el caso de estudio.
     */
    public static long contarEntradasConEstadoCompartido(List<Registro> registros) {
        long[] contador = {0};
        registros.parallelStream()
                .filter(Registro::esEntrada)
                .forEach(r -> contador[0]++);   // efecto secundario sobre estado compartido
        return contador[0];
    }

    /** Versión funcional equivalente: sin estado compartido, siempre correcta. */
    public static long contarEntradasFuncional(List<Registro> registros) {
        return registros.parallelStream()
                .filter(Registro::esEntrada)
                .count();
    }
}

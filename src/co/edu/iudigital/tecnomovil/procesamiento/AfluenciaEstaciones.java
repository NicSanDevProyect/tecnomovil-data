package co.edu.iudigital.tecnomovil.procesamiento;

import co.edu.iudigital.tecnomovil.modelo.Registro;

import java.util.Collection;
import java.util.Map;

/**
 * Tarea (a): cálculo de afluencia por estación.
 * Cuenta cuántos ingresos (validaciones de ENTRADA) recibe cada estación en el día.
 */
public final class AfluenciaEstaciones {

    private AfluenciaEstaciones() {
    }

    /**
     * Función pura: estación → número de ingresos.
     * Pipeline: filter(esEntrada) → groupingBy(estacion, counting).
     */
    public static Map<String, Long> calcular(Collection<Registro> registros, boolean paralelo) {
        return Operaciones.contarPor(registros, Registro::esEntrada, Registro::estacion, paralelo);
    }
}

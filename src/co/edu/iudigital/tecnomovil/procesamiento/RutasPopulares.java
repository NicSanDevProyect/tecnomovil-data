package co.edu.iudigital.tecnomovil.procesamiento;

import co.edu.iudigital.tecnomovil.modelo.Registro;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Tarea (c): rutas más utilizadas.
 * El volumen de uso de una ruta es el número de viajes que inician en ella
 * (validaciones de ENTRADA).
 */
public final class RutasPopulares {

    private RutasPopulares() {
    }

    /** Función pura: ruta → número de viajes. */
    public static Map<String, Long> usoPorRuta(Collection<Registro> registros, boolean paralelo) {
        return Operaciones.contarPor(registros, Registro::esEntrada, Registro::ruta, paralelo);
    }

    /** Función pura: las {@code n} rutas con mayor volumen de uso, de mayor a menor. */
    public static List<Map.Entry<String, Long>> masUtilizadas(Collection<Registro> registros, int n, boolean paralelo) {
        return Operaciones.top(usoPorRuta(registros, paralelo), n);
    }
}

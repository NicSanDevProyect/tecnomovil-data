package co.edu.iudigital.tecnomovil.datos;

import co.edu.iudigital.tecnomovil.modelo.Ruta;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Catálogo inmutable de rutas simuladas de la ciudad de TecnoValle.
 * Todas sus operaciones son funciones puras sobre datos constantes.
 */
public final class CatalogoRutas {

    /** Pasajeros que un bus puede movilizar en una hora (80 de capacidad × 2 vueltas). */
    public static final int PASAJEROS_POR_BUS_HORA = 160;

    /** Volumen de referencia sobre el que se dimensionó la flota simulada. */
    public static final int REGISTROS_REFERENCIA = 1_000_000;

    public static final List<Ruta> RUTAS = List.of(
            new Ruta("R01", "Troncal Norte-Sur",
                    List.of("Terminal Norte", "Bosques del Norte", "Universidad", "Parque Central", "Plaza Mayor", "Terminal Sur"), 20, 60),
            new Ruta("R02", "Troncal Oriente-Occidente",
                    List.of("Villa Nueva", "El Mirador", "Parque Central", "Centro Administrativo", "Mercado Central", "San Rafael"), 16, 58),
            new Ruta("R03", "Circular Centro",
                    List.of("Plaza Mayor", "Mercado Central", "Hospital General", "Centro Administrativo", "Estadio"), 14, 40),
            new Ruta("R04", "Universitaria",
                    List.of("Terminal Norte", "Universidad", "Estadio", "Hospital General", "Las Palmas"), 12, 40),
            new Ruta("R05", "Industrial",
                    List.of("Terminal Sur", "Barrio Industrial", "Zona Franca", "Puerto Seco", "Aeropuerto"), 10, 35),
            new Ruta("R06", "Alimentadora Los Pinos",
                    List.of("Los Pinos", "La Floresta", "Bosques del Norte", "Terminal Norte"), 8, 20),
            new Ruta("R07", "Alimentadora Santa Lucía",
                    List.of("Santa Lucía", "Alto Prado", "San Rafael", "Mercado Central"), 7, 22),
            new Ruta("R08", "Aeropuerto Express",
                    List.of("Parque Central", "Plaza Mayor", "Aeropuerto"), 6, 25),
            new Ruta("R09", "Alimentadora Ciudad Jardín",
                    List.of("Ciudad Jardín", "Las Palmas", "Villa Nueva", "El Mirador"), 4, 15),
            new Ruta("R10", "Circular Nocturna",
                    List.of("Terminal Sur", "Plaza Mayor", "Parque Central", "Terminal Norte"), 3, 12)
    );

    /** Suma de los pesos de popularidad. */
    public static final int PESO_TOTAL = RUTAS.stream().mapToInt(Ruta::peso).sum();

    /** Pesos acumulados, calculados una sola vez de forma declarativa. */
    private static final int[] PESO_ACUMULADO = IntStream.range(0, RUTAS.size())
            .map(i -> RUTAS.subList(0, i + 1).stream().mapToInt(Ruta::peso).sum())
            .toArray();

    private CatalogoRutas() {
    }

    /**
     * Función pura: elige la ruta que corresponde a un valor en [0, PESO_TOTAL).
     * Las rutas con más peso abarcan un intervalo mayor y salen elegidas más veces.
     */
    public static Ruta rutaPorPeso(int valor) {
        return IntStream.range(0, RUTAS.size())
                .filter(i -> valor < PESO_ACUMULADO[i])
                .mapToObj(RUTAS::get)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Valor fuera de rango: " + valor));
    }

    /** Función pura: busca una ruta por su código. */
    public static Optional<Ruta> buscar(String codigo) {
        return RUTAS.stream().filter(r -> r.codigo().equals(codigo)).findFirst();
    }

    /**
     * Función de orden superior: devuelve una función que calcula la capacidad
     * horaria de cada ruta, escalada según el volumen de registros simulados
     * (así el umbral de ocupación tiene sentido con 1 o con 6 millones de registros).
     */
    public static Function<String, Long> capacidadHoraria(long totalRegistros) {
        double escala = (double) totalRegistros / REGISTROS_REFERENCIA;
        return codigo -> buscar(codigo)
                .map(r -> Math.round(r.flota() * PASAJEROS_POR_BUS_HORA * escala))
                .orElse(0L);
    }
}

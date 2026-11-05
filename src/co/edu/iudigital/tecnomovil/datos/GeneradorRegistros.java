package co.edu.iudigital.tecnomovil.datos;

import co.edu.iudigital.tecnomovil.modelo.Accion;
import co.edu.iudigital.tecnomovil.modelo.Registro;
import co.edu.iudigital.tecnomovil.modelo.Ruta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Genera registros simulados de validadores de forma <b>determinista</b>.
 *
 * <p>Cada usuario y cada viaje obtiene su propio generador aleatorio derivado de
 * la semilla, por lo que el resultado depende solo de los argumentos
 * ({@code cantidad}, {@code semilla}): con los mismos argumentos siempre se obtiene
 * la misma lista. Es decir, {@link #generar(int, long)} se comporta como una
 * función pura y los resultados del informe son reproducibles.</p>
 *
 * <p>Cada viaje produce dos registros: una ENTRADA en la estación de origen y una
 * SALIDA en la estación de destino de la misma ruta. Las horas siguen una
 * distribución con picos en la mañana (6–8 h) y en la tarde (17–19 h).</p>
 */
public final class GeneradorRegistros {

    /** Día simulado de operación. */
    public static final LocalDate FECHA = LocalDate.of(2026, 11, 4);

    /** Semilla por defecto para obtener resultados reproducibles. */
    public static final long SEMILLA_POR_DEFECTO = 2026L;

    /** Viajes que intenta hacer cada usuario en el día (las horas repetidas se descartan). */
    private static final int VIAJES_POR_USUARIO = 5;

    /** Peso relativo de cada hora del día (índice 0 = 00:00 … 23 = 23:00). */
    private static final int[] PESO_HORA = {
            0, 0, 0, 0, 0, 3, 9, 12, 9, 5, 4, 4,
            6, 5, 4, 4, 6, 10, 12, 8, 4, 3, 2, 0
    };

    private static final int[] PESO_HORA_ACUMULADO = IntStream.range(0, PESO_HORA.length)
            .map(h -> IntStream.rangeClosed(0, h).map(i -> PESO_HORA[i]).sum())
            .toArray();

    private static final int PESO_HORA_TOTAL = PESO_HORA_ACUMULADO[PESO_HORA.length - 1];

    private GeneradorRegistros() {
    }

    /**
     * Genera {@code cantidad} registros ordenados cronológicamente.
     *
     * @return lista <b>inmodificable</b> ({@link Stream#toList()})
     */
    public static List<Registro> generar(int cantidad, long semilla) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        return IntStream.iterate(0, u -> u + 1)               // usuarios 0, 1, 2, …
                .mapToObj(u -> viajesDeUsuario(u, semilla))
                .flatMap(s -> s)
                .limit(cantidad)
                .sorted(Comparator.comparing(Registro::timestamp)
                        .thenComparing(Registro::idUsuario))
                .toList();
    }

    /** Atajo con la semilla por defecto. */
    public static List<Registro> generar(int cantidad) {
        return generar(cantidad, SEMILLA_POR_DEFECTO);
    }

    /** Viajes de un usuario: una hora distinta por viaje, en orden creciente. */
    private static Stream<Registro> viajesDeUsuario(int usuario, long semilla) {
        String id = "U%06d".formatted(usuario);
        SplittableRandom rnd = new SplittableRandom(semilla * 1_000_003L + usuario);
        return rnd.ints(VIAJES_POR_USUARIO, 0, PESO_HORA_TOTAL)
                .map(GeneradorRegistros::horaPorPeso)
                .distinct()
                .sorted()
                .boxed()
                .flatMap(hora -> viaje(id, usuario, hora, semilla));
    }

    /**
     * Un viaje inicia entre el minuto 0 y 29 de la hora y dura máximo 25 minutos,
     * así los viajes de un mismo usuario (en horas distintas) nunca se solapan.
     */
    private static Stream<Registro> viaje(String id, int usuario, int hora, long semilla) {
        SplittableRandom rnd = new SplittableRandom(semilla * 7_919L + usuario * 24L + hora);
        Ruta ruta = CatalogoRutas.rutaPorPeso(rnd.nextInt(CatalogoRutas.PESO_TOTAL));
        int n = ruta.estaciones().size();
        int origen = rnd.nextInt(n);
        int destino = (origen + 1 + rnd.nextInt(n - 1)) % n;   // siempre distinto del origen
        int duracion = Math.abs(destino - origen) * 4 + rnd.nextInt(1, 6);
        LocalDateTime inicio = FECHA.atTime(hora, rnd.nextInt(30), rnd.nextInt(60));
        return Stream.of(
                new Registro(id, ruta.codigo(), ruta.estaciones().get(origen), Accion.ENTRADA, inicio),
                new Registro(id, ruta.codigo(), ruta.estaciones().get(destino), Accion.SALIDA, inicio.plusMinutes(duracion)));
    }

    /** Función pura: convierte un valor en [0, PESO_HORA_TOTAL) en una hora del día. */
    static int horaPorPeso(int valor) {
        return IntStream.range(0, PESO_HORA_ACUMULADO.length)
                .filter(h -> valor < PESO_HORA_ACUMULADO[h])
                .findFirst()
                .orElseThrow();
    }
}

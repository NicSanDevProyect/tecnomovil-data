package co.edu.iudigital.tecnomovil;

import co.edu.iudigital.tecnomovil.datos.CatalogoRutas;
import co.edu.iudigital.tecnomovil.datos.GeneradorRegistros;
import co.edu.iudigital.tecnomovil.modelo.Accion;
import co.edu.iudigital.tecnomovil.modelo.Registro;
import co.edu.iudigital.tecnomovil.procesamiento.AfluenciaEstaciones;
import co.edu.iudigital.tecnomovil.procesamiento.HorasPico;
import co.edu.iudigital.tecnomovil.procesamiento.PatronesViaje;
import co.edu.iudigital.tecnomovil.procesamiento.RutasPopulares;
import co.edu.iudigital.tecnomovil.procesamiento.SobrecargaRutas;
import co.edu.iudigital.tecnomovil.procesamiento.TiemposEntreEstaciones;
import co.edu.iudigital.tecnomovil.referencia.EnfoqueImperativo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Pruebas del módulo sin frameworks externos (no se usa JUnit).
 *
 * <p>Cada prueba es un {@link Runnable} (una lambda) con un nombre. El "framework"
 * es una función que ejecuta la lista de pruebas y cuenta cuáles fallan.</p>
 */
public final class PruebasTecnoMovil {

    /** Una prueba: nombre y cuerpo (lambda que lanza AssertionError si falla). */
    record Prueba(String nombre, Runnable cuerpo) {
    }

    /**
     * Conjunto pequeño de datos con resultados conocidos de antemano.
     * U1: A→B (R01, 10 min) y B→D (R02, 20 min); U2: A→C (R01, 20 min); U3: D→A (R02, 30 min).
     */
    private static final List<Registro> MUESTRA = List.of(
            r("U1", "R01", "A", Accion.ENTRADA, 7, 0),
            r("U2", "R01", "A", Accion.ENTRADA, 7, 5),
            r("U1", "R01", "B", Accion.SALIDA, 7, 10),
            r("U2", "R01", "C", Accion.SALIDA, 7, 25),
            r("U1", "R02", "B", Accion.ENTRADA, 18, 0),
            r("U3", "R02", "D", Accion.ENTRADA, 18, 10),
            r("U1", "R02", "D", Accion.SALIDA, 18, 20),
            r("U3", "R02", "A", Accion.SALIDA, 18, 40)
    );

    /** Volumen mediano para comparar los modos secuencial y paralelo. */
    private static final List<Registro> VOLUMEN = GeneradorRegistros.generar(100_000);

    public static void main(String[] args) {
        List<Prueba> pruebas = List.of(
                new Prueba("a) Afluencia cuenta solo las entradas por estación", () ->
                        igual(Map.of("A", 2L, "B", 1L, "D", 1L), AfluenciaEstaciones.calcular(MUESTRA, false))),

                new Prueba("b) Flujo por hora agrupa entradas y salidas", () ->
                        igual(Map.of(7, 4L, 18, 4L), HorasPico.flujoPorHora(MUESTRA, false))),

                new Prueba("b) Clasificador de franjas (función que devuelve función)", () -> {
                    Function<Long, HorasPico.Franja> regla = HorasPico.clasificador(100);
                    igual(HorasPico.Franja.PICO, regla.apply(130L));
                    igual(HorasPico.Franja.NORMAL, regla.apply(100L));
                    igual(HorasPico.Franja.VALLE, regla.apply(60L));
                }),

                new Prueba("c) Ranking de rutas (empate ordenado por código)", () ->
                        igual(List.of(Map.entry("R01", 2L), Map.entry("R02", 2L)),
                                RutasPopulares.masUtilizadas(MUESTRA, 5, false))),

                new Prueba("d) Patrones respetan el orden de aparición", () ->
                        igual(Map.of("U1", List.of("A", "B", "B", "D"),
                                        "U2", List.of("A", "C"),
                                        "U3", List.of("D", "A")),
                                PatronesViaje.porUsuario(MUESTRA, true))),

                new Prueba("e) Tramos ENTRADA→SALIDA y promedio de 20 minutos", () -> {
                    List<TiemposEntreEstaciones.Tramo> tramos = TiemposEntreEstaciones.tramos(MUESTRA, false);
                    igual(4, tramos.size());
                    igual(20.0, TiemposEntreEstaciones.promedioGeneralMinutos(tramos));
                    igual(Map.of("R01", 15.0, "R02", 25.0), TiemposEntreEstaciones.promedioPorRuta(tramos, false));
                }),

                new Prueba("f) Ruta crítica según el criterio recibido (Predicate)", () -> {
                    Function<String, Long> capacidad = ruta -> 2L;   // ocupación = 2 / 2 = 100 %
                    verificar(SobrecargaRutas.evaluar(MUESTRA, capacidad, SobrecargaRutas.superaUmbral(0.9), false)
                            .stream().allMatch(SobrecargaRutas.EstadoRuta::critica), "con umbral 90 % deben ser críticas");
                    verificar(SobrecargaRutas.evaluar(MUESTRA, capacidad, SobrecargaRutas.superaUmbral(1.0), false)
                            .stream().noneMatch(SobrecargaRutas.EstadoRuta::critica), "con umbral 100 % no deben ser críticas");
                }),

                new Prueba("Funciones puras: no modifican la lista original", () -> {
                    List<Registro> copia = new ArrayList<>(MUESTRA);
                    AfluenciaEstaciones.calcular(copia, true);
                    HorasPico.flujoPorHora(copia, true);
                    PatronesViaje.porUsuario(copia, true);
                    TiemposEntreEstaciones.tramos(copia, true);
                    igual(MUESTRA, copia);
                }),

                new Prueba("Funciones puras: misma entrada, misma salida", () ->
                        igual(AfluenciaEstaciones.calcular(VOLUMEN, true), AfluenciaEstaciones.calcular(VOLUMEN, true))),

                new Prueba("Inmutabilidad: los resultados no se pueden modificar", () -> {
                    lanzaUnsupported(() -> AfluenciaEstaciones.calcular(MUESTRA, false).put("X", 1L));
                    lanzaUnsupported(() -> PatronesViaje.porUsuario(MUESTRA, false).get("U1").add("X"));
                    lanzaUnsupported(() -> RutasPopulares.masUtilizadas(MUESTRA, 2, false).clear());
                    lanzaUnsupported(() -> GeneradorRegistros.generar(10).remove(0));
                }),

                new Prueba("stream() y parallelStream() dan el mismo resultado", () -> {
                    igual(AfluenciaEstaciones.calcular(VOLUMEN, false), AfluenciaEstaciones.calcular(VOLUMEN, true));
                    igual(HorasPico.flujoPorHora(VOLUMEN, false), HorasPico.flujoPorHora(VOLUMEN, true));
                    igual(RutasPopulares.usoPorRuta(VOLUMEN, false), RutasPopulares.usoPorRuta(VOLUMEN, true));
                    igual(PatronesViaje.porUsuario(VOLUMEN, false), PatronesViaje.porUsuario(VOLUMEN, true));
                    igual(TiemposEntreEstaciones.tramos(VOLUMEN, false), TiemposEntreEstaciones.tramos(VOLUMEN, true));
                    Function<String, Long> capacidad = CatalogoRutas.capacidadHoraria(VOLUMEN.size());
                    igual(SobrecargaRutas.evaluar(VOLUMEN, capacidad, SobrecargaRutas.superaUmbral(0.9), false),
                            SobrecargaRutas.evaluar(VOLUMEN, capacidad, SobrecargaRutas.superaUmbral(0.9), true));
                }),

                new Prueba("Generador: determinista, tamaño exacto y orden cronológico", () -> {
                    igual(GeneradorRegistros.generar(5_000), GeneradorRegistros.generar(5_000));
                    igual(100_000, VOLUMEN.size());
                    verificar(IntStream.range(1, VOLUMEN.size()).allMatch(i ->
                                    !VOLUMEN.get(i).timestamp().isBefore(VOLUMEN.get(i - 1).timestamp())),
                            "los registros deben estar en orden cronológico");
                }),

                new Prueba("Imperativo y funcional calculan la misma afluencia", () ->
                        igual(EnfoqueImperativo.afluenciaPorEstacion(VOLUMEN), AfluenciaEstaciones.calcular(VOLUMEN, true)))
        );

        long fallidas = pruebas.stream()
                .map(PruebasTecnoMovil::ejecutar)
                .filter(ok -> !ok)
                .count();

        System.out.printf("%n  %d pruebas, %d exitosas, %d fallidas%n",
                pruebas.size(), pruebas.size() - fallidas, fallidas);
        if (fallidas > 0) {
            System.exit(1);
        }
    }

    /** Ejecuta una prueba, imprime su resultado y devuelve si pasó. */
    private static boolean ejecutar(Prueba prueba) {
        try {
            prueba.cuerpo().run();
            System.out.println("  [OK]    " + prueba.nombre());
            return true;
        } catch (AssertionError | RuntimeException e) {
            System.out.println("  [FALLA] " + prueba.nombre() + " -> " + e.getMessage());
            return false;
        }
    }

    // ---------- Aserciones mínimas ----------

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void igual(Object esperado, Object actual) {
        verificar(Objects.equals(esperado, actual),
                "se esperaba " + resumir(esperado) + " pero fue " + resumir(actual));
    }

    /** Recorta valores largos para que el mensaje de error sea legible. */
    private static String resumir(Object valor) {
        String texto = String.valueOf(valor);
        return texto.length() <= 120 ? texto : texto.substring(0, 120) + "…";
    }

    private static void lanzaUnsupported(Runnable accion) {
        try {
            accion.run();
        } catch (UnsupportedOperationException e) {
            return;
        }
        throw new AssertionError("se esperaba UnsupportedOperationException");
    }

    private static Registro r(String usuario, String ruta, String estacion, Accion accion, int hora, int minuto) {
        return new Registro(usuario, ruta, estacion, accion, GeneradorRegistros.FECHA.atTime(hora, minuto));
    }
}

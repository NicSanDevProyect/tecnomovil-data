package co.edu.iudigital.tecnomovil;

import co.edu.iudigital.tecnomovil.datos.GeneradorRegistros;
import co.edu.iudigital.tecnomovil.modelo.Registro;
import co.edu.iudigital.tecnomovil.procesamiento.AfluenciaEstaciones;
import co.edu.iudigital.tecnomovil.procesamiento.HorasPico;
import co.edu.iudigital.tecnomovil.procesamiento.Operaciones;
import co.edu.iudigital.tecnomovil.procesamiento.PatronesViaje;
import co.edu.iudigital.tecnomovil.procesamiento.RutasPopulares;
import co.edu.iudigital.tecnomovil.referencia.EnfoqueImperativo;
import co.edu.iudigital.tecnomovil.rendimiento.Medidor;
import co.edu.iudigital.tecnomovil.reporte.FormateadorReporte;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static co.edu.iudigital.tecnomovil.reporte.FormateadorReporte.*;

/**
 * Punto de entrada del módulo TecnoMovil Data.
 *
 * <p>Sigue el esquema "núcleo funcional, capa imperativa": todo el cálculo se hace
 * con funciones puras en {@code procesamiento}, y este {@code Main} es el único
 * lugar con efectos secundarios (leer argumentos e imprimir en consola).</p>
 *
 * <p>Uso: {@code java -cp out co.edu.iudigital.tecnomovil.Main [cantidad] [--secuencial] [--sin-rendimiento]}</p>
 */
public final class Main {

    private static final int CANTIDAD_POR_DEFECTO = 1_000_000;

    private Main() {
    }

    public static void main(String[] args) {
        int cantidad = Arrays.stream(args)
                .filter(a -> a.matches("\\d+"))
                .map(Integer::parseInt)
                .findFirst()
                .orElse(CANTIDAD_POR_DEFECTO);
        boolean paralelo = Arrays.stream(args).noneMatch("--secuencial"::equals);

        System.out.println(titulo("TecnoMovil Data – Procesamiento funcional de transporte urbano"));
        System.out.printf("  Generando %s registros simulados (semilla %d)...%n",
                numero(cantidad), GeneradorRegistros.SEMILLA_POR_DEFECTO);

        long inicio = System.nanoTime();
        List<Registro> registros = GeneradorRegistros.generar(cantidad);
        System.out.printf("  Registros generados: %s en %s ms%n", numero(registros.size()), milis(inicio));
        System.out.printf("  Modo de procesamiento: %s | Núcleos disponibles: %d%n",
                paralelo ? "parallelStream()" : "stream()", Runtime.getRuntime().availableProcessors());

        mostrarAfluencia(registros, paralelo);
        mostrarHorasPico(registros, paralelo);
        mostrarRutas(registros, paralelo);
        mostrarPatrones(registros, paralelo);
        mostrarComparacionImperativa(registros);
        mostrarInmutabilidad(registros, paralelo);
        if (Arrays.stream(args).noneMatch("--sin-rendimiento"::equals)) {
            mostrarRendimiento(registros);
        }
    }

    /** Tareas que se miden en la comparación de rendimiento. */
    private static List<Medidor.Tarea> tareas() {
        return List.of(
                new Medidor.Tarea("a) Afluencia por estación", AfluenciaEstaciones::calcular),
                new Medidor.Tarea("b) Flujo por hora", HorasPico::flujoPorHora),
                new Medidor.Tarea("c) Rutas más utilizadas", RutasPopulares::usoPorRuta),
                new Medidor.Tarea("d) Patrones de viaje", PatronesViaje::porUsuario)
        );
    }

    private static void mostrarRendimiento(List<Registro> registros) {
        System.out.println(titulo("Rendimiento: stream() vs. parallelStream() (promedio de 3 ejecuciones)"));
        System.out.printf("  %-34s %12s %12s %10s %10s%n", "Tarea", "Secuencial", "Paralelo", "Acelera", "Iguales");
        List<Medidor.Comparacion> comparaciones = tareas().stream()
                .map(t -> Medidor.comparar(t, registros, 3))
                .toList();
        comparaciones.stream()
                .map(c -> "  %-34s %9s ms %9s ms %9sx %10s".formatted(c.tarea(),
                        decimal(c.msSecuencial(), 1), decimal(c.msParalelo(), 1),
                        decimal(c.aceleracion(), 2), c.mismoResultado() ? "SÍ" : "NO"))
                .forEach(System.out::println);
        double totalSec = comparaciones.stream().mapToDouble(Medidor.Comparacion::msSecuencial).sum();
        double totalPar = comparaciones.stream().mapToDouble(Medidor.Comparacion::msParalelo).sum();
        System.out.printf("  %-34s %9s ms %9s ms %9sx%n", "TOTAL",
                decimal(totalSec, 1), decimal(totalPar, 1), decimal(totalSec / totalPar, 2));
    }

    private static void mostrarComparacionImperativa(List<Registro> registros) {
        System.out.println(titulo("Comparación: enfoque imperativo vs. funcional"));
        boolean mismoResultado = EnfoqueImperativo.afluenciaPorEstacion(registros)
                .equals(AfluenciaEstaciones.calcular(registros, true));
        System.out.printf("  Afluencia imperativa (for + HashMap) = funcional (streams): %s%n",
                mismoResultado ? "SÍ, mismo resultado" : "NO");

        long esperado = EnfoqueImperativo.contarEntradasFuncional(registros);
        System.out.printf("%n  Conteo de entradas en paralelo, 5 ejecuciones:%n");
        System.out.printf("  %-12s %22s %22s%n", "Ejecución", "Estado compartido", "Funcional (count)");
        IntStream.rangeClosed(1, 5)
                .mapToObj(i -> "  %-12s %22s %22s".formatted("#" + i,
                        numero(EnfoqueImperativo.contarEntradasConEstadoCompartido(registros)),
                        numero(EnfoqueImperativo.contarEntradasFuncional(registros))))
                .forEach(System.out::println);
        System.out.printf("  Valor correcto: %s. El contador compartido pierde incrementos%n", numero(esperado));
        System.out.println("  por condiciones de carrera; la versión funcional no tiene estado compartido.");
    }

    private static void mostrarInmutabilidad(List<Registro> registros, boolean paralelo) {
        System.out.println(titulo("Verificación de inmutabilidad"));
        int hashAntes = registros.hashCode();
        AfluenciaEstaciones.calcular(registros, paralelo);
        PatronesViaje.porUsuario(registros, paralelo);
        System.out.printf("  Lista original sin cambios tras procesar: %s%n",
                hashAntes == registros.hashCode() ? "SÍ" : "NO");
        System.out.printf("  Modificar la lista de registros  -> %s%n",
                intentar(() -> registros.add(registros.get(0))));
        System.out.printf("  Modificar el mapa de afluencia   -> %s%n",
                intentar(() -> AfluenciaEstaciones.calcular(registros, paralelo).put("Nueva", 1L)));
        System.out.printf("  Modificar un patrón de viaje     -> %s%n",
                intentar(() -> PatronesViaje.porUsuario(registros.subList(0, 100), paralelo)
                        .values().iterator().next().add("Otra")));
    }

    /** Ejecuta una acción que intenta modificar datos e informa qué ocurrió. */
    private static String intentar(Runnable accion) {
        try {
            accion.run();
            return "se modificó (no es inmutable)";
        } catch (UnsupportedOperationException e) {
            return "UnsupportedOperationException (inmutable)";
        }
    }

    private static void mostrarAfluencia(List<Registro> registros, boolean paralelo) {
        System.out.println(titulo("a) Afluencia por estación (ingresos en el día)"));
        Map<String, Long> afluencia = AfluenciaEstaciones.calcular(registros, paralelo);
        System.out.println(tablaConteo(Operaciones.ordenarDescendente(afluencia), 24));
        System.out.printf("%n  Total de ingresos: %s en %d estaciones%n",
                numero(afluencia.values().stream().mapToLong(Long::longValue).sum()), afluencia.size());
    }

    private static void mostrarHorasPico(List<Registro> registros, boolean paralelo) {
        System.out.println(titulo("b) Flujo por hora del día y horas pico"));
        Map<Integer, Long> flujo = HorasPico.flujoPorHora(registros, paralelo);
        Map<Integer, HorasPico.Franja> franjas = HorasPico.clasificar(flujo);
        long maximo = flujo.values().stream().mapToLong(Long::longValue).max().orElse(0);
        flujo.keySet().stream()
                .sorted()
                .map(h -> "  %s %12s  %s  %s".formatted(rangoHora(h), numero(flujo.get(h)),
                        barra(flujo.get(h), maximo, 30), franjas.get(h)))
                .forEach(System.out::println);
        System.out.println("\n  Top 3 horas pico:");
        System.out.println(tablaConteo(HorasPico.horasPico(flujo, 3), FormateadorReporte::rangoHora, 14));
    }

    private static void mostrarRutas(List<Registro> registros, boolean paralelo) {
        System.out.println(titulo("c) Rutas más utilizadas (viajes por ruta)"));
        System.out.println(tablaConteo(RutasPopulares.masUtilizadas(registros, 10, paralelo), 6));
    }

    private static void mostrarPatrones(List<Registro> registros, boolean paralelo) {
        System.out.println(titulo("d) Patrones de viaje por usuario (muestra)"));
        Map<String, List<String>> patrones = PatronesViaje.porUsuario(registros, paralelo);
        patrones.keySet().stream()
                .sorted()
                .limit(5)
                .map(u -> "  %s: %s".formatted(u, PatronesViaje.describir(patrones.get(u))))
                .forEach(System.out::println);
        System.out.printf("%n  Usuarios distintos: %s | Promedio de estaciones por usuario: %s%n",
                numero(patrones.size()), decimal(PatronesViaje.promedioEstacionesPorUsuario(patrones), 2));
    }

    private static String milis(long inicioNanos) {
        return numero((System.nanoTime() - inicioNanos) / 1_000_000);
    }
}

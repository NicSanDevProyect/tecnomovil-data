package co.edu.iudigital.tecnomovil;

import co.edu.iudigital.tecnomovil.datos.GeneradorRegistros;
import co.edu.iudigital.tecnomovil.modelo.Registro;
import co.edu.iudigital.tecnomovil.procesamiento.AfluenciaEstaciones;
import co.edu.iudigital.tecnomovil.procesamiento.HorasPico;
import co.edu.iudigital.tecnomovil.procesamiento.Operaciones;
import co.edu.iudigital.tecnomovil.procesamiento.PatronesViaje;
import co.edu.iudigital.tecnomovil.procesamiento.RutasPopulares;
import co.edu.iudigital.tecnomovil.reporte.FormateadorReporte;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static co.edu.iudigital.tecnomovil.reporte.FormateadorReporte.*;

/**
 * Punto de entrada del módulo TecnoMovil Data.
 *
 * <p>Sigue el esquema "núcleo funcional, capa imperativa": todo el cálculo se hace
 * con funciones puras en {@code procesamiento}, y este {@code Main} es el único
 * lugar con efectos secundarios (leer argumentos e imprimir en consola).</p>
 *
 * <p>Uso: {@code java -cp out co.edu.iudigital.tecnomovil.Main [cantidad] [--secuencial]}</p>
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

package co.edu.iudigital.tecnomovil.modelo;

import java.util.List;
import java.util.Objects;

/**
 * Ruta del sistema de transporte (dato de referencia inmutable).
 *
 * @param codigo     código de la ruta (por ejemplo, "R01")
 * @param nombre     nombre descriptivo
 * @param estaciones estaciones en el orden en que las recorre la ruta
 * @param peso       popularidad relativa usada para simular la demanda
 * @param flota      número de buses asignados (define la capacidad simulada)
 */
public record Ruta(String codigo, String nombre, List<String> estaciones, int peso, int flota) {

    public Ruta {
        Objects.requireNonNull(codigo, "codigo");
        Objects.requireNonNull(nombre, "nombre");
        // Copia defensiva inmodificable: nadie puede alterar la lista de estaciones.
        estaciones = List.copyOf(estaciones);
        if (estaciones.size() < 2) {
            throw new IllegalArgumentException("Una ruta necesita al menos dos estaciones");
        }
        if (peso <= 0 || flota <= 0) {
            throw new IllegalArgumentException("peso y flota deben ser positivos");
        }
    }
}

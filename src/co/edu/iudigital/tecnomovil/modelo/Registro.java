package co.edu.iudigital.tecnomovil.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro inmutable de una validación en el sistema de transporte.
 *
 * <p>Al ser un {@code record}, todos sus campos son {@code private final}, no tiene
 * métodos {@code set} y {@code equals}/{@code hashCode}/{@code toString} se generan
 * a partir de sus valores. Una vez creado, un registro nunca cambia, por lo que
 * puede compartirse entre hilos sin sincronización (requisito para usar
 * {@code parallelStream()} con seguridad).</p>
 *
 * @param idUsuario identificador de la tarjeta del usuario
 * @param ruta      código de la ruta (por ejemplo, "R01")
 * @param estacion  nombre de la estación donde se validó
 * @param accion    {@link Accion#ENTRADA} o {@link Accion#SALIDA}
 * @param timestamp fecha y hora de la validación
 */
public record Registro(String idUsuario,
                       String ruta,
                       String estacion,
                       Accion accion,
                       LocalDateTime timestamp) {

    /** Constructor compacto: valida que ningún campo sea nulo. */
    public Registro {
        Objects.requireNonNull(idUsuario, "idUsuario");
        Objects.requireNonNull(ruta, "ruta");
        Objects.requireNonNull(estacion, "estacion");
        Objects.requireNonNull(accion, "accion");
        Objects.requireNonNull(timestamp, "timestamp");
    }

    /** Función pura: indica si el registro corresponde a un ingreso. */
    public boolean esEntrada() {
        return accion == Accion.ENTRADA;
    }

    /** Función pura: indica si el registro corresponde a una salida. */
    public boolean esSalida() {
        return accion == Accion.SALIDA;
    }

    /** Función pura: hora del día (0–23) en que ocurrió la validación. */
    public int hora() {
        return timestamp.getHour();
    }
}

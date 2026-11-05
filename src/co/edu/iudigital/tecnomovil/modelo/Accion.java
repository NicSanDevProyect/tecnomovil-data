package co.edu.iudigital.tecnomovil.modelo;

/**
 * Tipo de validación registrada por un validador de tarjeta.
 * <ul>
 *   <li>{@link #ENTRADA}: el usuario ingresa a la estación.</li>
 *   <li>{@link #SALIDA}: el usuario valida para salir o cambia de ruta.</li>
 * </ul>
 */
public enum Accion {
    ENTRADA,
    SALIDA
}

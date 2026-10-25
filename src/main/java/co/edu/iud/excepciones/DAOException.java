package co.edu.iud.excepciones;

/**
 * Excepción propia de la capa de acceso a datos.
 *
 * Envuelve las SQLException (y otros errores técnicos) para que la vista nunca
 * vea detalles internos como nombres de tablas, consultas o rutas. El mensaje
 * de esta excepción SIEMPRE es apto para mostrarle al usuario; el detalle
 * técnico viaja en la "causa" y se registra en el log (sesión 4: no filtrar
 * información del error).
 */
public class DAOException extends Exception {

    private static final long serialVersionUID = 1L;

    public DAOException(String mensaje) {
        super(mensaje);
    }

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

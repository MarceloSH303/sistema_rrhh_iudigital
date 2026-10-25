package co.edu.iud.excepciones;

/**
 * Se lanza cuando un dato ingresado por el usuario no cumple las reglas de
 * validación (campo vacío, formato inválido, fecha imposible, etc.).
 *
 * Se distingue de DAOException porque el error es del dato, no de la base de
 * datos: la vista lo muestra como una advertencia para que el usuario corrija.
 */
public class ValidacionException extends Exception {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}

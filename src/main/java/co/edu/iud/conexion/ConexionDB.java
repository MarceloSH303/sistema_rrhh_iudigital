package co.edu.iud.conexion;

import co.edu.iud.excepciones.DAOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entrega conexiones a PostgreSQL. La contraseña NO está en el código:
 * se lee de la variable de entorno DB_PASSWORD.
 */
public final class ConexionDB {

    private static final Logger LOG = Logger.getLogger(ConexionDB.class.getName());

    private static final String URL = "jdbc:postgresql://localhost:5432/recursos_humanos";
    private static final String USER = "app_rrhh";

    private ConexionDB() {
    }

    public static Connection getConnection() throws DAOException {
        String clave = System.getenv("DB_PASSWORD");
        if (clave == null || clave.isBlank()) {
            throw new DAOException("Falta la variable de entorno DB_PASSWORD "
                    + "con la contraseña de la base de datos.");
        }
        try {
            return DriverManager.getConnection(URL, USER, clave);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo abrir la conexión con la base de datos", e);
            throw new DAOException("No fue posible conectar con la base de datos. "
                    + "Verifique que PostgreSQL esté encendido y que la contraseña sea correcta.", e);
        }
    }
}
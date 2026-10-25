package co.edu.iud.dao;

import co.edu.iud.conexion.ConexionDB;
import co.edu.iud.excepciones.DAOException;
import co.edu.iud.modelo.Catalogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lee las tablas de catálogo para llenar los JComboBox del formulario.
 * El usuario de la aplicación solo tiene SELECT sobre estas tablas.
 */
public class CatalogoDAO {

    private static final Logger LOG = Logger.getLogger(CatalogoDAO.class.getName());

    /**
     * Los nombres de tabla salen de este enum y nunca de un texto escrito por
     * el usuario. Por eso es seguro concatenarlos en la consulta (los
     * identificadores no se pueden parametrizar con PreparedStatement).
     */
    public enum Tipo {
        TIPO_IDENTIFICACION("tipo_identificacion"),
        ESTADO_CIVIL("estado_civil"),
        SEXO("sexo"),
        ROL_FAMILIAR("rol_familiar"),
        NIVEL_ESTUDIO("nivel_estudio");

        private final String tabla;

        Tipo(String tabla) {
            this.tabla = tabla;
        }

        String tabla() {
            return tabla;
        }
    }

    public List<Catalogo> listar(Tipo tipo) throws DAOException {
        String sql = "SELECT id, nombre FROM " + tipo.tabla() + " ORDER BY id";
        List<Catalogo> resultado = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(new Catalogo(rs.getInt("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error al leer el catálogo " + tipo, e);
            throw new DAOException("No fue posible cargar las listas de opciones.", e);
        }
        return resultado;
    }
}

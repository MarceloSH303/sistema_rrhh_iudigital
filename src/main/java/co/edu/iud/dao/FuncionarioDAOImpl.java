package co.edu.iud.dao;

import co.edu.iud.conexion.ConexionDB;
import co.edu.iud.excepciones.DAOException;
import co.edu.iud.modelo.Catalogo;
import co.edu.iud.modelo.Funcionario;
import co.edu.iud.modelo.InformacionAcademica;
import co.edu.iud.modelo.MiembroFamiliar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación de FuncionarioDAO para PostgreSQL con JDBC.
 *
 * Reglas de seguridad aplicadas:
 *  - Todas las consultas usan PreparedStatement: ningún dato del usuario se
 *    concatena en el SQL (evita inyección SQL).
 *  - Crear y actualizar usan una transacción: o se guarda todo (funcionario,
 *    familiares y estudios) o no se guarda nada.
 *  - Las SQLException se traducen a DAOException con mensajes limpios; el
 *    detalle técnico solo va al log.
 */
public class FuncionarioDAOImpl implements FuncionarioDAO {

    private static final Logger LOG = Logger.getLogger(FuncionarioDAOImpl.class.getName());

    private static final String SELECT_FUNCIONARIO = """
            SELECT f.id, f.numero_identificacion, f.nombres, f.apellidos,
                   f.direccion, f.telefono, f.fecha_nacimiento,
                   ti.id AS ti_id, ti.nombre AS ti_nombre,
                   ec.id AS ec_id, ec.nombre AS ec_nombre,
                   s.id  AS s_id,  s.nombre  AS s_nombre
              FROM funcionario f
              JOIN tipo_identificacion ti ON ti.id = f.tipo_identificacion_id
              JOIN estado_civil ec        ON ec.id = f.estado_civil_id
              JOIN sexo s                 ON s.id  = f.sexo_id
            """;

    private static final String ORDEN = " ORDER BY f.apellidos, f.nombres";

    private static final String INSERT_FUNCIONARIO = """
            INSERT INTO funcionario
                (tipo_identificacion_id, numero_identificacion, nombres, apellidos,
                 estado_civil_id, sexo_id, direccion, telefono, fecha_nacimiento)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

    private static final String UPDATE_FUNCIONARIO = """
            UPDATE funcionario
               SET tipo_identificacion_id = ?, numero_identificacion = ?, nombres = ?,
                   apellidos = ?, estado_civil_id = ?, sexo_id = ?, direccion = ?,
                   telefono = ?, fecha_nacimiento = ?
             WHERE id = ?
            """;

    private static final String INSERT_FAMILIAR = """
            INSERT INTO miembro_familiar
                (funcionario_id, rol_familiar_id, nombres, apellidos,
                 tipo_identificacion_id, numero_identificacion, fecha_nacimiento)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String INSERT_ESTUDIO = """
            INSERT INTO informacion_academica
                (funcionario_id, universidad, nivel_estudio_id, titulo)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_FAMILIARES = """
            SELECT m.id, m.nombres, m.apellidos, m.numero_identificacion, m.fecha_nacimiento,
                   r.id AS rol_id, r.nombre AS rol_nombre,
                   ti.id AS ti_id, ti.nombre AS ti_nombre
              FROM miembro_familiar m
              JOIN rol_familiar r         ON r.id  = m.rol_familiar_id
              JOIN tipo_identificacion ti ON ti.id = m.tipo_identificacion_id
             WHERE m.funcionario_id = ?
             ORDER BY m.id
            """;

    private static final String SELECT_ESTUDIOS = """
            SELECT a.id, a.universidad, a.titulo,
                   n.id AS nivel_id, n.nombre AS nivel_nombre
              FROM informacion_academica a
              JOIN nivel_estudio n ON n.id = a.nivel_estudio_id
             WHERE a.funcionario_id = ?
             ORDER BY a.id
            """;

    /** Bloque de trabajo que se ejecuta dentro de una transacción. */
    @FunctionalInterface
    private interface OperacionSQL {
        void ejecutar(Connection con) throws SQLException, DAOException;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Override
    public List<Funcionario> listar() throws DAOException {
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_FUNCIONARIO + ORDEN);
             ResultSet rs = ps.executeQuery()) {
            return mapearLista(rs);
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public List<Funcionario> buscar(String texto) throws DAOException {
        if (texto == null || texto.isBlank()) {
            return listar();
        }
        // Se escapan los comodines de LIKE para que "%" o "_" se busquen como texto literal.
        String patron = "%" + texto.trim()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        String sql = SELECT_FUNCIONARIO
                + " WHERE (f.nombres || ' ' || f.apellidos) ILIKE ? ESCAPE '\\'"
                + "    OR f.numero_identificacion ILIKE ? ESCAPE '\\'"
                + ORDEN;
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    @Override
    public Funcionario buscarPorId(int id) throws DAOException {
        try (Connection con = ConexionDB.getConnection()) {
            Funcionario f;
            try (PreparedStatement ps = con.prepareStatement(SELECT_FUNCIONARIO + " WHERE f.id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new DAOException("El funcionario ya no existe.");
                    }
                    f = mapear(rs);
                }
            }
            f.setFamiliares(leerFamiliares(con, id));
            f.setEstudios(leerEstudios(con, id));
            return f;
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    // ------------------------------------------------------------------
    // Escritura (cada operación es una transacción)
    // ------------------------------------------------------------------

    @Override
    public void crear(Funcionario f) throws DAOException {
        enTransaccion(con -> {
            int id = insertarFuncionario(con, f);
            f.setId(id);
            insertarHijos(con, id, f);
        });
    }

    @Override
    public void actualizar(Funcionario f) throws DAOException {
        if (f.getId() == null) {
            throw new DAOException("No se puede actualizar un funcionario que aún no ha sido guardado.");
        }
        enTransaccion(con -> {
            try (PreparedStatement ps = con.prepareStatement(UPDATE_FUNCIONARIO)) {
                asignarDatos(ps, f);
                ps.setInt(10, f.getId());
                if (ps.executeUpdate() == 0) {
                    throw new DAOException("El funcionario ya no existe.");
                }
            }
            // Los hijos se reemplazan: se borran y se vuelven a insertar con lo que quedó en pantalla.
            borrarHijos(con, f.getId());
            insertarHijos(con, f.getId(), f);
        });
    }

    @Override
    public void eliminar(int id) throws DAOException {
        // ON DELETE CASCADE borra los familiares y estudios; una sola sentencia basta.
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM funcionario WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("El funcionario ya no existe.");
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    // ------------------------------------------------------------------
    // Transacciones
    // ------------------------------------------------------------------

    private void enTransaccion(OperacionSQL operacion) throws DAOException {
        try (Connection con = ConexionDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                operacion.ejecutar(con);
                con.commit();
            } catch (SQLException e) {
                revertir(con);
                throw traducir(e);
            } catch (DAOException e) {
                revertir(con);
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    private void revertir(Connection con) {
        try {
            con.rollback();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "No se pudo revertir la transacción", e);
        }
    }

    // ------------------------------------------------------------------
    // Escritura de piezas
    // ------------------------------------------------------------------

    private int insertarFuncionario(Connection con, Funcionario f) throws SQLException, DAOException {
        try (PreparedStatement ps = con.prepareStatement(INSERT_FUNCIONARIO)) {
            asignarDatos(ps, f);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DAOException("No fue posible guardar el funcionario.");
                }
                return rs.getInt(1);
            }
        }
    }

    private void asignarDatos(PreparedStatement ps, Funcionario f) throws SQLException {
        ps.setInt(1, f.getTipoIdentificacion().getId());
        ps.setString(2, f.getNumeroIdentificacion());
        ps.setString(3, f.getNombres());
        ps.setString(4, f.getApellidos());
        ps.setInt(5, f.getEstadoCivil().getId());
        ps.setInt(6, f.getSexo().getId());
        ps.setString(7, f.getDireccion());
        ps.setString(8, f.getTelefono());
        ps.setObject(9, f.getFechaNacimiento());
    }

    private void insertarHijos(Connection con, int funcionarioId, Funcionario f) throws SQLException {
        if (!f.getFamiliares().isEmpty()) {
            try (PreparedStatement ps = con.prepareStatement(INSERT_FAMILIAR)) {
                for (MiembroFamiliar m : f.getFamiliares()) {
                    ps.setInt(1, funcionarioId);
                    ps.setInt(2, m.getRolFamiliar().getId());
                    ps.setString(3, m.getNombres());
                    ps.setString(4, m.getApellidos());
                    ps.setInt(5, m.getTipoIdentificacion().getId());
                    ps.setString(6, m.getNumeroIdentificacion());
                    ps.setObject(7, m.getFechaNacimiento());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
        if (!f.getEstudios().isEmpty()) {
            try (PreparedStatement ps = con.prepareStatement(INSERT_ESTUDIO)) {
                for (InformacionAcademica e : f.getEstudios()) {
                    ps.setInt(1, funcionarioId);
                    ps.setString(2, e.getUniversidad());
                    ps.setInt(3, e.getNivelEstudio().getId());
                    ps.setString(4, e.getTitulo());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private void borrarHijos(Connection con, int funcionarioId) throws SQLException {
        for (String tabla : new String[]{"miembro_familiar", "informacion_academica"}) {
            // Los nombres de tabla son constantes de este método, no datos del usuario.
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM " + tabla + " WHERE funcionario_id = ?")) {
                ps.setInt(1, funcionarioId);
                ps.executeUpdate();
            }
        }
    }

    // ------------------------------------------------------------------
    // Lectura de piezas
    // ------------------------------------------------------------------

    private List<Funcionario> mapearLista(ResultSet rs) throws SQLException {
        List<Funcionario> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        return lista;
    }

    private Funcionario mapear(ResultSet rs) throws SQLException {
        Funcionario f = new Funcionario();
        f.setId(rs.getInt("id"));
        f.setTipoIdentificacion(new Catalogo(rs.getInt("ti_id"), rs.getString("ti_nombre")));
        f.setNumeroIdentificacion(rs.getString("numero_identificacion"));
        f.setNombres(rs.getString("nombres"));
        f.setApellidos(rs.getString("apellidos"));
        f.setEstadoCivil(new Catalogo(rs.getInt("ec_id"), rs.getString("ec_nombre")));
        f.setSexo(new Catalogo(rs.getInt("s_id"), rs.getString("s_nombre")));
        f.setDireccion(rs.getString("direccion"));
        f.setTelefono(rs.getString("telefono"));
        f.setFechaNacimiento(rs.getObject("fecha_nacimiento", LocalDate.class));
        return f;
    }

    private List<MiembroFamiliar> leerFamiliares(Connection con, int funcionarioId) throws SQLException {
        List<MiembroFamiliar> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT_FAMILIARES)) {
            ps.setInt(1, funcionarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MiembroFamiliar m = new MiembroFamiliar();
                    m.setId(rs.getInt("id"));
                    m.setRolFamiliar(new Catalogo(rs.getInt("rol_id"), rs.getString("rol_nombre")));
                    m.setNombres(rs.getString("nombres"));
                    m.setApellidos(rs.getString("apellidos"));
                    m.setTipoIdentificacion(new Catalogo(rs.getInt("ti_id"), rs.getString("ti_nombre")));
                    m.setNumeroIdentificacion(rs.getString("numero_identificacion"));
                    m.setFechaNacimiento(rs.getObject("fecha_nacimiento", LocalDate.class));
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    private List<InformacionAcademica> leerEstudios(Connection con, int funcionarioId) throws SQLException {
        List<InformacionAcademica> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT_ESTUDIOS)) {
            ps.setInt(1, funcionarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InformacionAcademica e = new InformacionAcademica();
                    e.setId(rs.getInt("id"));
                    e.setUniversidad(rs.getString("universidad"));
                    e.setNivelEstudio(new Catalogo(rs.getInt("nivel_id"), rs.getString("nivel_nombre")));
                    e.setTitulo(rs.getString("titulo"));
                    lista.add(e);
                }
            }
        }
        return lista;
    }

    // ------------------------------------------------------------------
    // Traducción de errores
    // ------------------------------------------------------------------

    /**
     * Convierte una SQLException en una DAOException con un mensaje seguro para
     * el usuario. El detalle técnico (incluido el texto original de PostgreSQL)
     * se registra en el log, nunca se muestra en pantalla.
     */
    private DAOException traducir(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        LOG.log(Level.SEVERE, "Error SQL (SQLState " + estado + ")", e);
        String detalle = e.getMessage() == null ? "" : e.getMessage();

        String mensaje;
        switch (estado) {
            case "23505" -> { // unique_violation
                if (detalle.contains("uq_miembro_doc")) {
                    mensaje = "Ese familiar ya está registrado en el grupo familiar de este funcionario.";
                } else if (detalle.contains("uq_funcionario_doc")) {
                    mensaje = "Ya existe un funcionario con ese tipo y número de identificación.";
                } else {
                    mensaje = "Ya existe un registro con esos datos.";
                }
            }
            case "23503" -> mensaje = "La operación no se pudo completar porque hay datos relacionados.";
            case "23514" -> mensaje = "Un dato no cumple las reglas de la base de datos "
                    + "(por ejemplo, la edad mínima del funcionario o una fecha futura).";
            case "23502" -> mensaje = "Falta un dato obligatorio.";
            case "22001" -> mensaje = "Uno de los textos es demasiado largo.";
            case "42501" -> mensaje = "La aplicación no tiene permisos para realizar esta operación.";
            default -> {
                if (estado.startsWith("08")) {
                    mensaje = "Se perdió la conexión con la base de datos. Intente de nuevo.";
                } else {
                    mensaje = "Ocurrió un error al acceder a la base de datos.";
                }
            }
        }
        return new DAOException(mensaje, e);
    }
}

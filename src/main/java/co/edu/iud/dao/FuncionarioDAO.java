package co.edu.iud.dao;

import co.edu.iud.excepciones.DAOException;
import co.edu.iud.modelo.Funcionario;

import java.util.List;

/**
 * Contrato del patrón DAO para funcionarios.
 *
 * La vista solo conoce esta interfaz: no sabe si los datos vienen de
 * PostgreSQL, de otro motor o de una lista en memoria. Cambiar de motor
 * significa escribir otra implementación, sin tocar las ventanas.
 */
public interface FuncionarioDAO {

    /** Todos los funcionarios, sin grupo familiar ni estudios (para la tabla principal). */
    List<Funcionario> listar() throws DAOException;

    /** Filtra por nombre completo o número de identificación. Sin grupo familiar ni estudios. */
    List<Funcionario> buscar(String texto) throws DAOException;

    /** Un funcionario completo, con su grupo familiar y su información académica. */
    Funcionario buscarPorId(int id) throws DAOException;

    /** Guarda funcionario, familiares y estudios en una sola transacción. Asigna el id generado. */
    void crear(Funcionario funcionario) throws DAOException;

    /** Actualiza funcionario, familiares y estudios en una sola transacción. */
    void actualizar(Funcionario funcionario) throws DAOException;

    /** Elimina al funcionario; la base de datos borra en cascada sus familiares y estudios. */
    void eliminar(int id) throws DAOException;
}

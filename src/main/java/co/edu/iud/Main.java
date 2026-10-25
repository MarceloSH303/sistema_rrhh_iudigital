package co.edu.iud;

import co.edu.iud.conexion.ConexionDB;
import co.edu.iud.dao.FuncionarioDAOImpl;
import co.edu.iud.excepciones.DAOException;
import co.edu.iud.vista.VentanaPrincipal;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Punto de entrada. Antes de abrir la ventana comprueba que haya conexión con
 * PostgreSQL; si no la hay, explica el problema en lugar de mostrar una
 * ventana vacía.
 *
 * Ejecutar desde la raíz del proyecto (IntelliJ lo hace por defecto), porque
 * config.properties se busca en el directorio de trabajo.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Si falla, Swing usa su aspecto por defecto; no es un error grave.
            }

            try (Connection prueba = ConexionDB.getConnection()) {
                prueba.isValid(2);
            } catch (DAOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(),
                        "No se pudo iniciar la aplicación", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "No se pudo verificar la conexión.",
                        "No se pudo iniciar la aplicación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new VentanaPrincipal(new FuncionarioDAOImpl()).setVisible(true);
        });
    }
}

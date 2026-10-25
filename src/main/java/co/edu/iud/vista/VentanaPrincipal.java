package co.edu.iud.vista;

import co.edu.iud.dao.CatalogoDAO;
import co.edu.iud.dao.FuncionarioDAO;
import co.edu.iud.excepciones.DAOException;
import co.edu.iud.modelo.Funcionario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal: lista funcionarios y permite crear, editar y eliminar.
 *
 * La vista NO contiene SQL: solo llama a FuncionarioDAO y muestra los errores
 * (DAOException) con un cuadro de diálogo amable.
 */
public class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNAS = {
        "ID", "Tipo doc.", "Número", "Nombres", "Apellidos",
        "Estado civil", "Sexo", "Teléfono", "Nacimiento"
    };

    private final transient FuncionarioDAO funcionarioDAO;
    private final transient CatalogoDAO catalogoDAO = new CatalogoDAO();

    private final DefaultTableModel modeloTabla = new ModeloNoEditable(COLUMNAS) {
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> getColumnClass(int columna) {
            return columna == 0 ? Integer.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextField txtBuscar = new JTextField(25);
    private final JLabel lblEstado = new JLabel(" ");
    private transient List<Funcionario> funcionarios = new ArrayList<>();

    public VentanaPrincipal(FuncionarioDAO funcionarioDAO) {
        super("Gestión de funcionarios - Recursos Humanos IU Digital");
        this.funcionarioDAO = funcionarioDAO;
        construirInterfaz();
        cargar(null);
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 560);
        setLocationRelativeTo(null);

        // Barra superior: búsqueda
        JButton btnBuscar = new JButton("Buscar");
        JButton btnLimpiar = new JButton("Limpiar");
        btnBuscar.addActionListener(e -> cargar(txtBuscar.getText()));
        txtBuscar.addActionListener(e -> cargar(txtBuscar.getText()));
        btnLimpiar.addActionListener(e -> {
            txtBuscar.setText("");
            cargar(null);
        });
        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        superior.add(new JLabel("Buscar por nombre o documento:"));
        superior.add(txtBuscar);
        superior.add(btnBuscar);
        superior.add(btnLimpiar);

        // Tabla
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        tabla.getColumnModel().getColumn(1).setPreferredWidth(170); // Tipo de documento
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editar();
                }
            }
        });

        // Barra inferior: acciones
        JButton btnNuevo = new JButton("Nuevo");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnActualizar = new JButton("Actualizar lista");
        btnNuevo.addActionListener(e -> nuevo());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnActualizar.addActionListener(e -> cargar(txtBuscar.getText()));
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        acciones.add(btnNuevo);
        acciones.add(btnEditar);
        acciones.add(btnEliminar);
        acciones.add(btnActualizar);

        JPanel inferior = new JPanel(new BorderLayout());
        inferior.add(acciones, BorderLayout.WEST);
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
        inferior.add(lblEstado, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(superior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(inferior, BorderLayout.SOUTH);
    }

    // ---------- operaciones ----------

    private void cargar(String filtro) {
        try {
            funcionarios = (filtro == null || filtro.isBlank())
                    ? funcionarioDAO.listar()
                    : funcionarioDAO.buscar(filtro);
        } catch (DAOException e) {
            mostrarError(e);
            return;
        }
        modeloTabla.setRowCount(0);
        for (Funcionario f : funcionarios) {
            modeloTabla.addRow(new Object[]{
                f.getId(),
                f.getTipoIdentificacion().getNombre(),
                f.getNumeroIdentificacion(),
                f.getNombres(),
                f.getApellidos(),
                f.getEstadoCivil().getNombre(),
                f.getSexo().getNombre(),
                f.getTelefono(),
                f.getFechaNacimiento()
            });
        }
        lblEstado.setText(funcionarios.size() + " funcionario(s)");
    }

    private void nuevo() {
        abrirFormulario(null);
    }

    private void editar() {
        Funcionario seleccionado = seleccionado();
        if (seleccionado == null) {
            return;
        }
        try {
            // Se vuelve a consultar para traer también familiares y estudios.
            abrirFormulario(funcionarioDAO.buscarPorId(seleccionado.getId()));
        } catch (DAOException e) {
            mostrarError(e);
            cargar(txtBuscar.getText());
        }
    }

    private void eliminar() {
        Funcionario seleccionado = seleccionado();
        if (seleccionado == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + seleccionado.getNombreCompleto() + "?\n"
                + "También se eliminarán su grupo familiar y su información académica.\n"
                + "Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            funcionarioDAO.eliminar(seleccionado.getId());
            cargar(txtBuscar.getText());
        } catch (DAOException e) {
            mostrarError(e);
        }
    }

    private void abrirFormulario(Funcionario existente) {
        try {
            FuncionarioDialog dialogo =
                    new FuncionarioDialog(this, funcionarioDAO, catalogoDAO, existente);
            dialogo.setVisible(true);
            if (dialogo.fueGuardado()) {
                cargar(txtBuscar.getText());
            }
        } catch (DAOException e) {
            mostrarError(e);
        }
    }

    private Funcionario seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un funcionario de la tabla.",
                    "Sin selección", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        // La tabla puede estar ordenada: se convierte la fila visible a la fila del modelo.
        return funcionarios.get(tabla.convertRowIndexToModel(fila));
    }

    private void mostrarError(DAOException e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

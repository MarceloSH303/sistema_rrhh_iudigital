package co.edu.iud.vista;

import co.edu.iud.dao.CatalogoDAO;
import co.edu.iud.dao.CatalogoDAO.Tipo;
import co.edu.iud.dao.FuncionarioDAO;
import co.edu.iud.excepciones.DAOException;
import co.edu.iud.excepciones.ValidacionException;
import co.edu.iud.modelo.Catalogo;
import co.edu.iud.modelo.Funcionario;
import co.edu.iud.modelo.InformacionAcademica;
import co.edu.iud.modelo.MiembroFamiliar;
import co.edu.iud.util.Validador;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulario para crear o editar un funcionario, con tres pestañas:
 * datos personales, grupo familiar e información académica.
 *
 * Flujo de "Guardar": leer el formulario, validar (Validador) y solo entonces
 * llamar al DAO. Los errores de validación y los de base de datos se muestran
 * por separado.
 */
public class FuncionarioDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final String AYUDA_FECHA = "AAAA-MM-DD";

    private final transient FuncionarioDAO funcionarioDAO;
    private final transient Funcionario original; // null cuando se está creando
    private boolean guardado = false;

    // Datos personales
    private final JComboBox<Catalogo> cmbTipoDoc;
    private final JComboBox<Catalogo> cmbEstadoCivil;
    private final JComboBox<Catalogo> cmbSexo;
    private final JTextField txtNumero = new JTextField(20);
    private final JTextField txtNombres = new JTextField(30);
    private final JTextField txtApellidos = new JTextField(30);
    private final JTextField txtDireccion = new JTextField(30);
    private final JTextField txtTelefono = new JTextField(20);
    private final JTextField txtNacimiento = new JTextField(AYUDA_FECHA.length());

    // Grupo familiar
    private final transient List<MiembroFamiliar> familiares = new ArrayList<>();
    private final DefaultTableModel modeloFamiliares = new ModeloNoEditable(new String[]{
        "Rol", "Tipo doc.", "Número", "Nombres", "Apellidos", "Nacimiento"});
    private final JTable tablaFamiliares = new JTable(modeloFamiliares);
    private final JComboBox<Catalogo> cmbRolFam;
    private final JComboBox<Catalogo> cmbTipoDocFam;
    private final JTextField txtNumeroFam = new JTextField();
    private final JTextField txtNombresFam = new JTextField();
    private final JTextField txtApellidosFam = new JTextField();
    private final JTextField txtNacimientoFam = new JTextField();

    // Información académica
    private final transient List<InformacionAcademica> estudios = new ArrayList<>();
    private final DefaultTableModel modeloEstudios = new ModeloNoEditable(new String[]{
        "Universidad", "Nivel de estudio", "Título"});
    private final JTable tablaEstudios = new JTable(modeloEstudios);
    private final JTextField txtUniversidad = new JTextField();
    private final JComboBox<Catalogo> cmbNivel;
    private final JTextField txtTitulo = new JTextField();

    public FuncionarioDialog(Frame owner, FuncionarioDAO funcionarioDAO, CatalogoDAO catalogoDAO,
                             Funcionario existente) throws DAOException {
        super(owner, existente == null ? "Nuevo funcionario" : "Editar funcionario", true);
        this.funcionarioDAO = funcionarioDAO;
        this.original = existente;

        List<Catalogo> tipos = catalogoDAO.listar(Tipo.TIPO_IDENTIFICACION);
        cmbTipoDoc = crearCombo(tipos);
        cmbTipoDocFam = crearCombo(tipos);
        cmbEstadoCivil = crearCombo(catalogoDAO.listar(Tipo.ESTADO_CIVIL));
        cmbSexo = crearCombo(catalogoDAO.listar(Tipo.SEXO));
        cmbRolFam = crearCombo(catalogoDAO.listar(Tipo.ROL_FAMILIAR));
        cmbNivel = crearCombo(catalogoDAO.listar(Tipo.NIVEL_ESTUDIO));

        construirInterfaz();
        if (existente != null) {
            cargar(existente);
        }
        setLocationRelativeTo(owner);
    }

    public boolean fueGuardado() {
        return guardado;
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private void construirInterfaz() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1040, 560);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Datos personales", panelDatos());
        pestanas.addTab("Grupo familiar", panelFamiliares());
        pestanas.addTab("Información académica", panelEstudios());

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        botones.add(btnGuardar);
        botones.add(btnCancelar);

        setLayout(new BorderLayout());
        add(pestanas, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnGuardar);
    }

    private JPanel panelDatos() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        int fila = 0;
        agregarFila(p, gc, fila++, "Tipo de identificación *", cmbTipoDoc);
        agregarFila(p, gc, fila++, "Número de identificación *", txtNumero);
        agregarFila(p, gc, fila++, "Nombres *", txtNombres);
        agregarFila(p, gc, fila++, "Apellidos *", txtApellidos);
        agregarFila(p, gc, fila++, "Estado civil *", cmbEstadoCivil);
        agregarFila(p, gc, fila++, "Sexo *", cmbSexo);
        agregarFila(p, gc, fila++, "Dirección *", txtDireccion);
        agregarFila(p, gc, fila++, "Teléfono *", txtTelefono);
        agregarFila(p, gc, fila++, "Fecha de nacimiento * (" + AYUDA_FECHA + ")", txtNacimiento);

        // Fila vacía que empuja todo hacia arriba
        gc.gridx = 0;
        gc.gridy = fila;
        gc.weighty = 1;
        p.add(new JLabel(), gc);
        return p;
    }

    private void agregarFila(JPanel p, GridBagConstraints gc, int fila, String etiqueta,
                             JComponent campo) {
        gc.gridy = fila;
        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        p.add(new JLabel(etiqueta), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        p.add(campo, gc);
    }

    private JPanel panelFamiliares() {
        JPanel form = new JPanel(new GridLayout(2, 7, 6, 2));
        for (String t : new String[]{"Rol", "Tipo doc.", "Número", "Nombres", "Apellidos",
            "Nacimiento", ""}) {
            form.add(new JLabel(t));
        }
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregarFamiliar());
        form.add(cmbRolFam);
        form.add(cmbTipoDocFam);
        form.add(txtNumeroFam);
        form.add(txtNombresFam);
        form.add(txtApellidosFam);
        form.add(txtNacimientoFam);
        form.add(btnAgregar);

        tablaFamiliares.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JButton btnQuitar = new JButton("Quitar seleccionado");
        btnQuitar.addActionListener(e -> quitarFamiliar());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(btnQuitar);

        JPanel encabezado = new JPanel(new BorderLayout(0, 4));
        encabezado.add(form, BorderLayout.CENTER);
        encabezado.add(new JLabel("Fecha de nacimiento en formato " + AYUDA_FECHA
                + ". Los cambios se guardan al pulsar «Guardar»."), BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(encabezado, BorderLayout.NORTH);
        p.add(new JScrollPane(tablaFamiliares), BorderLayout.CENTER);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private JPanel panelEstudios() {
        JPanel form = new JPanel(new GridLayout(2, 4, 6, 2));
        for (String t : new String[]{"Universidad", "Nivel de estudio", "Título", ""}) {
            form.add(new JLabel(t));
        }
        JButton btnAgregar = new JButton("Agregar");
        btnAgregar.addActionListener(e -> agregarEstudio());
        form.add(txtUniversidad);
        form.add(cmbNivel);
        form.add(txtTitulo);
        form.add(btnAgregar);

        tablaEstudios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JButton btnQuitar = new JButton("Quitar seleccionado");
        btnQuitar.addActionListener(e -> quitarEstudio());
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(btnQuitar);

        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.add(form, BorderLayout.NORTH);
        p.add(new JScrollPane(tablaEstudios), BorderLayout.CENTER);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private static JComboBox<Catalogo> crearCombo(List<Catalogo> opciones) {
        return new JComboBox<>(opciones.toArray(new Catalogo[0]));
    }

    // ------------------------------------------------------------------
    // Grupo familiar y estudios (listas en memoria hasta pulsar Guardar)
    // ------------------------------------------------------------------

    private void agregarFamiliar() {
        try {
            MiembroFamiliar m = new MiembroFamiliar();
            m.setRolFamiliar((Catalogo) cmbRolFam.getSelectedItem());
            m.setTipoIdentificacion((Catalogo) cmbTipoDocFam.getSelectedItem());
            m.setNumeroIdentificacion(txtNumeroFam.getText());
            m.setNombres(txtNombresFam.getText());
            m.setApellidos(txtApellidosFam.getText());
            m.setFechaNacimiento(Validador.parsearFecha(txtNacimientoFam.getText(),
                    "fecha de nacimiento del familiar"));
            Validador.validarMiembro(m);
            familiares.add(m);
            agregarFilaFamiliar(m);
            for (JTextField campo : new JTextField[]{txtNumeroFam, txtNombresFam,
                txtApellidosFam, txtNacimientoFam}) {
                campo.setText("");
            }
        } catch (ValidacionException e) {
            avisar(e.getMessage());
        }
    }

    private void agregarFilaFamiliar(MiembroFamiliar m) {
        modeloFamiliares.addRow(new Object[]{
            m.getRolFamiliar(), m.getTipoIdentificacion(), m.getNumeroIdentificacion(),
            m.getNombres(), m.getApellidos(), m.getFechaNacimiento()});
    }

    private void quitarFamiliar() {
        int fila = tablaFamiliares.getSelectedRow();
        if (fila >= 0) {
            familiares.remove(fila);
            modeloFamiliares.removeRow(fila);
        }
    }

    private void agregarEstudio() {
        try {
            InformacionAcademica e = new InformacionAcademica();
            e.setUniversidad(txtUniversidad.getText());
            e.setNivelEstudio((Catalogo) cmbNivel.getSelectedItem());
            e.setTitulo(txtTitulo.getText());
            Validador.validarEstudio(e);
            estudios.add(e);
            agregarFilaEstudio(e);
            txtUniversidad.setText("");
            txtTitulo.setText("");
        } catch (ValidacionException ex) {
            avisar(ex.getMessage());
        }
    }

    private void agregarFilaEstudio(InformacionAcademica e) {
        modeloEstudios.addRow(new Object[]{e.getUniversidad(), e.getNivelEstudio(), e.getTitulo()});
    }

    private void quitarEstudio() {
        int fila = tablaEstudios.getSelectedRow();
        if (fila >= 0) {
            estudios.remove(fila);
            modeloEstudios.removeRow(fila);
        }
    }

    // ------------------------------------------------------------------
    // Cargar y guardar
    // ------------------------------------------------------------------

    private void cargar(Funcionario f) {
        cmbTipoDoc.setSelectedItem(f.getTipoIdentificacion());
        txtNumero.setText(f.getNumeroIdentificacion());
        txtNombres.setText(f.getNombres());
        txtApellidos.setText(f.getApellidos());
        cmbEstadoCivil.setSelectedItem(f.getEstadoCivil());
        cmbSexo.setSelectedItem(f.getSexo());
        txtDireccion.setText(f.getDireccion());
        txtTelefono.setText(f.getTelefono());
        txtNacimiento.setText(String.valueOf(f.getFechaNacimiento()));
        for (MiembroFamiliar m : f.getFamiliares()) {
            familiares.add(m);
            agregarFilaFamiliar(m);
        }
        for (InformacionAcademica e : f.getEstudios()) {
            estudios.add(e);
            agregarFilaEstudio(e);
        }
    }

    private Funcionario leerFormulario() throws ValidacionException {
        Funcionario f = new Funcionario();
        f.setId(original == null ? null : original.getId());
        f.setTipoIdentificacion((Catalogo) cmbTipoDoc.getSelectedItem());
        f.setNumeroIdentificacion(txtNumero.getText());
        f.setNombres(txtNombres.getText());
        f.setApellidos(txtApellidos.getText());
        f.setEstadoCivil((Catalogo) cmbEstadoCivil.getSelectedItem());
        f.setSexo((Catalogo) cmbSexo.getSelectedItem());
        f.setDireccion(txtDireccion.getText());
        f.setTelefono(txtTelefono.getText());
        f.setFechaNacimiento(Validador.parsearFecha(txtNacimiento.getText(), "fecha de nacimiento"));
        f.setFamiliares(new ArrayList<>(familiares));
        f.setEstudios(new ArrayList<>(estudios));
        return f;
    }

    private void guardar() {
        try {
            Funcionario f = leerFormulario();
            Validador.validar(f);
            if (f.getId() == null) {
                funcionarioDAO.crear(f);
            } else {
                funcionarioDAO.actualizar(f);
            }
            guardado = true;
            dispose();
        } catch (ValidacionException e) {
            avisar(e.getMessage());
        } catch (DAOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Revise los datos", JOptionPane.WARNING_MESSAGE);
    }
}

package co.edu.iud.vista;

import javax.swing.table.DefaultTableModel;

/** Modelo de tabla de solo lectura: los datos se editan en el formulario, no en la celda. */
class ModeloNoEditable extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    ModeloNoEditable(String[] columnas) {
        super(columnas, 0);
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }
}

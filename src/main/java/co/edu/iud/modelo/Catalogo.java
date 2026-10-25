package co.edu.iud.modelo;

import java.util.Objects;

/**
 * Fila de cualquiera de las tablas de catálogo (tipo_identificacion,
 * estado_civil, sexo, rol_familiar, nivel_estudio): todas tienen id y nombre.
 *
 * toString() devuelve el nombre para que los JComboBox muestren texto legible.
 */
public class Catalogo {

    private final int id;
    private final String nombre;

    public Catalogo(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Catalogo otro && otro.id == id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

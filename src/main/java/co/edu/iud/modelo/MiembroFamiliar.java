package co.edu.iud.modelo;

import java.time.LocalDate;

/** Integrante del grupo familiar de un funcionario. */
public class MiembroFamiliar {

    private Integer id;
    private Catalogo rolFamiliar;
    private String nombres;
    private String apellidos;
    private Catalogo tipoIdentificacion;
    private String numeroIdentificacion;
    private LocalDate fechaNacimiento;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Catalogo getRolFamiliar() {
        return rolFamiliar;
    }

    public void setRolFamiliar(Catalogo rolFamiliar) {
        this.rolFamiliar = rolFamiliar;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Catalogo getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(Catalogo tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}

package co.edu.iud.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Funcionario de la institución, con su grupo familiar y su información académica. */
public class Funcionario {

    private Integer id; // null mientras no se haya guardado
    private Catalogo tipoIdentificacion;
    private String numeroIdentificacion;
    private String nombres;
    private String apellidos;
    private Catalogo estadoCivil;
    private Catalogo sexo;
    private String direccion;
    private String telefono;
    private LocalDate fechaNacimiento;
    private List<MiembroFamiliar> familiares = new ArrayList<>();
    private List<InformacionAcademica> estudios = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Catalogo getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(Catalogo estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public Catalogo getSexo() {
        return sexo;
    }

    public void setSexo(Catalogo sexo) {
        this.sexo = sexo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public List<MiembroFamiliar> getFamiliares() {
        return familiares;
    }

    public void setFamiliares(List<MiembroFamiliar> familiares) {
        this.familiares = familiares;
    }

    public List<InformacionAcademica> getEstudios() {
        return estudios;
    }

    public void setEstudios(List<InformacionAcademica> estudios) {
        this.estudios = estudios;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}

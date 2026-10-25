package co.edu.iud.modelo;

/** Un estudio realizado por un funcionario. */
public class InformacionAcademica {

    private Integer id;
    private String universidad;
    private Catalogo nivelEstudio;
    private String titulo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUniversidad() {
        return universidad;
    }

    public void setUniversidad(String universidad) {
        this.universidad = universidad;
    }

    public Catalogo getNivelEstudio() {
        return nivelEstudio;
    }

    public void setNivelEstudio(Catalogo nivelEstudio) {
        this.nivelEstudio = nivelEstudio;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}

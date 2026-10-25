package co.edu.iud.util;

import co.edu.iud.excepciones.ValidacionException;
import co.edu.iud.modelo.Catalogo;
import co.edu.iud.modelo.Funcionario;
import co.edu.iud.modelo.InformacionAcademica;
import co.edu.iud.modelo.MiembroFamiliar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validación de entradas ANTES de llegar al DAO (sesión 3: lista blanca, no
 * lista negra). Cada regla define qué SÍ se acepta; todo lo demás se rechaza.
 *
 * Los métodos validar* además "limpian" los textos (quitan espacios sobrantes)
 * directamente en el objeto recibido. Los límites de longitud coinciden con los
 * VARCHAR del script crear_bd.sql, y la edad mínima con el CHECK de funcionario.
 */
public final class Validador {

    public static final int EDAD_MINIMA_FUNCIONARIO = 18;

    private static final Pattern NOMBRE = Pattern.compile("^\\p{L}[\\p{L} .'-]*$");
    private static final Pattern DOCUMENTO = Pattern.compile("^[A-Za-z0-9]{4,20}$");
    private static final Pattern TELEFONO = Pattern.compile("^\\+?[0-9 ]{7,20}$");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final LocalDate FECHA_MINIMA = LocalDate.of(1900, 1, 1);

    private Validador() {
        // Clase de utilidad: no se instancia.
    }

    /** Convierte "AAAA-MM-DD" en LocalDate; rechaza fechas imposibles como 2023-02-31. */
    public static LocalDate parsearFecha(String texto, String campo) throws ValidacionException {
        if (texto == null || texto.isBlank()) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        try {
            return LocalDate.parse(texto.trim(), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new ValidacionException("El campo " + campo
                    + " debe tener el formato AAAA-MM-DD (por ejemplo 1990-05-23) y ser una fecha real.");
        }
    }

    public static void validar(Funcionario f) throws ValidacionException {
        seleccionado(f.getTipoIdentificacion(), "tipo de identificación");
        f.setNumeroIdentificacion(documento(f.getNumeroIdentificacion(), "número de identificación"));
        f.setNombres(nombre(f.getNombres(), "nombres"));
        f.setApellidos(nombre(f.getApellidos(), "apellidos"));
        seleccionado(f.getEstadoCivil(), "estado civil");
        seleccionado(f.getSexo(), "sexo");
        f.setDireccion(texto(f.getDireccion(), "dirección", 255));
        f.setTelefono(telefono(f.getTelefono()));

        LocalDate nacimiento = f.getFechaNacimiento();
        if (nacimiento == null) {
            throw new ValidacionException("La fecha de nacimiento es obligatoria.");
        }
        if (nacimiento.isBefore(FECHA_MINIMA)) {
            throw new ValidacionException("La fecha de nacimiento no es válida.");
        }
        if (nacimiento.isAfter(LocalDate.now().minusYears(EDAD_MINIMA_FUNCIONARIO))) {
            throw new ValidacionException("El funcionario debe ser mayor de "
                    + EDAD_MINIMA_FUNCIONARIO + " años.");
        }

        Set<String> vistos = new HashSet<>();
        for (MiembroFamiliar m : f.getFamiliares()) {
            validarMiembro(m);
            String clave = m.getTipoIdentificacion().getId() + ":"
                    + m.getNumeroIdentificacion().toUpperCase(Locale.ROOT);
            if (!vistos.add(clave)) {
                throw new ValidacionException("El familiar con documento "
                        + m.getNumeroIdentificacion() + " está repetido en el grupo familiar.");
            }
            if (m.getTipoIdentificacion().equals(f.getTipoIdentificacion())
                    && m.getNumeroIdentificacion().equalsIgnoreCase(f.getNumeroIdentificacion())) {
                throw new ValidacionException(
                        "Un familiar no puede tener el mismo documento que el funcionario.");
            }
        }
        for (InformacionAcademica e : f.getEstudios()) {
            validarEstudio(e);
        }
    }

    public static void validarMiembro(MiembroFamiliar m) throws ValidacionException {
        seleccionado(m.getRolFamiliar(), "rol del familiar");
        m.setNombres(nombre(m.getNombres(), "nombres del familiar"));
        m.setApellidos(nombre(m.getApellidos(), "apellidos del familiar"));
        seleccionado(m.getTipoIdentificacion(), "tipo de identificación del familiar");
        m.setNumeroIdentificacion(documento(m.getNumeroIdentificacion(),
                "número de identificación del familiar"));
        LocalDate nacimiento = m.getFechaNacimiento();
        if (nacimiento == null) {
            throw new ValidacionException("La fecha de nacimiento del familiar es obligatoria.");
        }
        if (nacimiento.isBefore(FECHA_MINIMA) || nacimiento.isAfter(LocalDate.now())) {
            throw new ValidacionException("La fecha de nacimiento del familiar no es válida.");
        }
    }

    public static void validarEstudio(InformacionAcademica e) throws ValidacionException {
        e.setUniversidad(texto(e.getUniversidad(), "universidad", 150));
        seleccionado(e.getNivelEstudio(), "nivel de estudio");
        e.setTitulo(texto(e.getTitulo(), "título", 150));
    }

    // ---------- reglas básicas ----------

    private static String texto(String valor, String campo, int max) throws ValidacionException {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw new ValidacionException("El campo " + campo + " no puede superar "
                    + max + " caracteres.");
        }
        return limpio;
    }

    private static String nombre(String valor, String campo) throws ValidacionException {
        String limpio = texto(valor, campo, 100);
        if (!NOMBRE.matcher(limpio).matches()) {
            throw new ValidacionException("El campo " + campo
                    + " solo admite letras, espacios, puntos, apóstrofos y guiones.");
        }
        return limpio;
    }

    private static String documento(String valor, String campo) throws ValidacionException {
        String limpio = texto(valor, campo, 20);
        if (!DOCUMENTO.matcher(limpio).matches()) {
            throw new ValidacionException("El campo " + campo
                    + " debe tener entre 4 y 20 caracteres, solo letras y números.");
        }
        return limpio;
    }

    private static String telefono(String valor) throws ValidacionException {
        String limpio = texto(valor, "teléfono", 20);
        if (!TELEFONO.matcher(limpio).matches()) {
            throw new ValidacionException("El teléfono debe tener entre 7 y 20 dígitos "
                    + "(puede iniciar con + y llevar espacios).");
        }
        return limpio;
    }

    private static void seleccionado(Catalogo valor, String campo) throws ValidacionException {
        if (valor == null) {
            throw new ValidacionException("Seleccione el " + campo + ".");
        }
    }
}

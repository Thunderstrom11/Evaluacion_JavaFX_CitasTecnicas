package ni.edu.uam.evaluacion_javafx_citastecnicas.util;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

//validacion
public class Validador {

    public static final List<String> ESTADOS =
            List.of("Pendiente", "En proceso", "Finalizada", "Cancelada");

    private static final String REGEX_CORREO = "^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$";

    private Validador() {
    }

    public static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    public static boolean correoValido(String correo) {
        return correo != null && correo.trim().matches(REGEX_CORREO);
    }

    // Telefono de 8 digitos formato local de Nicaragua.
    public static boolean telefonoValido(String telefono) {
        return telefono != null && telefono.trim().matches("\\d{8}");
    }

    // devolver null si no es una hora valida.
    public static LocalTime parsearHora(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return LocalTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static boolean estadoValido(String estado) {
        return estado != null && ESTADOS.contains(estado);
    }
}
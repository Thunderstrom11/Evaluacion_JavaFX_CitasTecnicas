package ni.edu.uam.evaluacion_javafx_citastecnicas.model;

import lombok.Data;

@Data
public class Cliente {

    private Integer id;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;
    private boolean activo;

    //Se usa en los mensajes y en la tabla de citas
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    //Hace a que el ComboBox de citas muestre el nombre completo
    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
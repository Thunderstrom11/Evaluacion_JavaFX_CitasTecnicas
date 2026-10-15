package ni.edu.uam.evaluacion_javafx_citastecnicas.model;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CitaTecnica {

    private Integer id;
    private Integer clienteId;
    private String nombreCliente;   // lo llena el INNER JOIN del DAO
    private LocalDate fecha;
    private LocalTime hora;
    private String tipoServicio;
    private String descripcion;
    private String estado;
}
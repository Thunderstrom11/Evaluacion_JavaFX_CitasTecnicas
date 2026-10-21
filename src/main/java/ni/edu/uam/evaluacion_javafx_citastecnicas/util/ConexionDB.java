package ni.edu.uam.evaluacion_javafx_citastecnicas.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


//unica clase que conoce la URL, el usuario y la clave del postgre todos los dao piden conexion aca.

public class ConexionDB {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/gestion_citas";
    private static final String USUARIO = "postgres";
    private static final String CLAVE = "diegoch1207";

    private ConexionDB() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    //Comprueba la conexion con una sentencia fija usando Statement.
    public static boolean probarConexion() throws SQLException {
        try (Connection cn = getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1")) {

            return rs.next();
        }
    }
}
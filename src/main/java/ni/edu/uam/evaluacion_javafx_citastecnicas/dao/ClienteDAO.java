package ni.edu.uam.evaluacion_javafx_citastecnicas.dao;

import lombok.Getter;
import ni.edu.uam.evaluacion_javafx_citastecnicas.model.Cliente;
import ni.edu.uam.evaluacion_javafx_citastecnicas.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//Acceso a datos de la tabla cliente usando PreparedStatement con parametros

@Getter
public class ClienteDAO {

    private String mensajeError = "";

    private Cliente convertir(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setTelefono(rs.getString("telefono"));
        c.setCorreo(rs.getString("correo"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }

    // ------------------------- CREATE -------------------------

    public boolean guardar(Cliente c) {
        String sql = """
                INSERT INTO cliente (nombres, apellidos, telefono, correo, activo)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, c.getNombres());
            ps.setString(2, c.getApellidos());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setBoolean(5, c.isActivo());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return false;
        }
    }

    // -------------------------- READ --------------------------

    public List<Cliente> listar() {
        String sql = """
                SELECT id, nombres, apellidos, telefono, correo, activo
                FROM cliente
                ORDER BY apellidos, nombres
                """;

        List<Cliente> lista = new ArrayList<>();

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(convertir(rs));
            }

        } catch (SQLException e) {
            mensajeError = traducir(e);
        }
        return lista;
    }

    // Busqueda por coincidencia en nombres, apellidos o correo
    public List<Cliente> buscar(String texto) {
        String sql = """
                SELECT id, nombres, apellidos, telefono, correo, activo
                FROM cliente
                WHERE LOWER(nombres)   LIKE LOWER(?)
                   OR LOWER(apellidos) LIKE LOWER(?)
                   OR LOWER(correo)    LIKE LOWER(?)
                ORDER BY apellidos, nombres
                """;

        List<Cliente> lista = new ArrayList<>();
        String patron = "%" + texto.trim() + "%";

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(convertir(rs));
                }
            }

        } catch (SQLException e) {
            mensajeError = traducir(e);
        }
        return lista;
    }

    public int contar() {
        String sql = "SELECT COUNT(*) FROM cliente";

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return 0;
        }
    }

    // ------------------------- UPDATE -------------------------

    public boolean actualizar(Cliente c) {
        String sql = """
                UPDATE cliente
                   SET nombres = ?, apellidos = ?, telefono = ?, correo = ?, activo = ?
                 WHERE id = ?
                """;

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, c.getNombres());
            ps.setString(2, c.getApellidos());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setBoolean(5, c.isActivo());
            ps.setInt(6, c.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return false;
        }
    }

    // ------------------------- DELETE -------------------------

    public boolean eliminar(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return false;
        }
    }

    // --------------------- Reglas de negocio ------------------

    //En caso de un correo duplicado. idExcluir = null cuando es un registro nuevo.
    public boolean existeCorreo(String correo, Integer idExcluir) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE LOWER(correo) = LOWER(?)";
        if (idExcluir != null) {
            sql += " AND id <> ?";   // al editar, el propio registro no cuenta
        }

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, correo.trim());
            if (idExcluir != null) {
                ps.setInt(2, idExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return false;
        }
    }

    //No poder eliminar un cliente con citas asociadas
    public boolean tieneCitas(int clienteId) {
        String sql = "SELECT COUNT(*) FROM cita_tecnica WHERE cliente_id = ?";

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return false;
        }
    }

    //Traductor de codigos de Error SQL
    private String traducir(SQLException e) {
        return switch (e.getSQLState()) {
            case "23505" -> "Ya existe un cliente registrado con ese correo.";
            case "23503" -> "El cliente tiene citas asociadas; no se puede eliminar.";
            case "23502" -> "Faltan datos obligatorios del cliente.";
            case "22P02" -> "Algun dato tiene un formato invalido.";
            default -> "Error de base de datos: " + e.getMessage();
        };
    }
}
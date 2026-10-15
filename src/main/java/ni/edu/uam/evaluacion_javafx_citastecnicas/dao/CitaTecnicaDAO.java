package ni.edu.uam.evaluacion_javafx_citastecnicas.dao;

import ni.edu.uam.evaluacion_javafx_citastecnicas.model.CitaTecnica;
import ni.edu.uam.evaluacion_javafx_citastecnicas.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//Acceso a datos de la tabla cita_tecnica.
public class CitaTecnicaDAO {

    private String mensajeError = "";

    public String getMensajeError() {
        return mensajeError;
    }

    private static final String SELECT_BASE = """
            SELECT c.id,
                   c.cliente_id,
                   c.fecha,
                   c.hora,
                   c.tipo_servicio,
                   c.descripcion,
                   c.estado,
                   cl.nombres || ' ' || cl.apellidos AS nombre_cliente
            FROM cita_tecnica c
            INNER JOIN cliente cl ON cl.id = c.cliente_id
            """;

    private CitaTecnica convertir(ResultSet rs) throws SQLException {
        CitaTecnica c = new CitaTecnica();
        c.setId(rs.getInt("id"));
        c.setClienteId(rs.getInt("cliente_id"));
        c.setNombreCliente(rs.getString("nombre_cliente"));
        c.setFecha(rs.getDate("fecha").toLocalDate());
        c.setHora(rs.getTime("hora").toLocalTime());
        c.setTipoServicio(rs.getString("tipo_servicio"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setEstado(rs.getString("estado"));
        return c;
    }

//create
    public boolean guardar(CitaTecnica c) {
        String sql = """
                INSERT INTO cita_tecnica
                    (cliente_id, fecha, hora, tipo_servicio, descripcion, estado)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, c.getClienteId());
            ps.setObject(2, c.getFecha());
            ps.setObject(3, c.getHora());
            ps.setString(4, c.getTipoServicio());
            ps.setString(5, c.getDescripcion());
            ps.setString(6, c.getEstado());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return false;
        }
    }

//read
    public List<CitaTecnica> listar() {
        String sql = SELECT_BASE + " ORDER BY c.fecha DESC, c.hora DESC";

        List<CitaTecnica> lista = new ArrayList<>();

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

    //filtra por estado y/o fecha.
    public List<CitaTecnica> filtrar(String estado, LocalDate fecha) {
        String sql = SELECT_BASE + " WHERE 1 = 1";
        if (estado != null) {
            sql += " AND c.estado = ?";
        }
        if (fecha != null) {
            sql += " AND c.fecha = ?";
        }
        sql += " ORDER BY c.fecha DESC, c.hora DESC";

        List<CitaTecnica> lista = new ArrayList<>();

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            int i = 1;
            if (estado != null) {
                ps.setString(i++, estado);
            }
            if (fecha != null) {
                ps.setObject(i, fecha);
            }

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
        String sql = "SELECT COUNT(*) FROM cita_tecnica";

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return 0;
        }
    }

//update
    public boolean actualizar(CitaTecnica c) {
        String sql = """
                UPDATE cita_tecnica
                   SET cliente_id = ?, fecha = ?, hora = ?, tipo_servicio = ?,
                       descripcion = ?, estado = ?
                 WHERE id = ?
                """;

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, c.getClienteId());
            ps.setObject(2, c.getFecha());
            ps.setObject(3, c.getHora());
            ps.setString(4, c.getTipoServicio());
            ps.setString(5, c.getDescripcion());
            ps.setString(6, c.getEstado());
            ps.setInt(7, c.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return false;
        }
    }

//delete
    public boolean eliminar(int id) {
        String sql = "DELETE FROM cita_tecnica WHERE id = ?";

        try (Connection cn = ConexionDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducir(e);
            return false;
        }
    }

    private String traducir(SQLException e) {
        return switch (e.getSQLState()) {
            case "23503" -> "El cliente seleccionado no existe.";
            case "23502" -> "Faltan datos obligatorios de la cita.";
            case "23514" -> "El estado debe ser Pendiente, En proceso, Finalizada o Cancelada.";
            case "22P02" -> "Algun dato tiene un formato invalido.";
            default -> "Error de base de datos: " + e.getMessage();
        };
    }
}
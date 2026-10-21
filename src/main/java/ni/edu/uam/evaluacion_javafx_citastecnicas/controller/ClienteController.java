package ni.edu.uam.evaluacion_javafx_citastecnicas.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import ni.edu.uam.evaluacion_javafx_citastecnicas.dao.ClienteDAO;
import ni.edu.uam.evaluacion_javafx_citastecnicas.model.Cliente;
import ni.edu.uam.evaluacion_javafx_citastecnicas.util.AlertUtils;
import ni.edu.uam.evaluacion_javafx_citastecnicas.util.HiloCarga;
import ni.edu.uam.evaluacion_javafx_citastecnicas.util.Validador;
import javafx.application.Platform;
import java.util.List;

public class ClienteController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private CheckBox chkActivo;
    @FXML private TextField txtBuscar;

    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, Number> colId;
    @FXML private TableColumn<Cliente, String> colNombres;
    @FXML private TableColumn<Cliente, String> colApellidos;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private TableColumn<Cliente, Boolean> colActivo;

    private final ClienteDAO dao = new ClienteDAO();
    private final ObservableList<Cliente> datos = FXCollections.observableArrayList();

    // Lista que ve la tabla
    private FilteredList<Cliente> filtrados;

    // -1 significa que no hay ninguna fila en edicion
    private int idEnEdicion = -1;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(f -> new javafx.beans.property.SimpleIntegerProperty(f.getValue().getId()));
        colNombres.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getNombres()));
        colApellidos.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getApellidos()));
        colTelefono.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getTelefono()));
        colCorreo.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getCorreo()));
        colActivo.setCellValueFactory(f ->
                new javafx.beans.property.SimpleBooleanProperty(f.getValue().isActivo()));

        filtrados = new FilteredList<>(datos, c -> true);
        tblClientes.setItems(filtrados);
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> filtrar());

        txtId.setDisable(true);
        chkActivo.setSelected(true);

        // Buscador en tiempo real: cada tecla vuelve a aplicar el predicado
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> filtrar());

        // Editar / Eliminar solo se habilitan cuando hay una fila seleccionada
        btnEditar.disableProperty().bind(
                tblClientes.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.disableProperty().bind(
                tblClientes.getSelectionModel().selectedItemProperty().isNull());
        cargar();
    }

    //La tabla se refresca despues de cada operacion.
    private void cargar() {
        tblClientes.setPlaceholder(new Label("Cargando clientes..."));

        HiloCarga.ejecutar(
                "Hilo-Carga-Clientes",
                dao::listar,
                lista -> {
                    datos.setAll(lista);
                    filtrar();
                    tblClientes.setPlaceholder(new Label("Sin clientes registrados"));
                },
                ex -> {                                        // el error
                    tblClientes.setPlaceholder(new Label("No se pudieron cargar los clientes"));
                    AlertUtils.error("Error al cargar", String.valueOf(ex.getMessage()));
                });
    }

    private void filtrar() {
        String texto = txtBuscar.getText() == null
                ? "" : txtBuscar.getText().trim().toLowerCase();

        filtrados.setPredicate(c -> {
            if (texto.isEmpty()) {
                return true;
            }
            return coincide(c.getNombres(), texto)
                    || coincide(c.getApellidos(), texto)
                    || coincide(c.getTelefono(), texto)
                    || coincide(c.getCorreo(), texto);
        });
    }

    private boolean coincide(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
    }

    @FXML
    private void guardar() {
        // Validaciones antes de guardar el Cliente en la base de datos
        if (Validador.vacio(txtNombres.getText())
                || Validador.vacio(txtApellidos.getText())
                || Validador.vacio(txtTelefono.getText())
                || Validador.vacio(txtCorreo.getText())) {

            AlertUtils.aviso("Datos incompletos", "Todos los campos son obligatorios.");
            return;
        }

        if (!Validador.telefonoValido(txtTelefono.getText())) {
            AlertUtils.aviso("Telefono invalido", "El telefono debe tener 8 digitos.");
            return;
        }

        if (!Validador.correoValido(txtCorreo.getText())) {
            AlertUtils.aviso("Correo invalido", "El correo no tiene un formato valido.");
            return;
        }

        Cliente c = new Cliente();
        c.setNombres(txtNombres.getText().trim());
        c.setApellidos(txtApellidos.getText().trim());
        c.setTelefono(txtTelefono.getText().trim());
        c.setCorreo(txtCorreo.getText().trim());
        c.setActivo(chkActivo.isSelected());

        boolean exito;

        if (idEnEdicion > 0) {
            if (dao.existeCorreo(c.getCorreo(), idEnEdicion)) {
                AlertUtils.aviso("Correo duplicado",
                        "Ya existe otro cliente con ese correo.");
                return;
            }
            c.setId(idEnEdicion);
            exito = dao.actualizar(c);

        } else {
            if (dao.existeCorreo(c.getCorreo(), null)) {
                AlertUtils.aviso("Correo duplicado",
                        "Ya existe un cliente registrado con ese correo.");
                return;
            }
            exito = dao.guardar(c);
        }

        if (!exito) {
            AlertUtils.error("No se pudo guardar", dao.getMensajeError());
            return;
        }

        AlertUtils.info("Operacion exitosa", idEnEdicion > 0
                ? "Cliente actualizado correctamente."
                : "Cliente registrado correctamente.");

        cargar();
        limpiar();
    }

    @FXML
    private void editar() {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            AlertUtils.aviso("Sin seleccion", "Seleccione un cliente de la tabla.");
            return;
        }

        idEnEdicion = seleccionado.getId();
        txtId.setText(String.valueOf(seleccionado.getId()));
        txtNombres.setText(seleccionado.getNombres());
        txtApellidos.setText(seleccionado.getApellidos());
        txtTelefono.setText(seleccionado.getTelefono());
        txtCorreo.setText(seleccionado.getCorreo());
        chkActivo.setSelected(seleccionado.isActivo());
    }

    @FXML
    private void eliminar() {
        Cliente seleccionado = tblClientes.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            AlertUtils.aviso("Sin seleccion", "Seleccione un cliente de la tabla.");
            return;
        }

        // Regla de negocio: no se elimina un cliente con citas asociadas.
        if (dao.tieneCitas(seleccionado.getId())) {
            AlertUtils.aviso("Eliminacion restringida",
                    "El cliente " + seleccionado.getNombreCompleto()
                            + " tiene citas asociadas.\n"
                            + "Elimine primero sus citas y vuelva a intentarlo.");
            return;
        }

        if (!AlertUtils.confirmar("Confirmar eliminacion",
                "Desea eliminar el cliente " + seleccionado.getNombreCompleto() + "?")) {
            return;
        }

        if (dao.eliminar(seleccionado.getId())) {
            AlertUtils.info("Cliente eliminado", "El cliente se elimino correctamente.");
        } else {
            AlertUtils.error("No se pudo eliminar", dao.getMensajeError());
        }

        cargar();
        limpiar();
    }

    @FXML
    private void limpiar() {
        idEnEdicion = -1;
        txtId.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        chkActivo.setSelected(true);
        tblClientes.getSelectionModel().clearSelection();
    }

}
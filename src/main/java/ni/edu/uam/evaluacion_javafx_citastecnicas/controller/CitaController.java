package ni.edu.uam.evaluacion_javafx_citastecnicas.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ni.edu.uam.evaluacion_javafx_citastecnicas.dao.CitaTecnicaDAO;
import ni.edu.uam.evaluacion_javafx_citastecnicas.dao.ClienteDAO;
import ni.edu.uam.evaluacion_javafx_citastecnicas.model.CitaTecnica;
import ni.edu.uam.evaluacion_javafx_citastecnicas.model.Cliente;
import ni.edu.uam.evaluacion_javafx_citastecnicas.util.AlertUtils;
import ni.edu.uam.evaluacion_javafx_citastecnicas.util.Validador;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaController {

    // Opcion del combo de filtro que representa "sin filtro de estado".
    private static final String TODOS_LOS_ESTADOS = "Todos los estados";

    //formulario
    @FXML private TextField txtId;
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private DatePicker dtpFecha;
    @FXML private TextField txtHora;
    @FXML private TextField txtTipoServicio;
    @FXML private TextField txtDescripcion;
    @FXML private ComboBox<String> cmbEstado;

    //botones
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    //filtros
    @FXML private TextField txtBuscador;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private DatePicker dtpFiltroFecha;

    // tabla
    @FXML private TableView<CitaTecnica> tblCitas;
    @FXML private TableColumn<CitaTecnica, Number> colId;
    @FXML private TableColumn<CitaTecnica, String> colCliente;
    @FXML private TableColumn<CitaTecnica, LocalDate> colFecha;
    @FXML private TableColumn<CitaTecnica, LocalTime> colHora;
    @FXML private TableColumn<CitaTecnica, String> colServicio;
    @FXML private TableColumn<CitaTecnica, String> colEstado;

    private final CitaTecnicaDAO dao = new CitaTecnicaDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    // Lista completa que viene de la base de datos
    private final ObservableList<CitaTecnica> datos = FXCollections.observableArrayList();

    // Lista que ve la tabla: "datos" pasando por buscador + filtros
    private FilteredList<CitaTecnica> filtrados;

    private int idEnEdicion = -1;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(f -> new javafx.beans.property.SimpleIntegerProperty(f.getValue().getId()));
        colCliente.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getNombreCliente()));
        colFecha.setCellValueFactory(f -> new javafx.beans.property.SimpleObjectProperty<>(f.getValue().getFecha()));
        colHora.setCellValueFactory(f -> new javafx.beans.property.SimpleObjectProperty<>(f.getValue().getHora()));
        colServicio.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getTipoServicio()));
        colEstado.setCellValueFactory(f -> new javafx.beans.property.SimpleStringProperty(f.getValue().getEstado()));


        filtrados = new FilteredList<>(datos, c -> true);
        tblCitas.setItems(filtrados);


        cmbEstado.setItems(FXCollections.observableArrayList(Validador.ESTADOS));


        cmbFiltroEstado.setItems(FXCollections.observableArrayList(TODOS_LOS_ESTADOS));
        cmbFiltroEstado.getItems().addAll(Validador.ESTADOS);
        cmbFiltroEstado.setValue(TODOS_LOS_ESTADOS);

        dtpFiltroFecha.setPromptText("Todas las fechas");

        txtId.setDisable(true);

        txtBuscador.textProperty().addListener((obs, anterior, nuevo) -> filtrar());
        cmbFiltroEstado.valueProperty().addListener((obs, anterior, nuevo) -> filtrar());
        dtpFiltroFecha.valueProperty().addListener((obs, anterior, nuevo) -> filtrar());


        btnEditar.disableProperty().bind(
                tblCitas.getSelectionModel().selectedItemProperty().isNull());
        btnEliminar.disableProperty().bind(
                tblCitas.getSelectionModel().selectedItemProperty().isNull());

        cargarClientes();
        cargar();
    }

    // ComboBox con los clientes existentes.
    private void cargarClientes() {
        cmbCliente.setItems(FXCollections.observableArrayList(clienteDAO.listar()));
    }

    private void cargar() {
        datos.setAll(dao.listar());
        filtrar();
    }



    private void filtrar() {
        String texto = txtBuscador.getText() == null
                ? "" : txtBuscador.getText().trim().toLowerCase();

        String estadoSeleccionado = cmbFiltroEstado.getValue();
        String estado = (estadoSeleccionado == null
                || TODOS_LOS_ESTADOS.equals(estadoSeleccionado)) ? null : estadoSeleccionado;

        LocalDate fecha = dtpFiltroFecha.getValue();

        filtrados.setPredicate(c -> {
            // Filtro por estado
            if (estado != null && !estado.equalsIgnoreCase(c.getEstado())) {
                return false;
            }
            // Filtro por fecha
            if (fecha != null && !fecha.equals(c.getFecha())) {
                return false;
            }
            // Texto del buscador
            if (texto.isEmpty()) {
                return true;
            }
            // Coincidencia exacta por ID
            if (texto.matches("\\d+") && String.valueOf(c.getId()).equals(texto)) {
                return true;
            }
            return coincide(c.getNombreCliente(), texto)
                    || coincide(c.getTipoServicio(), texto)
                    || coincide(c.getDescripcion(), texto)
                    || coincide(c.getEstado(), texto)
                    || coincide(c.getFecha() == null ? null : c.getFecha().toString(), texto);
        });
    }

    private boolean coincide(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
    }



    @FXML
    private void guardar() {
        Cliente cliente = cmbCliente.getSelectionModel().getSelectedItem();

        if (cliente == null) {
            AlertUtils.aviso("Cliente requerido", "Seleccione el cliente de la cita.");
            return;
        }
        if (dtpFecha.getValue() == null) {
            AlertUtils.aviso("Fecha requerida", "Seleccione la fecha de la cita.");
            return;
        }

        LocalTime hora = Validador.parsearHora(txtHora.getText());
        if (hora == null) {
            AlertUtils.aviso("Hora invalida",
                    "Escriba la hora en formato HH:mm (por ejemplo 09:30).");
            return;
        }

        if (Validador.vacio(txtTipoServicio.getText())
                || Validador.vacio(txtDescripcion.getText())) {
            AlertUtils.aviso("Datos incompletos",
                    "El tipo de servicio y la descripcion son obligatorios.");
            return;
        }

        String estado = cmbEstado.getSelectionModel().getSelectedItem();
        if (!Validador.estadoValido(estado)) {
            AlertUtils.aviso("Estado invalido", "Seleccione un estado permitido.");
            return;
        }

        CitaTecnica c = new CitaTecnica();
        c.setClienteId(cliente.getId());
        c.setFecha(dtpFecha.getValue());
        c.setHora(hora);
        c.setTipoServicio(txtTipoServicio.getText().trim());
        c.setDescripcion(txtDescripcion.getText().trim());
        c.setEstado(estado);

        boolean exito;

        if (idEnEdicion > 0) {
            c.setId(idEnEdicion);
            exito = dao.actualizar(c);
        } else {
            exito = dao.guardar(c);
        }

        if (!exito) {
            AlertUtils.error("No se pudo guardar", dao.getMensajeError());
            return;
        }

        AlertUtils.info("Operacion exitosa", idEnEdicion > 0
                ? "Cita actualizada correctamente."
                : "Cita registrada correctamente.");

        cargar();
        limpiar();
    }

    @FXML
    private void editar() {
        CitaTecnica seleccionada = tblCitas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtils.aviso("Sin seleccion", "Seleccione una cita de la tabla.");
            return;
        }

        idEnEdicion = seleccionada.getId();
        txtId.setText(String.valueOf(seleccionada.getId()));
        txtHora.setText(seleccionada.getHora().toString());
        dtpFecha.setValue(seleccionada.getFecha());
        txtTipoServicio.setText(seleccionada.getTipoServicio());
        txtDescripcion.setText(seleccionada.getDescripcion());
        cmbEstado.getSelectionModel().select(seleccionada.getEstado());
        seleccionarCliente(seleccionada.getClienteId());
    }

    // Deja seleccionado en el ComboBox el cliente de la cita que se edita.
    private void seleccionarCliente(Integer clienteId) {
        for (Cliente c : cmbCliente.getItems()) {
            if (c.getId().equals(clienteId)) {
                cmbCliente.getSelectionModel().select(c);
                return;
            }
        }
    }

    @FXML
    private void eliminar() {
        CitaTecnica seleccionada = tblCitas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            AlertUtils.aviso("Sin seleccion", "Seleccione una cita de la tabla.");
            return;
        }

        if (!AlertUtils.confirmar("Confirmar eliminacion",
                "Desea eliminar la cita de " + seleccionada.getNombreCliente()
                        + " del " + seleccionada.getFecha() + "?")) {
            return;
        }

        if (dao.eliminar(seleccionada.getId())) {
            AlertUtils.info("Cita eliminada", "La cita se elimino correctamente.");
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
        cmbCliente.getSelectionModel().clearSelection();
        dtpFecha.setValue(null);
        txtHora.clear();
        txtTipoServicio.clear();
        txtDescripcion.clear();
        cmbEstado.getSelectionModel().clearSelection();
        tblCitas.getSelectionModel().clearSelection();
    }

}

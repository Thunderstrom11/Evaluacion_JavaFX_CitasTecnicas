package ni.edu.uam.evaluacion_javafx_citastecnicas.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import ni.edu.uam.evaluacion_javafx_citastecnicas.App;
import ni.edu.uam.evaluacion_javafx_citastecnicas.dao.CitaTecnicaDAO;
import ni.edu.uam.evaluacion_javafx_citastecnicas.dao.ClienteDAO;
import ni.edu.uam.evaluacion_javafx_citastecnicas.util.ConexionDB;

//Pantalla de carga. Un hilo de trabajo consulta la base de datos y actualiza la barra de progreso con Platform.runLater()

public class CargaController {

    @FXML private ProgressBar barraProgreso;
    @FXML private Label lblEstado;
    @FXML private Label lblPorcentaje;
    @FXML private Button btnCancelar;
    @FXML private Button btnReintentar;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final CitaTecnicaDAO citaDAO = new CitaTecnicaDAO();

    private Stage stage;
    private Thread hiloCarga;

    //Lo llama App despues de cargar el FXML.
    public void setStage(Stage stage) {
        this.stage = stage;
        iniciarCarga();
    }

    @FXML
    private void iniciarCarga() {
        // Evita arrancar dos veces el mismo proceso.
        if (hiloCarga != null && hiloCarga.isAlive()) {
            return;
        }

        barraProgreso.setProgress(0);
        lblPorcentaje.setText("0 %");
        lblEstado.setText("Iniciando...");
        btnCancelar.setDisable(false);
        btnReintentar.setDisable(true);

        // Tarea que ejecutara el hilo.
        Runnable tarea = () -> {
            try {
                avanzar("Conectando con PostgreSQL...", 15);
                if (!ConexionDB.probarConexion()) {
                    throw new IllegalStateException("La base de datos no respondio.");
                }

                avanzar("Leyendo clientes...", 50);
                int clientes = clienteDAO.contar();

                avanzar("Leyendo citas tecnicas...", 80);
                int citas = citaDAO.contar();

                avanzar("Listo: " + clientes + " clientes y " + citas + " citas.", 100);
                Thread.sleep(400);

                Platform.runLater(this::abrirMenuPrincipal);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                Platform.runLater(() -> {
                    lblEstado.setText("Carga cancelada.");
                    btnCancelar.setDisable(true);
                    btnReintentar.setDisable(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblEstado.setText("No se pudo conectar: " + e.getMessage());
                    btnCancelar.setDisable(true);
                    btnReintentar.setDisable(false);
                });
            }
        };

        hiloCarga = new Thread(tarea);
        hiloCarga.setName("Hilo-Carga-Datos");
        hiloCarga.setDaemon(true);   // no deja la aplicacion colgada al cerrar
        hiloCarga.start();
    }

    // Avanza la barra y espera un momento.
    private void avanzar(String mensaje, int porcentaje) throws InterruptedException {
        Thread.sleep(250);   // simula el tiempo que tarda la consulta

        Platform.runLater(() -> {
            lblEstado.setText(mensaje);
            lblPorcentaje.setText(porcentaje + " %");
            barraProgreso.setProgress(porcentaje / 100.0);
        });
    }

    @FXML
    private void cancelar() {
        if (hiloCarga != null && hiloCarga.isAlive()) {
            btnCancelar.setDisable(true);
            lblEstado.setText("Cancelando...");
            hiloCarga.interrupt();
        }
    }

    @FXML
    private void reintentar() {
        iniciarCarga();
    }

    private void abrirMenuPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource("menu-principal.fxml"));

            stage.setTitle("Gestion de citas tecnicas");
            stage.setScene(new Scene(loader.load(), 640, 460));

        } catch (Exception e) {
            lblEstado.setText("Error al abrir el menu: " + e.getMessage());
        }
    }
}
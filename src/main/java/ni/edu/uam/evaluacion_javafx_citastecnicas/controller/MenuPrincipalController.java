package ni.edu.uam.evaluacion_javafx_citastecnicas.controller;

import javafx.fxml.FXML;
import javafx.application.Platform;
import ni.edu.uam.evaluacion_javafx_citastecnicas.util.SceneManager;

public class MenuPrincipalController {

        @FXML
        private void abrirClientes() {
            SceneManager.abrirVentana("cliente-view.fxml", "Clientes");
        }

        @FXML
        private void abrirCitas() {
            SceneManager.abrirVentana("cita-view.fxml", "Citas tecnicas");
        }

        @FXML
        private void salir() {
            Platform.exit();
        }
    }
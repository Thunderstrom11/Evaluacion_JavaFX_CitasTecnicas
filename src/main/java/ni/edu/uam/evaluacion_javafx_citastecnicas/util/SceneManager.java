package ni.edu.uam.evaluacion_javafx_citastecnicas.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ni.edu.uam.evaluacion_javafx_citastecnicas.App;

import java.io.IOException;
import java.net.URL;

//abre ventanas Modales desde un solo fxml

public class SceneManager {

    private SceneManager() {
    }

    public static void abrirVentana(String recurso, String titulo) {
        try {
            URL url = App.class.getResource(recurso);
            if (url == null) {
                AlertUtils.error("Recurso no encontrado", "No se encontro " + recurso);
                return;
            }

            Stage ventana = new Stage();
            ventana.setTitle(titulo);
            ventana.setScene(new Scene(new FXMLLoader(url).load()));
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.showAndWait();

        } catch (IOException e) {
            AlertUtils.error("Error al abrir la ventana", e.getMessage());
        }
    }
}
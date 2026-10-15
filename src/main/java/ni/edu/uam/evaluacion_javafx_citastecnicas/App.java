package ni.edu.uam.evaluacion_javafx_citastecnicas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.evaluacion_javafx_citastecnicas.controller.CargaController;

// Arranca JavaFX y muestra la pantalla de carga.
//La pantalla de carga es la que despues abre el menu principal.
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                App.class.getResource("carga-view.fxml"));

        Scene scene = new Scene(loader.load(), 560, 360);

        CargaController controller = loader.getController();
        controller.setStage(stage);   // aqui se dispara el hilo de carga

        stage.setTitle("Gestion de citas tecnicas - Cargando");
        stage.setScene(scene);
        stage.show();
    }
}
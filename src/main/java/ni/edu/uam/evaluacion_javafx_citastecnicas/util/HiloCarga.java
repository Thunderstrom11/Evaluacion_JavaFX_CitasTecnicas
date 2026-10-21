package ni.edu.uam.evaluacion_javafx_citastecnicas.util;

import javafx.application.Platform;

import java.util.function.Consumer;
import java.util.function.Supplier;


public final class HiloCarga {

    private HiloCarga() {
    }

    public static <T> Thread ejecutar(String nombre,
                                      Supplier<T> trabajo,
                                      Consumer<T> alTerminar,
                                      Consumer<Exception> alFallar) {

        Thread hilo = new Thread(() -> {
            try {
                Thread.sleep(250);
                T resultado = trabajo.get();                                // hilo secundario
                Platform.runLater(() -> alTerminar.accept(resultado));      // hilo de JavaFX
            } catch (Exception ex) {
                Platform.runLater(() -> alFallar.accept(ex));               // hilo de JavaFX
            }
        }, nombre);

        hilo.setDaemon(true);
        hilo.start();
        return hilo;
    }





}

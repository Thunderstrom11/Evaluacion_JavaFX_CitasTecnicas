module ni.edu.uam.evaluacion_javafx_citastecnicas {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.evaluacion_javafx_citastecnicas to javafx.fxml;
    exports ni.edu.uam.evaluacion_javafx_citastecnicas;
}
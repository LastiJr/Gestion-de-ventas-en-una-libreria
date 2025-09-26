package com.mycompany.gestionlibreria.controlador;

import com.mycompany.gestionlibreria.Sistema;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class ControladorMenu 
{

    @FXML private ToggleButton botonTema; 

    @FXML
    private void initialize() 
    {
        Platform.runLater(() -> 
        {
            Scene scene = botonTema.getScene();
            if (scene == null) 
                return;

            String css = Sistema.getInstance().getCurrentStylesheet();
            scene.getStylesheets().clear();
            if (css != null) 
                scene.getStylesheets().add(css);

            boolean oscuro = css != null && css.contains("/css/oscuro.css");
            botonTema.setSelected(oscuro);
            botonTema.setText(oscuro ? "Modo Claro" : "Modo Oscuro");

            Stage st = (Stage) scene.getWindow();
            st.setResizable(false);
            st.setMaximized(false);
            st.sizeToScene();
            st.centerOnScreen();
        });
    }

    @FXML private void opcionLibros()   { abrirEnMismaVentana("/fxml/libros.fxml",   800, 520, true); }
    @FXML private void opcionClientes() { abrirEnMismaVentana("/fxml/clientes.fxml", 700, 480, true); }
    @FXML private void opcionEstantes() { abrirEnMismaVentana("/fxml/estantes.fxml", 900, 560, true); }
    @FXML private void opcionVenta()    { abrirEnMismaVentana("/fxml/venta.fxml",    900, 600, true); }
    @FXML private void opcionVarios()   { abrirEnMismaVentana("/fxml/varios.fxml",  1000, 700, true); }


    private void abrirEnMismaVentana(String fxml, double w, double h, boolean resizable) 
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene sceneNueva = new Scene(loader.load(), w, h);

            String css = Sistema.getInstance().getCurrentStylesheet();
            sceneNueva.getStylesheets().clear();
            if (css != null) sceneNueva.getStylesheets().add(css);

            Stage stageActual = (Stage) botonTema.getScene().getWindow();
            stageActual.setScene(sceneNueva);
            stageActual.setTitle(tituloDesdeRuta(fxml));

            stageActual.setResizable(resizable);
            if (resizable) 
            {
                stageActual.setMinWidth(w);
                stageActual.setMinHeight(h);
            }
        } catch (Exception ex) 
        {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir: " + fxml).showAndWait();
        }
    }

    private void abrirEnMismaVentana(String fxml, double w, double h) 
    {
        abrirEnMismaVentana(fxml, w, h, false);
    }

    @FXML
    private void cambiarTema() 
    {
        Scene scene = (botonTema != null) ? botonTema.getScene() : null;
        if (scene == null) 
            return;

        String claro = getClass().getResource("/css/claro.css").toExternalForm();
        String oscuro = getClass().getResource("/css/oscuro.css").toExternalForm();

        scene.getStylesheets().clear();
        if (botonTema.isSelected()) 
        {
            scene.getStylesheets().add(oscuro);
            botonTema.setText("Modo Claro");
            Sistema.getInstance().setCurrentStylesheet(oscuro);
        } else 
        {
            scene.getStylesheets().add(claro);
            botonTema.setText("Modo Oscuro");
            Sistema.getInstance().setCurrentStylesheet(claro);  
        }
    }

    private String tituloDesdeRuta(String fxml) 
    {
        if (fxml.contains("libros")) 
            return "Libros";
        if (fxml.contains("clientes")) 
            return "Clientes";
        if (fxml.contains("estantes")) 
            return "Estantes";
        if (fxml.contains("venta")) 
            return "Ventas";
        if (fxml.contains("varios")) 
            return "Varios / Reportes";
        return "Gestión";
    }
}

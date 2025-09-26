package com.mycompany.gestionlibreria.controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControladorLogin 
{

    @FXML private TextField campoUsuario;
    @FXML private PasswordField campoContrasena;
    @FXML private Label etiquetaError;

    private static final String USUARIO_OK = "admin";
    private static final String CONTRASENA_OK = "1234";

    @FXML
    private void iniciarSesion(ActionEvent e) 
    {
        String u = campoUsuario.getText();
        String p = campoContrasena.getText();

        if (USUARIO_OK.equals(u) && CONTRASENA_OK.equals(p)) 
        {
            etiquetaError.setText(""); 
            abrirMenu((Node) e.getSource());
        } else 
        {
            etiquetaError.setText("Usuario o contraseña incorrectos.");
        }
    }

    private void abrirMenu(Node anyNodeFromScene) 
    {
        try 
        {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/menu.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) anyNodeFromScene.getScene().getWindow();
            stage.setTitle("Menú Principal - Librería");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) 
        {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al abrir el menú.").showAndWait();
        }
    }
}

package com.mycompany.gestionlibreria;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        
        Sistema.getInstance().cargarDatosDesdeCSV();

        inicializarServicioCorreo();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Login - Librería");
        stage.setScene(scene);
        stage.setResizable(false);

        stage.setOnCloseRequest(e -> 
        {
            try 
            { 
                Sistema.getInstance().guardarDatosEnCSV(); 
            } catch (Exception ignored){}
        });

        stage.show();
    }

    @Override
    public void stop() throws Exception 
    {
        try 
        { 
            Sistema.getInstance().guardarDatosEnCSV(); 
        } catch (Exception ignored){}
        super.stop();
    }

    private void inicializarServicioCorreo() 
    {
        try 
        {
            File f = new File("data/email.properties");
            if (!f.exists()) 
            {
                System.out.println("[Correo] data/email.properties no existe. Correo desactivado.");
                return;
            }
            Properties p = new Properties();
            try (FileInputStream fis = new FileInputStream(f)) 
            {
                p.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
            }

            String host     = p.getProperty("mail.host", "").trim();
            int port        = Integer.parseInt(p.getProperty("mail.port", "587").trim());
            boolean startls = Boolean.parseBoolean(p.getProperty("mail.starttls", "true"));
            String user     = p.getProperty("mail.user", "").trim();
            String pass     = p.getProperty("mail.pass", "").trim();

            if (host.isEmpty() || user.isEmpty() || pass.isEmpty()) 
            {
                System.out.println("[Correo] Config incompleta. Correo desactivado.");
                return;
            }

            ServicioCorreo sc = new ServicioCorreo(host, port, startls, user, pass);
            Sistema.getInstance().setCorreoService(sc);
            System.out.println("[Correo] ServicioCorreo inicializado: host=" + host + " port=" + port + " startTLS=" + startls);
        } catch (Exception ex) {
            System.err.println("[Correo] No se pudo inicializar ServicioCorreo: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) 
    {
        launch();
    }
}

package com.mycompany.gestionlibreria.controlador;

import com.mycompany.gestionlibreria.Estante;
import com.mycompany.gestionlibreria.Libro;
import com.mycompany.gestionlibreria.Sistema;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;


public class ControladorEstantes implements Initializable 
{

    @FXML private TableView<Estante> tablaEstantes;
    @FXML private TableColumn<Estante, String> colNombre;

    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, String> cIsbn, cTitulo;
    @FXML private TableColumn<Libro, Integer> cStock;

    @FXML private TextField txtNombre;

    private final Sistema sis = Sistema.getInstance();
    private final ObservableList<Estante> dataE = FXCollections.observableArrayList();
    private final ObservableList<Libro> dataL = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        tablaEstantes.setItems(dataE);

        cIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        cTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        cStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        tablaLibros.setItems(dataL);

        listar();

        tablaEstantes.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> {
            if (sel == null) 
            { 
                dataL.clear(); 
                return; 
            }
            dataL.setAll(sel.getLibros());
        });
    }


    @FXML
    private void listar() 
    {
        dataE.setAll(sis.getEstantes());
        tablaEstantes.refresh();
    }

    @FXML
    private void agregar() 
    {
        String n = txtNombre.getText().trim();
        if (n.isEmpty()) 
        { 
            alerta("Ingrese un nombre."); 
            return; 
        }
        if (contieneTilde(n)) 
        { 
            alerta("El nombre no debe contener tildes."); 
            return; 
        }
        if (sis.buscarEstantePorNombre(n) != null) 
        { 
            alerta("Ya existe un estante con ese nombre."); 
            return; 
        }

        sis.getEstantes().add(new Estante(n));
        txtNombre.clear();
        listar();
    }

    @FXML
    private void modificar() 
    {
        Estante e = tablaEstantes.getSelectionModel().getSelectedItem();
        if (e == null) 
        { 
            alerta("Seleccione un estante."); 
            return; 
        }

        String n = txtNombre.getText().trim();
        if (n.isEmpty()) 
        { 
            alerta("Ingrese un nombre."); 
            return; 
        }
        if (contieneTilde(n)) 
        { 
            alerta("El nombre no debe contener tildes."); 
            return; 
        }
        boolean dup = sis.getEstantes().stream().anyMatch(x -> x != e && x.getNombre().equalsIgnoreCase(n));
        if (dup) 
        { 
            alerta("Ya existe otro estante con ese nombre."); 
            return; 
        }

        e.setNombre(n);
        tablaEstantes.refresh();
    }

    @FXML
    private void eliminar() 
    {
        Estante e = tablaEstantes.getSelectionModel().getSelectedItem();
        if (e == null) 
        { 
            alerta("Seleccione un estante."); 
            return; 
        }
        if (confirm("¿Eliminar el estante y sus libros?")) 
        {
            sis.getEstantes().remove(e);
            dataL.clear();
            listar();
        }
    }

    @FXML
    private void volverAlMenu() 
    {
        try 
        {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Scene nueva = new Scene(fx.load());

            String css = com.mycompany.gestionlibreria.Sistema.getInstance().getCurrentStylesheet();
            if (css != null) 
                nueva.getStylesheets().add(css);

            Stage st = (Stage) tablaEstantes.getScene().getWindow(); 
            st.setScene(nueva);
            st.setTitle("Menú");
            st.setMaximized(false);
            st.setResizable(false);
            st.sizeToScene();   
            st.centerOnScreen();
        } catch (Exception ex) 
        { 
            ex.printStackTrace(); 
        }
    }


    @FXML private void agregarEstante()    
    { 
        agregar(); 
    }
    @FXML private void modificarEstante()  
    { 
        modificar(); 
    }
    @FXML private void eliminarEstante()   
    { 
        eliminar(); 
    }
    @FXML private void listarEstantes()    
    { 
        listar(); 
    }
    @FXML private void renombrar()         
    { 
        modificar(); 
    } 

    private boolean contieneTilde(String s) 
    {
        return s.matches(".*[áéíóúÁÉÍÓÚ].*");
    }

    private void alerta(String m)
    { 
        new Alert(Alert.AlertType.WARNING, m).showAndWait(); 
    }

    private boolean confirm(String m) 
    {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, m, ButtonType.OK, ButtonType.CANCEL);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}

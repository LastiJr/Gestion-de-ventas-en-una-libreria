package com.mycompany.gestionlibreria.controlador;

import com.mycompany.gestionlibreria.Cliente;
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
import java.util.regex.Pattern;
import com.mycompany.gestionlibreria.RutInvalido;
import com.mycompany.gestionlibreria.CorreoInvalido;

public class ControladorClientes implements Initializable 
{

    @FXML private TableView<Cliente> tabla;
    @FXML private TableColumn<Cliente,String> cNombre, cRut, cCorreo;
    @FXML private TableColumn<Cliente,Integer> cCompras;
    @FXML private TextField tNombre, tRut, tCorreo;

    private final Sistema sis = Sistema.getInstance();
    private final ObservableList<Cliente> data = FXCollections.observableArrayList();

   
    private static final Pattern EMAIL_RE = Pattern.compile
        (
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        cNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        cRut.setCellValueFactory(new PropertyValueFactory<>("rut"));
        cCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));

        
        cCompras.setCellValueFactory(cell -> 
        {
            Cliente cl = cell.getValue();
            long cnt = sis.getVentas().stream()
                    .filter(v -> v.getCliente()!=null
                            && v.getCliente().getRut()!=null
                            && v.getCliente().getRut().equalsIgnoreCase(cl.getRut()))
                    .count();
            return new javafx.beans.property.SimpleIntegerProperty((int)cnt).asObject();
        });

        tabla.setItems(data);
        listar();

        tabla.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> 
        {
            if (sel == null) return;
            tNombre.setText(sel.getNombre());
            tRut.setText(sel.getRut());
            tCorreo.setText(sel.getCorreo());
        });
    }

    @FXML private void listar() 
    {
        data.setAll(sis.getClientes());
        tabla.refresh();
    }

    @FXML private void agregar() 
    {
        try {
            String nombre = tNombre.getText().trim();
            String rutInp = tRut.getText().trim();
            String correo = tCorreo.getText().trim();

            if (!isValidEmail(correo)) 
            { 
                warn("Correo inválido."); 
                return; 
            }
            if (!validarRut(rutInp))   
            { 
                warn("RUT inválido."); 
                return; 
            }
            String rutFmt = formatearRut(rutInp);

            boolean rutDup = sis.getClientes().stream()
                    .anyMatch(c -> c.getRut()!=null && c.getRut().equalsIgnoreCase(rutFmt));
            if (rutDup) 
            { 
                warn("Ya existe un cliente con ese RUT."); 
                return; 
            }

            boolean mailDup = sis.getClientes().stream()
                    .anyMatch(c -> c.getCorreo()!=null && c.getCorreo().equalsIgnoreCase(correo));
            if (mailDup) 
            { 
                warn("Ya existe un cliente con ese correo."); 
                return; 
            }


            try {
                Cliente c = new Cliente(nombre, rutFmt, correo); 
                sis.agregarClienteAlSistema(c);
            } catch (RutInvalido | CorreoInvalido ex) {
                warn("Error: " + ex.getMessage());
                return;
            }

            tRut.setText(rutFmt);

            listar();
            info("Cliente agregado.");
        } catch (Exception e) 
        {
            error("Datos inválidos.");
        }
    }

    @FXML private void modificar() 
    {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) 
        { 
            warn("Seleccione un cliente."); 
            return; 
        }

        String nombre = tNombre.getText().trim();
        String rutInp = tRut.getText().trim();
        String correo = tCorreo.getText().trim();

        if (!isValidEmail(correo)) 
        { 
            warn("Correo inválido."); 
            return; 
        }
        if (!validarRut(rutInp))   
        { 
            warn("RUT inválido."); 
            return; 
        }
        String rutFmt = formatearRut(rutInp);

        boolean rutDup = sis.getClientes().stream()
                .anyMatch(c -> c != sel && c.getRut()!=null && c.getRut().equalsIgnoreCase(rutFmt));
        if (rutDup) 
        { 
            warn("RUT ya registrado."); 
            return; 
        }

        boolean mailDup = sis.getClientes().stream()
                .anyMatch(c -> c != sel && c.getCorreo()!=null && c.getCorreo().equalsIgnoreCase(correo));
        if (mailDup) 
        { 
            warn("Correo ya registrado."); 
            return; 
        }

        try {
            sel.setNombre(nombre);
            sel.setRut(rutFmt);      
            sel.setCorreo(correo);   
        } catch (RutInvalido | CorreoInvalido ex) {
            warn("Error: " + ex.getMessage());
            return;
        }

        tRut.setText(rutFmt);
        tabla.refresh();
        info("Cliente modificado.");
    }

    @FXML private void eliminar() 
    {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) 
            return;
        if (confirm("¿Eliminar cliente?")) 
        {
            sis.getClientes().remove(sel);
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

            Stage st = (Stage) tabla.getScene().getWindow(); 
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



    
    private void warn(String m)
    { 
        new Alert(Alert.AlertType.WARNING, m).showAndWait(); 
    }
    private void info(String m)
    { 
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); 
    }
    private void error(String m)
    { 
        new Alert(Alert.AlertType.ERROR, m).showAndWait(); 
    }
    private boolean confirm(String m)
    {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, m, ButtonType.OK, ButtonType.CANCEL);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    
    private boolean isValidEmail(String email)
    {
        return email != null && EMAIL_RE.matcher(email.trim()).matches();
    }

    
    private String limpiarRut(String rut) 
    {
        if (rut == null) return "";
        return rut.replace(".", "")
                  .replace("-", "")
                  .replace(" ", "")
                  .toUpperCase();
    }

    private boolean validarRut(String rut) 
    {
        String r = limpiarRut(rut);
        if (r.length() < 2) 
            return false;

        String cuerpo = r.substring(0, r.length() - 1);
        char dvIngresado = r.charAt(r.length() - 1);

        for (int i = 0; i < cuerpo.length(); i++) 
        {
            if (!Character.isDigit(cuerpo.charAt(i))) 
                return false;
        }

        int suma = 0, factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) 
        {
            suma += Character.digit(cuerpo.charAt(i), 10) * factor;
            factor++; if (factor > 7) factor = 2;
        }
        int resto = 11 - (suma % 11);
        char dvCalc;
        if (resto == 11) 
            dvCalc = '0';
        else if (resto == 10) dvCalc = 'K';
        else dvCalc = (char) ('0' + resto);

        
        if (dvIngresado == 'k') 
            dvIngresado = 'K';

        return dvCalc == dvIngresado;
    }

    private String formatearRut(String rut) 
    {
        String r = limpiarRut(rut);
        if (r.length() < 2) 
            return rut;

        String cuerpo = r.substring(0, r.length() - 1);
        String dv = String.valueOf(r.charAt(r.length() - 1));

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = cuerpo.length() - 1; i >= 0; i--) 
        {
            sb.append(cuerpo.charAt(i));
            count++;
            if (count == 3 && i > 0) { sb.append('.'); count = 0; }
        }
        return sb.reverse().toString() + "-" + dv;
    }
}

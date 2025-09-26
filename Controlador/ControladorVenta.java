package com.mycompany.gestionlibreria.controlador;

import com.mycompany.gestionlibreria.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ControladorVenta 
{

    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta,String> vId, vCliente, vFecha;
    @FXML private TableColumn<Venta,Integer> vItems;
    @FXML private TableColumn<Venta,String> vIsbns;
    @FXML private TableColumn<Venta,String> vTotal;
    @FXML private TableView<Libro> tablaItems;
    @FXML private TableColumn<Libro,String> cIsbn, cTitulo;
    @FXML private TableColumn<Libro,Integer> cCant;
    @FXML private TableColumn<Libro,Double> cPrecio;
    @FXML private ComboBox<String> comboCliente;
    @FXML private TextField txtIsbn, txtCantidad;
    @FXML private ToggleButton toggleCorreo;
    @FXML private Button btnVolver;

    private final Sistema sis = Sistema.getInstance();
    private Venta ventaActual;
    private final ObservableList<Venta> ventasData = FXCollections.observableArrayList();
    private final ObservableList<Libro> itemsVenta = FXCollections.observableArrayList();

    @FXML private void initialize() 
    {
        vId.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        vFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        vCliente.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getCliente()!=null ? cell.getValue().getCliente().getRut() : ""
        ));
        
        vItems.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getItems().stream().mapToInt(Libro::getStock).sum()).asObject()
        );
        
        vIsbns.setCellValueFactory(cell -> {
            Venta v = cell.getValue();
            Map<String,Integer> map = new LinkedHashMap<>();
            for (Libro l : v.getItems()) map.merge(l.getIsbn(), l.getStock(), Integer::sum);
            String txt = map.entrySet().stream()
                    .map(e-> e.getKey()+"(x"+e.getValue()+")")
                    .collect(Collectors.joining(", "));
            return new javafx.beans.property.SimpleStringProperty(txt);
        });
        vTotal.setCellValueFactory(cell -> {
            long suma = 0;
            for (Libro l : cell.getValue().getItems()) {
                suma += Math.round(l.getPrecio()) * Math.max(0, l.getStock());
            }
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("es","CL"));
            nf.setMaximumFractionDigits(0);
            return new javafx.beans.property.SimpleStringProperty("$"+nf.format(suma));
        });

        tablaVentas.setItems(ventasData);
        ventasData.setAll(sis.getVentas());

        cIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        cTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        cCant.setCellValueFactory(new PropertyValueFactory<>("stock"));
        cPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        cPrecio.setCellFactory(col -> new TableCell<Libro, Double>() 
        {
            @Override protected void updateItem(Double v, boolean e)
            {
                super.updateItem(v,e);
                if(e||v==null){setText(null);}
                else 
                {
                    java.text.NumberFormat nf=java.text.NumberFormat.getInstance(new java.util.Locale("es","CL"));
                    nf.setMaximumFractionDigits(0);
                    setText("$"+nf.format(v));
                }
            }});
        tablaItems.setItems(itemsVenta);

        comboCliente.setItems(FXCollections.observableArrayList(
                sis.getClientes().stream().map(Cliente::getRut).collect(Collectors.toList())
        ));

        toggleCorreo.selectedProperty().addListener((o,a,sel)->toggleCorreo.setText(sel?"ON":"OFF"));
        toggleCorreo.setSelected(true);

        nuevaVenta();
    }

    @FXML private void nuevaVenta() 
    {
        ventaActual = new Venta(sis.nextVentaId(), LocalDate.now(), null);
        itemsVenta.clear();
        txtIsbn.clear(); txtCantidad.clear();
    }

    @FXML private void agregarItem() 
    {
        try 
        {
            String isbn = txtIsbn.getText().trim();
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            Libro l = sis.getCatalogo().get(isbn);

            if (l == null) 
            { 
                alerta("ISBN no existe."); 
                return; 
            }
            if (cant <= 0) 
            { 
                alerta("Cantidad inválida."); 
                return; 
            }
            if (l.getStock() <= 0 || l.getStock() < cant) 
            { 
                alerta("Stock agotado"); 
                return; 
            }

            Libro existente = itemsVenta.stream().filter(it -> it.getIsbn().equals(isbn)).findFirst().orElse(null);
            if (existente == null) 
            {
                itemsVenta.add(new Libro(l.getTitulo(), l.getAutor(), l.getPrecio(), cant, l.getIsbn(), l.getEditorial()));
            } else 
            {
                existente.setStock(existente.getStock() + cant);
                tablaItems.refresh();
            }

            txtIsbn.clear(); txtCantidad.clear();
        } catch (Exception e) 
        { 
            alerta("Datos inválidos."); 
        }
    }

    @FXML private void confirmarVenta() 
    {
        if (comboCliente.getValue()==null) 
        { 
            alerta("Selecciona cliente."); 
            return; 
        }
        Cliente c = sis.getClientes().stream()
                .filter(cl -> cl.getRut().equals(comboCliente.getValue()))
                .findFirst().orElse(null);
        if (c == null) 
        { 
            alerta("Cliente no encontrado."); 
            return; 
        }

        for (Libro it : itemsVenta) 
        {
            Libro original = sis.getCatalogo().get(it.getIsbn());
            int cantidad = it.getStock();
            if (original.getStock() < cantidad) { alerta("Stock agotado"); 
            return; 
            }
        }

        ventaActual.getItems().clear();
        for (Libro it : itemsVenta) 
        {
            Libro original = sis.getCatalogo().get(it.getIsbn());
            int cantidad = it.getStock();
            original.ajustarStock(-cantidad);
            ventaActual.getItems().add(new Libro(original.getTitulo(), original.getAutor(),
                    original.getPrecio(), cantidad, original.getIsbn(), original.getEditorial()));
        }

        ventaActual.setCliente(c);
        sis.getVentas().add(ventaActual);
        ventasData.setAll(sis.getVentas());

        if (toggleCorreo.isSelected()) 
        {
            try 
            { 
                if (sis.getCorreoService()!=null) sis.getCorreoService().enviarVenta(ventaActual); 
            }
            catch (Exception ex) 
            { 
                System.err.println("[Correo] " + ex.getMessage()); 
            }
        }

        info("Venta registrada: " + ventaActual.getIdVenta());
        nuevaVenta();
    }

    private void alerta(String m)
    { 
        new Alert(Alert.AlertType.ERROR, m).showAndWait(); 
    }
    private void info(String m)
    { 
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); 
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

            Stage st = (Stage) btnVolver.getScene().getWindow(); 
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
}


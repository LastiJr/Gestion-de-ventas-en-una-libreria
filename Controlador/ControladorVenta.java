package com.mycompany.gestionlibreria.controlador;

import com.mycompany.gestionlibreria.Cliente;
import com.mycompany.gestionlibreria.Libro;
import com.mycompany.gestionlibreria.Sistema;
import com.mycompany.gestionlibreria.Venta;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;



public class ControladorVenta 
{
    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, String> vId;
    @FXML private TableColumn<Venta, String> vCliente;
    @FXML private TableColumn<Venta, String> vFecha;
    @FXML private TableColumn<Venta, Integer> vItems;
    @FXML private TableColumn<Venta, String> vIsbns;
    @FXML private TableColumn<Venta, String> vTotal;
    @FXML private TableView<Libro> tablaItems;
    @FXML private TableColumn<Libro, String> cIsbn;
    @FXML private TableColumn<Libro, String> cTitulo;
    @FXML private TableColumn<Libro, Integer> cCant;  
    @FXML private TableColumn<Libro, Double> cPrecio;
    @FXML private ComboBox<String> comboCliente;
    @FXML private TextField txtIsbn;
    @FXML private TextField txtCantidad;
    @FXML private ToggleButton toggleCorreo;
    @FXML private Button btnVolver;
    @FXML private Label lblItemsActual;
    @FXML private Label lblTotalActual;

    private final Sistema sis = Sistema.getInstance();
    private Venta ventaActual;
    private final ObservableList<Venta> ventasData = FXCollections.observableArrayList();
    private final ObservableList<Libro> itemsVenta = FXCollections.observableArrayList();
    private final NumberFormat nfCL = NumberFormat.getInstance(new Locale("es", "CL"));


    @FXML
    private void initialize() 
    {
        if (lblTotalActual != null) lblTotalActual.setText("$0");

        nfCL.setMaximumFractionDigits(0);

        vId.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getIdVenta())));

        vFecha.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getFecha())));

        vCliente.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCliente() != null ? cell.getValue().getCliente().getRut() : "")
        );

        vItems.setCellValueFactory(cell ->
                new SimpleIntegerProperty(cell.getValue().getCantidadTotalItems()).asObject()
        );

        vIsbns.setCellValueFactory(cell -> {
            Venta v = cell.getValue();
            Map<String, Integer> map = new LinkedHashMap<>();
            for (Libro l : v.getItems()) map.merge(l.getIsbn(), Math.max(0, l.getStock()), Integer::sum);
            String txt = map.entrySet().stream()
                    .map(e -> e.getKey() + "(x" + e.getValue() + ")")
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(txt);
        });

        vTotal.setCellValueFactory(cell -> 
        {
            long suma = 0;
            for (Libro l : cell.getValue().getItems()) 
            {
                int cantidad = Math.max(0, l.getStock());
                suma += Math.round(l.getPrecio()) * (long) cantidad;
            }
            return new SimpleStringProperty("$" + nfCL.format(suma));
        });

        tablaVentas.setItems(ventasData);
        ventasData.setAll(sis.getVentas());

        cIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        cTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        cCant.setCellValueFactory(new PropertyValueFactory<>("stock"));   // 'stock' como cantidad
        cPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        cPrecio.setCellFactory(col -> new TableCell<Libro, Double>() {
            @Override protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) setText(null);
                else setText("$" + nfCL.format(Math.round(v)));
            }
        });
        tablaItems.setItems(itemsVenta);

        comboCliente.setItems(FXCollections.observableArrayList(
                sis.getClientes().stream().map(Cliente::getRut).collect(Collectors.toList())
        ));

        toggleCorreo.selectedProperty().addListener((o, a, sel) -> toggleCorreo.setText(sel ? "ON" : "OFF"));
        toggleCorreo.setSelected(true);

        if (lblTotalActual != null) 
            lblTotalActual.setText("$0");
        if (lblItemsActual != null) 
            lblItemsActual.setText("0");

        itemsVenta.addListener((ListChangeListener<Libro>) c -> actualizarIndicadores());

        nuevaVenta();
        actualizarIndicadores();
    }


    @FXML
    private void nuevaVenta() 
    {
        ventaActual = new Venta(sis.nextVentaId(), LocalDate.now(), null);
        itemsVenta.clear();
        if (txtIsbn != null) 
            txtIsbn.clear();
        if (txtCantidad != null) 
            txtCantidad.clear();
        actualizarIndicadores();
    }

    @FXML
    private void agregarItem() 
    {
        try 
        {
            String isbn = txtIsbn.getText() != null ? txtIsbn.getText().trim() : "";
            String cantStr = txtCantidad.getText() != null ? txtCantidad.getText().trim() : "";

            if (isbn.isEmpty()) 
            { 
                alerta("Ingrese un ISBN."); 
                return; 
            }

            int cant;
            try 
            { 
                cant = Integer.parseInt(cantStr); 
            }
            catch (NumberFormatException nfe) 
            { 
                alerta("Cantidad inválida. Debe ser un entero > 0."); 
                return; 
            }
            if (cant <= 0) 
            {
                alerta("Cantidad inválida. Debe ser > 0."); 
                return; 
            }

            Libro l = sis.getCatalogo().get(isbn);
            if (l == null) 
            { 
                alerta("El ISBN no existe en el catálogo."); 
                return; 
            }
            if (l.getStock() < cant) 
            { 
                alerta("Stock insuficiente para el ISBN " + isbn + ".");
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

            txtIsbn.clear();
            txtCantidad.clear();
            actualizarIndicadores();
        } catch (Exception e) 
        {
            alerta("Datos inválidos.");
        }
    }

    @FXML
    private void confirmarVenta() {
        if (comboCliente.getValue() == null) 
        { 
            alerta("Selecciona un cliente."); 
            return; 
        }

        Cliente c = sis.getClientes().stream()
                .filter(cl -> cl.getRut().equals(comboCliente.getValue()))
                .findFirst()
                .orElse(null);

        if (c == null) 
        { 
            alerta("Cliente no encontrado."); 
            return; 
        }
        if (itemsVenta.isEmpty()) 
        { 
            alerta("No hay ítems en el carrito."); 
            return; 
        }

        ventaActual.getItems().clear();
        for (Libro it : itemsVenta) 
        {
            Libro original = sis.getCatalogo().get(it.getIsbn());
            int cantidad = Math.max(0, it.getStock());
            ventaActual.getItems().add(new Libro(
                    original.getTitulo(),
                    original.getAutor(),
                    original.getPrecio(),
                    cantidad,
                    original.getIsbn(),
                    original.getEditorial()
            ));
        }
        ventaActual.setCliente(c);

        boolean enviarCorreo = toggleCorreo.isSelected();
        sis.confirmarVenta(ventaActual, enviarCorreo);

        ventasData.setAll(sis.getVentas());
        info("Venta registrada: " + ventaActual.getIdVenta() +
                (ventaActual.isCorreoEnviado() ? " (correo enviado)" : " (correo NO enviado)"));

        nuevaVenta(); 
    }

    @FXML
    private void volverAlMenu(javafx.event.ActionEvent evt) 
    {
        try 
        {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Scene nueva = new Scene(fx.load());
            String css = sis.getCurrentStylesheet();
            if (css != null) 
                nueva.getStylesheets().add(css);

            Stage st = (Stage) ((Node) evt.getSource()).getScene().getWindow();
            st.setScene(nueva);
            st.setTitle("Menú");
            st.setMaximized(false);
            st.setResizable(false);
            st.sizeToScene();
            st.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void alerta(String m) 
    { 
        new Alert(Alert.AlertType.ERROR, m).showAndWait(); 
    }

    private void info(String m)   
    { 
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); 
    }

    
    private void actualizarIndicadores() {
        int totalItems = itemsVenta.stream().mapToInt(Libro::getStock).sum();
        long totalCLP = 0L;
        for (Libro it : itemsVenta) 
        {
            long pu = Math.round(it.getPrecio());
            totalCLP += pu * Math.max(0, it.getStock());
        }
        if (lblItemsActual != null) 
            lblItemsActual.setText(String.valueOf(totalItems));
        if (lblTotalActual != null) 
            lblTotalActual.setText("$" + nfCL.format(totalCLP));
    }
}

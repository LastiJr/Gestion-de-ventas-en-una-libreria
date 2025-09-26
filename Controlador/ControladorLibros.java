package com.mycompany.gestionlibreria.controlador;

import com.mycompany.gestionlibreria.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;

public class ControladorLibros 
{
    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, String> colIsbn, colTitulo, colAutor, colEditorial, colEstante;
    @FXML private TableColumn<Libro, Number> colPrecio, colStock;
    @FXML private TextField txtBuscarTitulo;
    @FXML private TextField txtIsbn, txtTitulo, txtAutor, txtEditorial, txtPrecio, txtStock;
    @FXML private ComboBox<String> comboEstante;

    private final Sistema sis = Sistema.getInstance();
    private final ObservableList<Libro> master = FXCollections.observableArrayList();
    private FilteredList<Libro> filtered;
    private SortedList<Libro> sorted;

    @FXML
    private void initialize() 
    {
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colEditorial.setCellValueFactory(new PropertyValueFactory<>("editorial"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colEstante.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(encontrarEstanteDe(cell.getValue()))
        );

        colPrecio.setCellFactory(col -> new TableCell<Libro, Number>() 
        {
            private final NumberFormat nf = NumberFormat.getInstance(new Locale("es","CL"));
            { 
                nf.setMaximumFractionDigits(0); 
            }
            @Override protected void updateItem(Number v, boolean empty) 
            {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : "$" + nf.format(Math.round(v.doubleValue())));
            }
        });

        comboEstante.setItems(FXCollections.observableArrayList(
                sis.getEstantes().stream().map(Estante::getNombre).collect(Collectors.toList())
        ));

        master.setAll(sis.getCatalogo().values());

        filtered = new FilteredList<>(master, l -> true);
        txtBuscarTitulo.textProperty().addListener((obs, old, text) -> {
            String q = normalizar(text);
            filtered.setPredicate(lib -> q.isEmpty()
                    || (lib.getTitulo() != null && normalizar(lib.getTitulo()).startsWith(q)));
        });

        sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tablaLibros.comparatorProperty());
        tablaLibros.setItems(sorted);

        tablaLibros.getSelectionModel().selectedItemProperty().addListener((o, a, sel) -> {
            if (sel == null) 
                return;
            txtIsbn.setText(sel.getIsbn());
            txtTitulo.setText(sel.getTitulo());
            txtAutor.setText(sel.getAutor());
            txtEditorial.setText(sel.getEditorial());
            txtPrecio.setText(String.valueOf(Math.round(sel.getPrecio())));
            txtStock.setText(String.valueOf(sel.getStock()));
            comboEstante.getSelectionModel().select(encontrarEstanteDe(sel));

            txtIsbn.setDisable(true);
            comboEstante.setDisable(true);
            txtIsbn.setTooltip(new Tooltip("No editable al modificar"));
            comboEstante.setTooltip(new Tooltip("No editable al modificar"));
        });

        txtIsbn.setDisable(false);
        comboEstante.setDisable(false);
    }

    @FXML
    private void agregar() 
    {
        try 
        {
            String isbn = safe(txtIsbn.getText());
            String titulo = safe(txtTitulo.getText());
            String autor = safe(txtAutor.getText());
            String editorial = safe(txtEditorial.getText());
            String estanteNombre = safe(comboEstante.getSelectionModel().getSelectedItem());

            if (isbn.isEmpty() || titulo.isEmpty() || autor.isEmpty() || editorial.isEmpty() || estanteNombre.isEmpty()) 
            {
                alerta("Completa todos los campos (incluyendo Estante).");
                return;
            }
            if (sis.getCatalogo().containsKey(isbn)) 
            { 
                alerta("Ya existe un libro con este ISBN."); 
                return; 
            }

            int stock = parseIntSafe(txtStock.getText(), -1);
            if (stock < 0) 
            { 
                alerta("Stock inválido."); 
                return; 
            }

            double precio = parseDoubleSafe(txtPrecio.getText(), -1);
            if (precio < 0) 
            { 
                alerta("Precio inválido."); 
                return; 
            }

            Estante est = sis.buscarEstantePorNombre(estanteNombre);
            if (est == null) 
            { 
                alerta("Estante no encontrado."); 
                return; 
            }

            boolean duplicado = est.getLibros().stream().anyMatch(l ->
                    eq(l.getTitulo(), titulo) &&
                    eq(l.getAutor(), autor) &&
                    Math.round(l.getPrecio()) == Math.round(precio) &&
                    l.getStock() == stock
            );
            if (duplicado) 
            { 
                alerta("Este libro ya existe con los mismos datos en ese Estante."); 
                return; 
            }

            Libro nuevo = new Libro(titulo, autor, precio, stock, isbn, editorial);
            est.getLibros().add(nuevo);
            sis.getCatalogo().put(isbn, nuevo);

            refrescarTablaDesdeSistema();
            info("Libro agregado.");
            limpiarCampos();
        } catch (Exception ex) 
        {
            ex.printStackTrace();
            alerta("No se pudo agregar el libro.");
        }
    }

    @FXML
    private void modificar() 
    {
        Libro sel = tablaLibros.getSelectionModel().getSelectedItem();
        if (sel == null) 
        { 
            alerta("Selecciona un libro en la tabla."); 
            return; 
        }

        try 
        {
            String nuevoTitulo = safe(txtTitulo.getText());
            String nuevoAutor = safe(txtAutor.getText());
            String nuevaEditorial = safe(txtEditorial.getText());

            if (nuevoTitulo.isEmpty() || nuevoAutor.isEmpty() || nuevaEditorial.isEmpty()
                    || safe(txtPrecio.getText()).isEmpty() || safe(txtStock.getText()).isEmpty()) 
            {
                alerta("Completa Título, Autor, Editorial, Precio y Stock.");
                return;
            }

            int nuevoStock = parseIntSafe(txtStock.getText(), -1);
            if (nuevoStock < 0) 
            { 
                alerta("Stock inválido."); 
                return; 
            }

            double nuevoPrecio = parseDoubleSafe(txtPrecio.getText(), -1);
            if (nuevoPrecio < 0) 
            { 
                alerta("Precio inválido."); 
                return; 
            }

            sel.setTitulo(nuevoTitulo);
            sel.setAutor(nuevoAutor);
            sel.setEditorial(nuevaEditorial);
            sel.setPrecio(nuevoPrecio);
            sel.setStock(nuevoStock);

            tablaLibros.refresh();
            info("Libro modificado (ISBN y Estante no cambian).");
            limpiarCampos();
        } catch (Exception ex) 
        {
            ex.printStackTrace();
            alerta("No se pudo modificar el libro.");
        }
    }

    @FXML
    private void eliminar() 
    {
        Libro sel = tablaLibros.getSelectionModel().getSelectedItem();
        if (sel == null) 
        { 
            alerta("Selecciona un libro en la tabla."); 
            return; 
        }
        try 
        {
            Estante est = buscarEstanteDe(sel);
            if (est != null) est.getLibros().removeIf(l -> eq(l.getIsbn(), sel.getIsbn()));
            sis.getCatalogo().remove(sel.getIsbn());

            refrescarTablaDesdeSistema();
            info("Libro eliminado.");
            limpiarCampos();
        } catch (Exception ex) 
        {
            ex.printStackTrace();
            alerta("No se pudo eliminar el libro.");
        }
    }

    @FXML
    private void listar() 
    { 
        refrescarTablaDesdeSistema(); 
        info("Listado actualizado."); 
    }


    @FXML
    private void volverAlMenu(javafx.event.ActionEvent evt) 
    {
        try 
        {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Scene nueva = new Scene(fx.load());

            String css = com.mycompany.gestionlibreria.Sistema.getInstance().getCurrentStylesheet();
            if (css != null) 
                nueva.getStylesheets().add(css);

            Stage st = (Stage) ((javafx.scene.Node) evt.getSource()).getScene().getWindow();
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


    private void refrescarTablaDesdeSistema() 
    { 
        master.setAll(sis.getCatalogo().values()); 
    }

    private void limpiarCampos() 
    {
        txtIsbn.clear(); txtTitulo.clear(); txtAutor.clear(); txtEditorial.clear();
        txtPrecio.clear(); txtStock.clear(); comboEstante.getSelectionModel().clearSelection();
        txtIsbn.setDisable(false); comboEstante.setDisable(false);
        txtIsbn.setTooltip(null);  comboEstante.setTooltip(null);
        tablaLibros.getSelectionModel().clearSelection();
    }

    private String encontrarEstanteDe(Libro libro) 
    {
        Estante e = buscarEstanteDe(libro);
        return e == null ? "" : e.getNombre();
    }
    private Estante buscarEstanteDe(Libro libro) 
    {
        if (libro == null) 
            return null;
        for (Estante e : sis.getEstantes()) 
        {
            for (Libro l : e.getLibros()) 
            {
                if (l.getIsbn() != null && l.getIsbn().equals(libro.getIsbn())) 
                    return e;
            }
        }
        return null;
    }

    private static String normalizar(String s) 
    { 
        return s == null ? "" : s.trim().toLowerCase(); 
    }
    private static boolean eq(String a, String b) 
    { 
        return a == null ? b == null : a.equals(b); 
    }
    private static String safe(String s)
    { 
        return s == null ? "" : s.trim(); 
    }
    private static int parseIntSafe(String s, int def)
    { 
        try 
        { 
            return Integer.parseInt(s.trim()); 
        } catch (Exception e) 
        { return def; 
        } 
    }
    private static double parseDoubleSafe(String s, double def)
    { 
        try 
        { 
            return Double.parseDouble(s.trim()); 
        } catch (Exception e) 
        { 
            return def; 
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
}

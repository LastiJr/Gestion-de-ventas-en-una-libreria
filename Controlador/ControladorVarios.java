package com.mycompany.gestionlibreria.controlador;

import com.mycompany.gestionlibreria.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ControladorVarios 
{

    @FXML private ComboBox<String> comboRango;
    @FXML private ComboBox<String> comboCategoria;
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private Label lblResumen;

    private final Sistema sis = Sistema.getInstance();
    private final NumberFormat nfCL = NumberFormat.getInstance(new java.util.Locale("es","CL"));

    private static final String R1D = "Último 1 día";
    private static final String R7D = "Últimos 7 días";
    private static final String R1M = "Último 1 mes";
    private static final String R1Y = "Último 1 año";
    private static final String RALL = "Desde el inicio";

    private static final String CAT_CLIENTES = "Clientes (total de compras)";
    private static final String CAT_ESTANTES = "Estanterias (total de ventas)";

    @FXML
    private void initialize() 
    {
        nfCL.setMaximumFractionDigits(0);

        comboRango.setItems(FXCollections.observableArrayList(R1D, R7D, R1M, R1Y, RALL));
        comboRango.getSelectionModel().select(R7D);

        comboCategoria.setItems(FXCollections.observableArrayList(CAT_CLIENTES, CAT_ESTANTES));
        comboCategoria.getSelectionModel().select(CAT_CLIENTES);

        comboRango.valueProperty().addListener((o,a,b)->refrescarGrafico());
        comboCategoria.valueProperty().addListener((o,a,b)->refrescarGrafico());

        yAxis.setForceZeroInRange(true);
        yAxis.setTickLabelFormatter(new StringConverterNumber(nfCL));

        barChart.setLegendVisible(false);
        barChart.setCategoryGap(15.0);
        barChart.setBarGap(3.0);
        xAxis.setTickLabelRotation(0);

        refrescarGrafico();
    }

    @FXML
    private void exportar() 
    {
        LocalDate desde = resolverFechaInicio(comboRango.getValue());
        java.util.List<Venta> ventas = filtrarVentasPorFecha(desde);

        Path exportDir = Paths.get("data", "export");
        try 
        { 
            if (!Files.exists(exportDir)) Files.createDirectories(exportDir); 
        } catch (IOException ignored) {}

        String sufijo = sufijoArchivo(comboRango.getValue(), desde);
        Path fileVentas = exportDir.resolve("ventas_" + sufijo + ".csv");
        Path fileItems  = exportDir.resolve("venta_items_" + sufijo + ".csv");

        try 
        {
            try (BufferedWriter bw = newWriterCSV(fileVentas)) 
            {
                writeRow(bw, "idVenta","fecha","rutCliente","total","correoEnviado");
                for (Venta v : ventas) 
                {
                    long total = calcularTotalVenta(v);
                    String rut = v.getCliente()!=null ? v.getCliente().getRut() : "";
                    writeRow(bw, v.getIdVenta(), String.valueOf(v.getFecha()), rut,
                            String.valueOf(total), v.isCorreoEnviado() ? "SI" : "NO");
                }
            }
            try (BufferedWriter bw = newWriterCSV(fileItems)) 
            {
                writeRow(bw, "idVenta","isbn","cantidad","precioUnit","titulo","estante");
                for (Venta v : ventas) 
                {
                    Map<String, AgrupItem> map = agruparItems(v);
                    for (AgrupItem ai : map.values()) 
                    {
                        writeRow(bw, v.getIdVenta(), ai.isbn, String.valueOf(ai.cant),
                                String.valueOf(Math.round(ai.precioUnit)), ai.titulo, ai.estante);
                    }
                }
            }
            info("Exportado en data/export/");
        } catch (Exception ex) 
        {
            ex.printStackTrace();
            alerta("No se pudo exportar.");
        }
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



    private void refrescarGrafico() 
    {
        LocalDate desde = resolverFechaInicio(comboRango.getValue());
        java.util.List<Venta> ventas = filtrarVentasPorFecha(desde);

        String cat = comboCategoria.getValue();
        barChart.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        if (CAT_CLIENTES.equals(cat)) 
        {
            LinkedHashMap<String, Long> comprasPorCliente = ventas.stream()
                    .collect(Collectors.groupingBy(
                            v -> v.getCliente()!=null ? v.getCliente().getRut() : "(sin rut)",
                            LinkedHashMap::new, Collectors.counting()));

            xAxis.setAutoRanging(false);
            xAxis.setCategories(FXCollections.observableArrayList(comprasPorCliente.keySet()));

            comprasPorCliente.forEach((rut, cnt) -> serie.getData().add(new XYChart.Data<>(rut, cnt)));
            xAxis.setLabel("Cliente (RUT)");
            yAxis.setLabel("Compras");
            lblResumen.setText("Clientes: " + comprasPorCliente.size() + "  |  Ventas en rango: " + ventas.size());
        } else 
        {
            LinkedHashMap<String, Long> totalPorEstante = new LinkedHashMap<>();
            for (Venta v : ventas) 
            {
                Map<String, AgrupItem> map = agruparItems(v);
                for (AgrupItem ai : map.values()) 
                {
                    long linea = Math.round(ai.precioUnit) * ai.cant;
                    totalPorEstante.merge(ai.estante, linea, Long::sum);
                }
            }

            xAxis.setAutoRanging(false);
            xAxis.setCategories(FXCollections.observableArrayList(totalPorEstante.keySet()));

            totalPorEstante.forEach((est, tot) -> serie.getData().add(new XYChart.Data<>(est, tot)));
            xAxis.setLabel("Estantería");
            yAxis.setLabel("Total ventas ($)");
            long suma = totalPorEstante.values().stream().mapToLong(Long::longValue).sum();
            lblResumen.setText("Estanterías: " + totalPorEstante.size() + "  |  Total en rango: $" + nfCL.format(suma));
        }

        barChart.getData().add(serie);
    }



    private LocalDate resolverFechaInicio(String opcion) 
    {
        LocalDate hoy = LocalDate.now();
        if (R1D.equals(opcion)) 
            return hoy.minusDays(1);
        if (R7D.equals(opcion)) 
            return hoy.minusDays(7);
        if (R1M.equals(opcion)) 
            return hoy.minusMonths(1);
        if (R1Y.equals(opcion)) 
            return hoy.minusYears(1);
        return LocalDate.MIN;
    }

    private java.util.List<Venta> filtrarVentasPorFecha(LocalDate desde) 
    {
        return sis.getVentas().stream()
                .filter(v -> v.getFecha()!=null && !v.getFecha().isBefore(desde))
                .sorted(java.util.Comparator.comparing(Venta::getFecha))
                .collect(Collectors.toList());
    }

    private static long calcularTotalVenta(Venta v)
    {
        long total = 0L;
        for (Libro item : v.getItems()) 
        {
            int cant = Math.max(0, item.getStock());
            long precio = Math.round(item.getPrecio());
            total += precio * cant;
        }
        return total;
    }

    private Map<String, AgrupItem> agruparItems(Venta v) 
    {
        Map<String, AgrupItem> map = new LinkedHashMap<>();
        for (Libro it : v.getItems()) 
        {
            String isbn = it.getIsbn();
            if (isbn == null) 
                continue;
            AgrupItem ai = map.get(isbn);
            if (ai == null) 
            {
                ai = new AgrupItem();
                ai.isbn = isbn;
                ai.titulo = it.getTitulo();
                ai.precioUnit = it.getPrecio();
                ai.cant = 0;
                Estante est = buscarEstantePorIsbn(isbn);
                ai.estante = est != null ? est.getNombre() : "(sin estante)";
                map.put(isbn, ai);
            }
            ai.cant += Math.max(0, it.getStock());
        }
        return map;
    }

    private Estante buscarEstantePorIsbn(String isbn) 
    {
        for (Estante e : sis.getEstantes()) 
        {
            for (Libro l : e.getLibros()) 
            {
                if (isbn.equals(l.getIsbn())) 
                    return e;
            }
        }
        return null;
    }

    private static class AgrupItem 
    {
        String isbn; String titulo; double precioUnit; int cant; String estante;
    }

    private static BufferedWriter newWriterCSV(Path path) throws IOException 
    {
        if (!Files.exists(path.getParent())) Files.createDirectories(path.getParent());
        
        OutputStream os = Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        os.write(0xEF); os.write(0xBB); os.write(0xBF);
        return new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }
    private static void writeRow(Writer w, String... cols) throws IOException 
    {
        for (int i=0;i<cols.length;i++)
        { 
            if (i>0) 
                w.write(';'); w.write(esc(cols[i])); }
        w.write("\r\n");
    }
    private static String esc(String s)
    {
        if (s == null) 
            return "";
        boolean needs = s.indexOf(';') >= 0 || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"","\"\"");
        return needs ? ("\"" + v + "\"") : v;
    }
    
    private String sufijoArchivo(String opcion, LocalDate desde)
    {
        if (RALL.equals(opcion)) 
            return "todo";
        return opcion.replace(" ", "").replace("í","i").replace("á","a").replace("é","e").replace("ó","o").replace("ú","u")
                .replace("Último","ultimo").replace("Últimos","ultimos")
                .replace("(", "").replace(")", "").replace("/", "_");
    }

    private void alerta(String m)
    { 
        new Alert(Alert.AlertType.ERROR, m).showAndWait(); 
    }
    private void info(String m)
    { 
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); 
    }

    private static class StringConverterNumber extends javafx.util.StringConverter<Number> 
    {
        private final NumberFormat nf;
        StringConverterNumber(NumberFormat nf){ this.nf = nf; }
        @Override public String toString(Number object)
        { 
            return object==null? "" : nf.format(object.longValue()); 
        }
        @Override public Number fromString(String string)
        { 
            try 
        { 
            return nf.parse(string).longValue(); 
        } catch(Exception e)
        { return 0; 
        } 
        }
    }
}

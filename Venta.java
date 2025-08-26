package libreria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Venta {

    private String idVenta;
    private LocalDate fecha;
    private Cliente cliente;
    private List<Libro> items = new ArrayList<>();
    
    
    public Venta(String idVenta, LocalDate fecha, Cliente cliente) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.cliente = cliente;
    } 
}

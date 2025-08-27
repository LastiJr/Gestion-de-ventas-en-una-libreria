package libreria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Venta 
{

    private String idVenta;
    private LocalDate fecha;
    private Cliente cliente;
    private List<Libro> items = new ArrayList<>();

    public Venta(String idVenta, LocalDate fecha, Cliente cliente) 
    {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.cliente = cliente;
    }

    public String getIdVenta() 
    { 
        return this.idVenta; 
    }
    public void setIdVenta(String idVenta)
    { 
        this.idVenta = idVenta; 
    }

    public LocalDate getFecha()
    { 
        return this.fecha;
    }
    public void setFecha(LocalDate fecha)
    { 
        this.fecha = fecha; 
    }

    public Cliente getCliente()
    { 
        return this.cliente;
    }
    public void setCliente(Cliente cliente)
    { 
        this.cliente = cliente; 
    }

    public List<Libro> getItems()
    { 
        return this.items; 
    }
    public void setItems(List<Libro> items)
    { 
        this.items = (items == null) ? new ArrayList<>() : items; 
    }

    //SOBRECARGA 
    
    public void agregarItem(Libro libro) 
    {
        agregarItem(libro, 1);
    }
    public void agregarItem(Libro libro, int cantidad) 
    {
        if (libro == null || cantidad <= 0) return;

        if (libro.getStock() < cantidad) 
        {
            System.out.println("Stock insuficiente para \"" + libro.getTitulo() + "\". Disponible: " + libro.getStock());
            return;
        }
        
        libro.ajustarStock(-cantidad);

        for (int i = 0; i < cantidad; i++)
        {
            items.add(libro);
        }
    }
    
    public void agregarItem(String titulo, String autor, double precio, int stock, String isbn) 
    {
        items.add(new Libro(isbn, titulo, autor, precio, stock));
    }
    
    public double total() 
    {
        double suma = 0;
        for (Libro l : items) suma += l.getPrecio();
        return suma;
    }

    @Override
    public String toString() 
    {
        return "Venta " + idVenta + " - " + fecha +
               " - Cliente: " + (cliente != null ? cliente.getNombre() : "N/A") +
               " - Ítems: " + items.size() +
               " - Total: $" + total();
    }
}




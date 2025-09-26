package com.mycompany.gestionlibreria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Venta 
{
    private String idVenta;
    private LocalDate fecha;
    private Cliente cliente;
    private final List<Libro> items = new ArrayList<>();

    
    private boolean correoEnviado = false;

    public Venta(String idVenta, LocalDate fecha, Cliente cliente) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.cliente = cliente;
    }

    
    public String getIdVenta() 
    { 
        return idVenta; 
    }
    public void setIdVenta(String idVenta) 
    { 
        this.idVenta = idVenta; 
    }

    public LocalDate getFecha() 
    { 
        return fecha; 
    }
    public void setFecha(LocalDate fecha) 
    { 
        this.fecha = fecha; 
    }

    public Cliente getCliente() 
    { 
        return cliente; 
    }
    public void setCliente(Cliente cliente) 
    { 
        this.cliente = cliente; 
    }

    public List<Libro> getItems() 
    { 
        return items; 
    }

    public boolean isCorreoEnviado() 
    { 
        return correoEnviado; 
    }
    public void setCorreoEnviado(boolean correoEnviado) 
    { 
        this.correoEnviado = correoEnviado; 
    }

    public void agregarItem(String titulo, String autor, double precio, int cantidad, String isbn, String editorial) 
    {
        items.add(new Libro(titulo, autor, precio, Math.max(0, cantidad), isbn, editorial));
    }

    
    public int getCantidadTotalItems() 
    {
        int total = 0;
        for (Libro it : items) total += Math.max(0, it.getStock());
        return total;
    }

    
    public double total() 
    {
        double suma = 0.0;
        for (Libro it : items) 
        {
            int cant = Math.max(0, it.getStock());
            suma += it.getPrecio() * cant;
        }
        return suma;
    }

    @Override
    public String toString() 
    {
        return "Venta{" +
                "idVenta='" + idVenta + '\'' +
                ", fecha=" + fecha +
                ", cliente=" + (cliente!=null?cliente.getRut():"") +
                ", items=" + items.size() +
                ", total=" + total() +
                ", correoEnviado=" + correoEnviado +
                '}';
    }
}

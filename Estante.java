package libreria;

import java.util.ArrayList;
import java.util.List;

public class Estante 
{

    private String nombre;
    private List<Libro> libros = new ArrayList<>();
    
    public Estante(String nombre) 
    {
        this.nombre = nombre;
    }

    public String getNombre()
    {
        return this.nombre;
    }
    public void setNombre(String nombre)
    {
        this.nombre = nombre;
    }
    
    public List<Libro> getLibros()
    {
        return this.libros;
    }
    public void setLibros(List<Libro> libros)
    {
        this.libros = libros;
    }
    

    public void agregarLibro(Libro l)
    {
        if (l == null) return;
     
        Libro existente = buscarPorIsbn(l.getIsbn()); 
        if (existente != null) 
        {
            existente.ajustarStock(l.getStock());
            if (l.getPrecio() > 0) existente.setPrecio(l.getPrecio());
            if (l.getTitulo() != null) existente.setTitulo(l.getTitulo());
            if (l.getAutor() != null) existente.setAutor(l.getAutor());
        } else{
            libros.add(l);
        }
    }

    public void agregarLibro(String titulo, String autor, double precio, int stock, String isbn) 
    {   
        libros.add(new Libro(titulo, autor, precio, stock, isbn));
    }
    
    // Busquedas
    public Libro buscarPorIsbn(String isbn) 
    {
        if (isbn == null) return null;
        for (Libro l : libros) 
        {
            if (isbn.equalsIgnoreCase(l.getIsbn())) return l;
        }
        return null;
    }
    
    public List<Libro> buscarLibrosPorTitulo(String titulo)
    {
        List<Libro> res = new ArrayList<>();
        if (titulo == null) return res;
        String q = titulo.toLowerCase();
        for (Libro l : libros)
            {
            if (l.getTitulo() != null && l.getTitulo().toLowerCase().contains(q)) 
            {
                res.add(l);
            }
        }
        return res;
    }

    public void listarLibros() 
    {
        if (libros.isEmpty()) 
        {
            System.out.println("  (sin libros)");
            return;
        }
        for (Libro l : libros) {
            System.out.println("  • " + l);
        }
    }

    @Override
    public String toString() 
    {
        return "Estante: " + nombre + " (" + libros.size() + " libros)";
    }
}





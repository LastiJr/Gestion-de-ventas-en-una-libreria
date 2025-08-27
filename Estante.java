package libreria;

import java.util.ArrayList;
import java.util.List;

public class Estante {

    private String nombre;
    private List<Libro> libros = new ArrayList<>();
    
    public Estante(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre(){
        return this.nombre;
    }
    public void setNombre(String nombre){
        this.nombre = nombre;
    }
    
    public List<Libro> getLibros(){
        return this.libros;
    }
    public void setLibros(List<Libro> libros){
        this.libros = libros;
    }



    public void agregarLibro(Libro libro) {
        if (libro != null) libros.add(libro);
    }
    public void agregarLibro(String titulo, String autor, double precio, int stock, String isbn) {
        libros.add(new Libro(titulo, autor, precio, stock, isbn));
    }
    public Libro buscarPorIsbn(String isbn) {
        if (isbn == null) return null;
        for (Libro l : libros) {
            if (isbn.equalsIgnoreCase(l.getIsbn())) return l;
        }
        return null;
    }
    public int cantidadLibros() { return libros.size(); }

    public String toString() {
        return "Estante: " + nombre + " (" + libros.size() + " libros)";
    }
}



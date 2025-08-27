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
}


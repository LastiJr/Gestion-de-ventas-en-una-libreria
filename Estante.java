package libreria;

import java.util.ArrayList;
import java.util.List;

public class Estante {

    private String nombre;
    private List<Libro> libros = new ArrayList<>();
    
    public Estante(String nombre) {
        this.nombre = nombre;
    }
}

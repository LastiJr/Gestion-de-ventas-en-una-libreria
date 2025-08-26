package libreria;

import java.util.*;

public class Sistema 
{
    public static void main(String[] args)
    {    
        System.out.println("Sistema de Gestión de Ventas de Librería");
    }
}

private void seed()
{
    Estante novelas = new Estante("Novelas");
    novelas.agregarLibro("El Quijote", "Cervantes", 12000, 5, "ISBN001");
    novelas.agregarLibro("1984", "George Orwell", 10000, 3, "ISBN002");

    Estante tecnologia = new Estante("Tecnología");
    tecnologia.agregarLibro("Clean Code", "Robert C. Martin", 25000, 4, "ISBN100");
    tecnologia.agregarLibro("Effective Java", "Joshua Bloch", 27000, 2, "ISBN101");
    
    estantes.add(novelas);
    estantes.add(tecnologia);
    
    for (Estante e : estantes) 
    {
        for (Libro l : e.getLibros()) 
        {
            catalogo.put(l.getIsbn(), l);
        }
    }
}

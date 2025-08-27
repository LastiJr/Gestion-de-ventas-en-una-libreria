package libreria;

import java.util.*;

public class Sistema
{

    private final List<Estante> estantes = new ArrayList<>();      
    private final Map<String, Libro> catalogo = new HashMap<>();   

    public static void main(String[] args) 
    {
        Sistema app = new Sistema();
        System.out.println("Sistema de Gestión de Ventas de Librería");
        app.seed();    
        app.menu();    
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

    private void menu() 
    {
        System.out.println("\n=== Catálogo precargado (demo SIA1.4) ===");
        for (Estante e : estantes) 
        {
            System.out.println("- " + e.getNombre());
            e.listarLibros();
        }
        System.out.println("=========================================\n");
    }
}

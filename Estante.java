package com.mycompany.gestionlibreria;

import java.util.ArrayList;
import java.util.List;

public class Estante {

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
    
    public boolean agregarLibro(Libro l) 
    {
        if (l == null) return false;
        if (buscarPorIsbn(l.getIsbn()) != null) 
        {
            System.out.println("No se agregó: ya existe un libro con ISBN " + l.getIsbn());
            return false;
        }
        libros.add(l);
        return true;
    }

 
    public boolean agregarLibro(String titulo, String autor, double precio, int stock, String isbn, String editorial) 
    {   
        if (buscarPorIsbn(isbn) != null) 
        {
            System.out.println("No se agregó: ya existe un libro con ISBN " + isbn);
            return false;
        }
        libros.add(new Libro(titulo, autor, precio, stock, isbn, editorial));
        return true;
    }
    
    public Libro buscarPorIsbn(String isbn) 
    {
        if (isbn == null) return null;
        for (Libro l : libros) 
        {
            if (isbn.equalsIgnoreCase(l.getIsbn())) return l;
        }
        return null;
    }

    public void listarLibros() 
    {
        if (libros.isEmpty()) 
        {
            System.out.println("  (sin libros)");
            return;
        }
        for (Libro l : libros) 
        {
            System.out.println("  • " + l);
        }
    }

    @Override
    public String toString() 
    {
        return "Estante: " + nombre + " (" + libros.size() + " libros)";
    }
}

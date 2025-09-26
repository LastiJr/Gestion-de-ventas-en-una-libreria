package com.mycompany.gestionlibreria;

public class Libro {
    
    private String titulo;
    private String autor;
    private double precio;
    private int stock;
    private String isbn;
    private String editorial;
    

    public Libro(String titulo, String autor, double precio, int stock, String isbn, String editorial)
    {
        this.titulo = titulo;
        this.autor = autor;
        this.precio = precio;
        this.stock = stock;
        this.isbn = isbn;
        this.editorial = editorial;
    }
    
    public String getTitulo()
    {
        return this.titulo;
    }
    public void setTitulo(String titulo)
    {
        this.titulo = titulo;
    }
    
    public String getAutor()
    {
        return this.autor;
    }
    public void setAutor(String autor)
    {
        this.autor = autor;
    }
    
    public double getPrecio()
    {
        return this.precio;
    }
    public void setPrecio(double precio)
    {
        this.precio = precio;
    }
    
    public int getStock()
    {
        return this.stock;
    }
    public void setStock(int stock)
    {
        this.stock = stock;
    }
    
    public String getIsbn()
    {
        return this.isbn;
    }
    public void setIsbn(String isbn)
    {
        this.isbn = isbn;
    }

    public String getEditorial() 
    {
        return editorial;
    }
    public void setEditorial(String editorial) 
    {
        this.editorial = editorial;
    }

    
    public void ajustarStock(int delta)
    {
        int nuevo = this.stock + delta;
        if (nuevo < 0) nuevo = 0;
        this.stock = nuevo;
    }

    @Override
    public String toString() 
    {
        return String.format("[%s] \"%s\" - %s (%s) | $%.0f | stock: %d",
                isbn, titulo, autor, editorial, precio, stock);
    }
}

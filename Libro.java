package libreria;

public class Libro {
    
    private String titulo;
    private String autor;
    private double precio;
    private int stock;
    private String isbn;
    
    public Libro(String titulo, String autor, double precio, int stock, String isbn){
        
        this.titulo = titulo;
        this.autor = autor;
        this.precio = precio;
        this.stock = stock;
        this.isbn = isbn;
    }
}

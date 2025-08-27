package libreria;

public class Cliente {
    
    private String nombre;
    private String rut;
    private String correo;
    
    public Cliente(String nombre, String rut, String correo){
        
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    public String getNombre(){
        return this.nombre;
    }
    public void setNombre(String nombre){
        this.nombre = nombre;
    }
    
    public String getRut(){
        return this.rut;
    }
    public void setRut(String rut){
        this.rut = rut;
    }
    
    public String getCorreo(){
        return this.correo;
    }
    public void setCorreo(String correo){
        this.correo = correo;
    }

    
    public String toString() {
        return nombre + " (" + rut + ")";
    }
}





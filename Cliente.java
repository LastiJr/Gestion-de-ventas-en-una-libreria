package libreria;

public class Cliente
{
    
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
    
    // Sobrecarga SIA 1.6
    public void setNombre(String nombres, String apellidos){
        this.nombre = (nombres == null ? "" : nombres.trim()) + " " + (apellidos == null ? "" : apellidos.trim());
        this.nombre = this.nombre.trim();
    }
    
    public void setCorreo(String localPart, String dominio){
        if (localPart == null) localPart = "";
        if (dominio == null) dominio = "";
        this.correo = localPart.trim() + "@" + dominio.trim();
    }
    
    public void setRut(int baseNumerica, char digitoVerificador){
        this.rut = baseNumerica + "-" + Character.toUpperCase(digitoVerificador);
    }
    
    @Override
    public String toString(){
        return nombre + " (" + rut + ")";
    }

    // Dos clientes son "iguales" si tienen el mismo RUT.
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Cliente)) return false;
        
        Cliente cliente = (Cliente) o;
        
        return Objects.equals(this.rut, cliente.rut);
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.rut);
    }
      
}






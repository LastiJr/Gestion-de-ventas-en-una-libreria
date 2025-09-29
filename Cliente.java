package com.mycompany.gestionlibreria;

import java.util.Objects;

public class Cliente {
    
    private String nombre;
    private String rut;
    private String correo;
    

    public Cliente(String nombre, String rut, String correo) throws RutInvalido, CorreoInvalido {
        this.nombre = nombre;
        setRut(rut);       
        setCorreo(correo); 
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
    public void setRut(String rut) throws RutInvalido {
        if (rut == null || rut.trim().isEmpty()) {
            throw new RutInvalido("RUT vacío");
        }
        if (rut.length() < 8) {
            throw new RutInvalido("RUT demasiado corto");
        }

        this.rut = rut.trim();
    }
    
    public String getCorreo(){
        return this.correo;
    }
    public void setCorreo(String correo) throws CorreoInvalido {
        if (correo == null || !correo.contains("@")) {
            throw new CorreoInvalido("Correo inválido: falta @");
        }
        if (correo.startsWith("@") || correo.endsWith("@")) {
            throw new CorreoInvalido("Correo inválido: posición incorrecta de @");
        }
        this.correo = correo.trim();
    }
    

    public void setNombre(String nombres, String apellidos){
        this.nombre = (nombres == null ? "" : nombres.trim()) + " " + (apellidos == null ? "" : apellidos.trim());
        this.nombre = this.nombre.trim();
    }
    
    public void setCorreo(String localPart, String dominio) throws CorreoInvalido {
        if (localPart == null || dominio == null || localPart.trim().isEmpty() || dominio.trim().isEmpty()) {
            throw new CorreoInvalido("Correo inválido: partes vacías");
        }
        this.correo = localPart.trim() + "@" + dominio.trim();
    }
    
    public void setRut(int baseNumerica, char digitoVerificador) throws RutInvalido {
        if (baseNumerica <= 0) {
            throw new RutInvalido("RUT numérico inválido");
        }
        this.rut = baseNumerica + "-" + Character.toUpperCase(digitoVerificador);
    }
    
    @Override
    public String toString(){
        return nombre + " (" + rut + ")";
    }


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

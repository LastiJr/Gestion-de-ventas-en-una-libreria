package com.mycompany.gestionlibreria;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;

public class Sistema 
{
    private static Sistema instance;
    public static synchronized Sistema getInstance() 
    {
        if (instance == null) 
            instance = new Sistema();
        return instance;
    }

    private String currentStylesheet;
    public String getCurrentStylesheet() 
    { 
        return currentStylesheet; 
    }
    public void setCurrentStylesheet(String css) 
    { 
        this.currentStylesheet = css; 
    }

    private ServicioCorreo correoService;
    public ServicioCorreo getCorreoService() 
    { 
        return correoService; 
    }
    public void setCorreoService(ServicioCorreo sc) 
    { 
        this.correoService = sc; 
    }

    private final List<Estante> estantes = new ArrayList<>();
    private final Map<String, Libro> catalogo = new HashMap<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final Map<String, Cliente> clientesPorRut = new HashMap<>();
    private final List<Venta> ventas = new ArrayList<>();

    public List<Estante> getEstantes() 
    { 
        return estantes; 
    }
    public Map<String, Libro> getCatalogo() 
    { 
        return catalogo; 
    }
    public List<Cliente> getClientes() 
    { 
        return clientes; 
    }
    public Map<String, Cliente> getClientesPorRut() 
    { 
        return clientesPorRut; 
    }
    public List<Venta> getVentas() 
    { 
        return ventas; 
    }

    private Sistema() 
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> 
        {
            try { guardarDatosEnCSV(); } catch (Exception ignored) {}
        }));
        cargarDatosDesdeCSV();
    }

    public Estante buscarEstantePorNombre(String nombre) 
    {
        for (Estante e : estantes) 
            if (e.getNombre().equalsIgnoreCase(nombre)) 
                return e;
        return null;
    }
    public Libro buscarLibroPorIsbn(String isbn) 
    { 
        return catalogo.get(isbn); 
    }

    public void agregarClienteAlSistema(Cliente c)
    {
        clientes.add(c);
        if (c.getRut()!=null) 
            clientesPorRut.put(c.getRut().toLowerCase(), c);
    }

    public String nextVentaId() 
    { 
        return "V" + System.currentTimeMillis(); 
    }


    public void confirmarVenta(Venta v, boolean enviarCorreo) 
    {
        if (v == null) 
            return;

        for (Libro item : v.getItems()) 
        {
            Libro real = catalogo.get(item.getIsbn());
            int cant = Math.max(0, item.getStock());
            if (real == null || real.getStock() < cant) 
            {
                throw new IllegalStateException("Stock agotado para ISBN: "+ (real != null ? real.getIsbn() : item.getIsbn()));
            }
        }

        for (Libro item : v.getItems()) 
        {
            Libro real = catalogo.get(item.getIsbn());
            int cant = Math.max(0, item.getStock());
            real.setStock(real.getStock() - cant);
        }

        boolean enviado = false;
        if (enviarCorreo && correoService != null) 
        {
            try 
            {
                correoService.enviarVenta(v);
                enviado = true;
            } catch (Exception e) 
            {
                System.err.println("[Correo] Error enviando comprobante: " + e.getMessage());
            }
        }
        v.setCorreoEnviado(enviado);

        ventas.add(v);
        try 
        {
            guardarDatosEnCSV();
        } catch (Exception e) 
        {
            System.err.println("[CSV] Error al guardar post-confirmación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private void menu() throws IOException 
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) 
        {
            System.out.println("\n=== MENÚ ===");
            System.out.println("1) Registrar VENTA simple");
            System.out.println("0) Salir");
            String op = br.readLine();
            if ("0".equals(op)) 
                break;
            if ("1".equals(op)) 
            {
                System.out.print("RUT: "); String r = br.readLine().trim().toLowerCase();
                Cliente cli = clientesPorRut.get(r);
                if (cli == null) 
                { System.out.println("Cliente no existe"); 
                continue; 
                }
                Venta v = new Venta(nextVentaId(), LocalDate.now(), cli);
                while (true) 
                {
                    System.out.print("ISBN (vacío fin): ");
                    String isbn = br.readLine().trim();
                    if (isbn.isEmpty()) 
                        break;
                    Libro l = catalogo.get(isbn);
                    if (l==null)
                    { 
                        System.out.println("No existe"); 
                        continue; 
                    }
                    System.out.print("Cant: ");
                    int cant = Integer.parseInt(br.readLine().trim());
                    if (l.getStock() < cant)
                    { 
                        System.out.println("Stock agotado"); 
                        continue; 
                    }
                    v.getItems().add(new Libro(l.getTitulo(), l.getAutor(), l.getPrecio(), cant, l.getIsbn(), l.getEditorial()));
                }
                confirmarVenta(v, true);
                System.out.println("Venta confirmada.");
            }
        }
    }

    public void cargarDatosDesdeCSV() 
    {
        try 
        {
            PersistenciaCSV.cargar(this);
            System.out.println("[CSV] Carga completada.");
        } catch (Exception e) 
        {
            System.err.println("[CSV] Error al cargar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void guardarDatosEnCSV() 
    {
        try 
        {
            PersistenciaCSV.guardar(this);
            System.out.println("[CSV] Guardado completado.");
        } catch (Exception e) 
        {
            System.err.println("[CSV] Error al guardar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

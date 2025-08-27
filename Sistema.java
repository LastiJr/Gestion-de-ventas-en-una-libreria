package libreria;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;


public class Sistema 
{

    // Colecciones principales
    private final List<Estante> estantes = new ArrayList<>();     
    private final Map<String, Libro> catalogo = new HashMap<>();  
    private final List<Cliente> clientes = new ArrayList<>();
    private final Map<String, Cliente> clientesPorRut = new HashMap<>();
    private final List<Venta> ventas = new ArrayList<>();

    public static void main(String[] args) 
    {
        Sistema app = new Sistema();
        System.out.println("Sistema de Gestión de Ventas de Librería");
        app.seed();
        
        try 
        {
            app.menu();
        } catch (IOException e) 
        {
            System.out.println("Error de entrada/salida: " + e.getMessage());
        }
        
        System.out.println("Hasta luego.");
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

        refrescarCatalogo();

        agregarClienteAlSistema(new Cliente("Ana Gómez", "11.111.111-1", "ana@example.com"));
        agregarClienteAlSistema(new Cliente("Luis Pérez", "22.222.222-2", "luis@example.com"));
    }

    private void refrescarCatalogo() 
    {
        catalogo.clear();
        for (Estante e : estantes) 
        {
            for (Libro l : e.getLibros()) 
            {
                catalogo.put(l.getIsbn(), l);
            }
        }
    }

    private void agregarClienteAlSistema(Cliente c) 
    {
        if (c == null) return;
        clientes.add(c);
        
        if (c.getRut() != null) 
        {
            clientesPorRut.put(c.getRut().toLowerCase(), c);
        }
    }

    //MENUUUUUU
    
    private void menu() throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) 
        {
            System.out.println("\n=== MENÚ ===");
            System.out.println("1) Agregar LIBRO a un ESTANTE (inserción manual)");
            System.out.println("2) Listar LIBROS por ESTANTE");
            System.out.println("3) Buscar LIBRO por ISBN");
            System.out.println("4) Registrar VENTA simple");
            System.out.println("5) Listar VENTAS");
            System.out.println("6) Listar CLIENTES");
            System.out.println("7) Agregar CLIENTE");
            System.out.println("0) Salir");
            System.out.print("Opción: ");
            String op = br.readLine();
            if ("0".equals(op)) break;

            switch(op) 
            {
                case "1": opcionAgregarLibro(br); break; 
                case "2": opcionListarPorEstante(); break;    
                case "3": opcionBuscarPorIsbn(br); break;
                case "4": opcionRegistrarVenta(br); break;
                case "5": opcionListarVentas(); break;
                case "6": opcionListarClientes(); break;
                case "7": opcionAgregarCliente(br); break;
                default: System.out.println("Opción inválida");
            }
        }
    }

    private void opcionAgregarLibro(BufferedReader br) throws IOException 
    {
        System.out.print("Nombre del estante: ");
        String nombre = leerNoVacio(br);
        Estante est = buscarEstantePorNombre(nombre);
        if (est == null) 
        {
            System.out.print("No existe ese estante. ¿Crearlo? (s/n): ");
            String crear = br.readLine().trim();
            
            if (!"s".equalsIgnoreCase(crear)) return;
            
            est = new Estante(nombre);
            
            estantes.add(est);
        }

        System.out.print("ISBN: ");   String isbn = leerNoVacio(br);
        System.out.print("Título: "); String titulo = leerNoVacio(br);
        System.out.print("Autor: ");  String autor = leerNoVacio(br);
        System.out.print("Precio: "); double precio = parseDoubleSafe(br.readLine(), 0);
        System.out.print("Stock: ");  int stock = parseIntSafe(br.readLine(), 0);

        est.agregarLibro(titulo, autor, precio, stock, isbn);

        Libro recien = est.buscarPorIsbn(isbn);
        if (recien != null) catalogo.put(isbn, recien);
        System.out.println("Libro agregado al estante \"" + est.getNombre() + "\"");
    }

    private void opcionListarPorEstante() 
    {
        System.out.println("\n=== LISTADO POR ESTANTE ===");
        for (Estante e : estantes) 
        {
            System.out.println("- " + e.getNombre());
            e.listarLibros();
        }
        System.out.println("===========================");
    }

    private void opcionBuscarPorIsbn(BufferedReader br) throws IOException 
    {
        System.out.print("ISBN a buscar: ");
        String isbn = leerNoVacio(br);
        Libro l = catalogo.get(isbn);
        if (l == null) System.out.println("No se encontró el libro.");
        else System.out.println("Encontrado: " + l);
    }

    private void opcionRegistrarVenta(BufferedReader br) throws IOException 
    {
        System.out.print("RUT cliente: ");
        String rut = leerNoVacio(br).toLowerCase();
        Cliente cli = clientesPorRut.get(rut);
        if (cli == null) 
        {
            System.out.println("Cliente no existe. Vamos a crearlo.");
            System.out.print("Nombre: "); String nombre = leerNoVacio(br);
            System.out.print("Correo: "); String correo = br.readLine().trim();
            cli = new Cliente(nombre, rut, correo);
            agregarClienteAlSistema(cli);
        }

        // Crear venta
        String idVenta = nextVentaId();
        Venta v = new Venta(idVenta, LocalDate.now(), cli);

        // Loop de ítems
        while (true)
        {
            System.out.print("ISBN del libro (o ENTER para terminar): ");
            String isbn = br.readLine().trim();
            if (isbn.isEmpty()) break;

            Libro l = catalogo.get(isbn);
            
            if (l == null) 
            {
                System.out.println("No existe ese ISBN en el catálogo.");
                continue;
            }

            System.out.print("Cantidad: ");
            int cant = parseIntSafe(br.readLine(), 1);
            if (cant <= 0) { System.out.println("Cantidad inválida."); continue; }

            v.agregarItem(l, cant);
        }

        if (v.getItems().isEmpty()) 
        {
            System.out.println("No se agregaron ítems. Venta cancelada.");
            return;
        }

        ventas.add(v);
        System.out.println("Venta registrada:\n" + v);
    }

    private void opcionListarVentas()
    {
        if (ventas.isEmpty()) 
        {
            System.out.println("(no hay ventas)");
            return;
        }
        for (Venta v : ventas) 
        {
            System.out.println(v);
        }
    }

    private void opcionListarClientes() 
    {
        if (clientes.isEmpty())
        {
            System.out.println("(no hay clientes)");
            return;
        }
        for (Cliente c : clientes)
        {
            System.out.println("• " + c);
        }
    }

    private void opcionAgregarCliente(BufferedReader br) throws IOException 
    {
        System.out.print("Nombre: "); String nombre = leerNoVacio(br);
        System.out.print("RUT: ");    String rut = leerNoVacio(br);
        System.out.print("Correo: "); String correo = br.readLine().trim();
        
        if (clientesPorRut.containsKey(rut.toLowerCase())) 
        {
            System.out.println("Ya existe un cliente con ese RUT.");
            return;
        }
        agregarClienteAlSistema(new Cliente(nombre, rut, correo));
        System.out.println("Cliente agregado.");
    }

    // HELPERS
    private Estante buscarEstantePorNombre(String nombre) 
    {
        for (Estante e : estantes)
        {
            if (e.getNombre().equalsIgnoreCase(nombre)) return e;
        }
        return null;
    }

    private String leerNoVacio(BufferedReader br) throws IOException 
    {
        while (true) 
        {
            String s = br.readLine();
            if (s != null) 
            {
                s = s.trim();
                if (!s.isEmpty()) return s;
            }
            
            System.out.print("Valor no puede estar vacío. Intenta de nuevo: ");
        }
    }

    private int parseIntSafe(String s, int def) 
    {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private double parseDoubleSafe(String s, double def) 
    {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return def; }
    }

    private String nextVentaId() 
    {
        return "V" + System.currentTimeMillis();
    }
}



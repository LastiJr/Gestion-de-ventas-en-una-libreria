package com.mycompany.gestionlibreria;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import com.mycompany.gestionlibreria.RutInvalido;
import com.mycompany.gestionlibreria.CorreoInvalido;

public final class PersistenciaCSV 
{
    private PersistenciaCSV(){}

    private static final char DELIM = ';';
    private static final String CRLF = "\r\n";

    private static Path dataDir()
    { 
        return Paths.get("data"); 
    }
    private static Path f(String name)
    { 
        return dataDir().resolve(name); 
    }

    public static void guardar(Sistema sis) throws IOException 
    {
        if (!Files.exists(dataDir())) Files.createDirectories(dataDir());

        Map<String,Integer> comprasPorRut = new HashMap<>();
        Map<String,Long> totalPorVenta = new HashMap<>();
        
        for (Venta v : sis.getVentas()) 
        {
            long total = calcularTotalVenta(v);
            totalPorVenta.put(v.getIdVenta(), total);
            if (v.getCliente() != null && v.getCliente().getRut() != null) 
            {
                String rut = v.getCliente().getRut().toLowerCase();
                comprasPorRut.merge(rut, 1, Integer::sum);
            }
        }

        try (BufferedWriter bw = newWriter(f("clientes.csv"))) 
        {
            writeHeader(bw, "nombre", "rut", "correo", "compras");
            for (Cliente c : sis.getClientes()) 
            {
                String rut = c.getRut() == null ? "" : c.getRut().toLowerCase();
                int compras = comprasPorRut.getOrDefault(rut, 0);
                writeRow(bw, c.getNombre(), c.getRut(), c.getCorreo(), String.valueOf(compras));
            }
        }
        
        try (BufferedWriter bw = newWriter(f("estantes.csv"))) 
        {
            writeHeader(bw, "nombre");
            for (Estante e : sis.getEstantes()) writeRow(bw, e.getNombre());
        }
        
        try (BufferedWriter bw = newWriter(f("libros.csv"))) 
        {
            writeHeader(bw, "isbn", "titulo", "autor", "editorial", "precio", "stock", "estante");
            for (Estante e : sis.getEstantes()) 
            {
                for (Libro l : e.getLibros()) 
                {
                    writeRow(bw,
                            l.getIsbn(),
                            l.getTitulo(),
                            l.getAutor(),
                            l.getEditorial(),
                            String.valueOf(Math.round(l.getPrecio())),
                            String.valueOf(l.getStock()),
                            e.getNombre());
                }
            }
        }
        
        try (BufferedWriter bw = newWriter(f("ventas.csv"))) 
        {
            writeHeader(bw, "idVenta", "fecha", "rutCliente", "total", "correoEnviado");
            for (Venta v : sis.getVentas()) 
            {
                String rut = (v.getCliente()!=null)? v.getCliente().getRut() : "";
                long total = totalPorVenta.getOrDefault(v.getIdVenta(), 0L);
                writeRow(bw, v.getIdVenta(), String.valueOf(v.getFecha()), rut,
                        String.valueOf(total), v.isCorreoEnviado() ? "SI" : "NO");
            }
        }
        
        try (BufferedWriter bw = newWriter(f("venta_items.csv"))) 
        {
            writeHeader(bw, "idVenta", "isbn", "cantidad");
            for (Venta v : sis.getVentas()) 
            {
                Map<String,Integer> cantidades = new LinkedHashMap<>();
                for (Libro item : v.getItems()) 
                {
                    int cant = item.getStock();
                    cantidades.merge(item.getIsbn(), cant, Integer::sum);
                }
                for (Map.Entry<String,Integer> e : cantidades.entrySet()) 
                {
                    writeRow(bw, v.getIdVenta(), e.getKey(), String.valueOf(e.getValue()));
                }
            }
        }
    }

    public static void cargar(Sistema sis) throws IOException 
    {
        sis.getEstantes().clear();
        sis.getCatalogo().clear();
        sis.getClientes().clear();
        sis.getClientesPorRut().clear();
        sis.getVentas().clear();

        if (!Files.exists(dataDir())) Files.createDirectories(dataDir());

        Map<String,Estante> estMap = new LinkedHashMap<>();
        Path fe = f("estantes.csv");
        if (Files.exists(fe)) 
        {
            try (BufferedReader br = newReader(fe)) 
            {
                skipHeader(br);
                String line;
                while ((line = br.readLine()) != null) 
                {
                    if (line.trim().isEmpty()) 
                        continue;
                    String[] a = parse(line);
                    String nombre = unesc(get(a,0));
                    if (nombre.isEmpty()) 
                        continue;
                    Estante e = new Estante(nombre);
                    estMap.put(nombre, e);
                    sis.getEstantes().add(e);
                }
            }
        }

        Path fc = f("clientes.csv");
        if (Files.exists(fc)) 
        {
            try (BufferedReader br = newReader(fc)) 
            {
                skipHeader(br);
                String line;
                while ((line = br.readLine()) != null) 
                {
                    if (line.trim().isEmpty()) 
                        continue;
                    String[] a = parse(line);
                    String nombre = unesc(get(a,0));
                    String rut    = unesc(get(a,1));
                    String correo = unesc(get(a,2));


                    try {
                        Cliente c = new Cliente(nombre, rut, correo);
                        sis.getClientes().add(c);
                        if (c.getRut()!=null) 
                            sis.getClientesPorRut().put(c.getRut().toLowerCase(), c);
                    } catch (RutInvalido | CorreoInvalido ex) {
                        System.err.println("[CSV] Cliente descartado por datos inválidos: " + ex.getMessage());

                    }
                }
            }
        }

        Path fl = f("libros.csv");
        if (Files.exists(fl)) 
        {
            try (BufferedReader br = newReader(fl)) 
            {
                skipHeader(br);
                String line;
                while ((line = br.readLine()) != null) 
                {
                    if (line.trim().isEmpty()) 
                        continue;
                    String[] a = parse(line);
                    if (a.length < 7) 
                        continue;
                    String isbn      = unesc(get(a,0));
                    String titulo    = unesc(get(a,1));
                    String autor     = unesc(get(a,2));
                    String editorial = unesc(get(a,3));
                    double precio    = parseDoubleSafe(get(a,4), 0);
                    int stock        = parseIntSafe(get(a,5), 0);
                    String estante   = unesc(get(a,6));

                    Libro l = new Libro(titulo, autor, precio, stock, isbn, editorial);
                    sis.getCatalogo().put(isbn, l);

                    Estante e = estMap.get(estante);
                    if (e == null) 
                    {
                        e = new Estante(estante);
                        estMap.put(estante, e);
                        sis.getEstantes().add(e);
                    }
                    e.getLibros().add(l);
                }
            }
        }

        
        Map<String,Venta> ventasTmp = new LinkedHashMap<>();
        Path fv = f("ventas.csv");
        if (Files.exists(fv)) 
        {
            try (BufferedReader br = newReader(fv)) 
            {
                skipHeader(br);
                String line;
                while ((line = br.readLine()) != null) 
                {
                    if (line.trim().isEmpty()) 
                        continue;
                    String[] a = parse(line);
                    String id    = unesc(get(a,0));
                    String fch   = unesc(get(a,1));
                    String rut   = unesc(get(a,2)).toLowerCase();
                    Cliente cli  = sis.getClientesPorRut().get(rut);
                    LocalDate ld = (fch==null || fch.isEmpty()) ? LocalDate.now() : LocalDate.parse(fch);
                    Venta v = new Venta(id, ld, cli);

                    String enviadoStr = (a.length >= 5) ? unesc(get(a,4)) : "";
                    boolean enviado = "SI".equalsIgnoreCase(enviadoStr) || "true".equalsIgnoreCase(enviadoStr);
                    v.setCorreoEnviado(enviado);

                    ventasTmp.put(id, v);
                    sis.getVentas().add(v);
                }
            }
        }
        Path fvi = f("venta_items.csv");
        if (Files.exists(fvi)) 
        {
            try (BufferedReader br = newReader(fvi)) 
            {
                skipHeader(br);
                String line;
                while ((line = br.readLine()) != null) 
                {
                    if (line.trim().isEmpty()) continue;
                    String[] a = parse(line);
                    if (a.length < 3) continue;
                    String id   = unesc(get(a,0));
                    String isbn = unesc(get(a,1));
                    int cant    = parseIntSafe(get(a,2), 0);

                    Venta v = ventasTmp.get(id);
                    Libro ref = sis.getCatalogo().get(isbn);
                    if (v!=null && ref!=null && cant>0) 
                    {
                        v.getItems().add(new Libro(
                                ref.getTitulo(),
                                ref.getAutor(),
                                ref.getPrecio(),
                                cant,
                                ref.getIsbn(),
                                ref.getEditorial()
                        ));
                    }
                }
            }
        }
    }

    
    private static long calcularTotalVenta(Venta v)
    {
        long total = 0L;
        for (Libro item : v.getItems()) 
        {
            long precio = Math.round(item.getPrecio());
            int cant = Math.max(0, item.getStock());
            total += precio * cant;
        }
        return total;
    }


    private static BufferedWriter newWriter(Path path) throws IOException 
    {
        if (!Files.exists(path.getParent())) 
            Files.createDirectories(path.getParent());
        OutputStream os = Files.newOutputStream(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        os.write(0xEF); os.write(0xBB); os.write(0xBF);
        return new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }

    private static BufferedReader newReader(Path path) throws IOException 
    {
        return Files.newBufferedReader(path, StandardCharsets.UTF_8);
    }

    private static void writeHeader(Writer w, String... headers) throws IOException 
    {
        writeRow(w, headers);
    }

    private static void writeRow(Writer w, String... cols) throws IOException 
    {
        for (int i=0;i<cols.length;i++)
        {
            if (i>0) 
                w.write(DELIM);
            w.write(esc(cols[i]));
        }
        w.write(CRLF);
    }

    
    private static String esc(String s)
    {
        if (s == null) 
            return "";
        boolean needs = s.indexOf(DELIM) >= 0 || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"","\"\"");
        return needs ? ("\"" + v + "\"") : v;
    }

    private static String[] parse(String line)
    {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder(); boolean q=false;
        for (int i=0;i<line.length();i++)
        {
            char c = line.charAt(i);
            if (c=='"') 
            { 
                q = !q; continue; 
            }
            if (c==DELIM && !q)
            { 
                cols.add(cur.toString()); 
                cur.setLength(0); 
            }
            else cur.append(c);
        }
        cols.add(cur.toString());
        String[] out = new String[cols.size()];
        for (int i=0;i<cols.size();i++) out[i] = unesc(cols.get(i));
        return out;
    }

    private static String unesc(String s)
    {
        String t = s==null? "" : s.trim();
        if (t.startsWith("\"") && t.endsWith("\"") && t.length()>=2) 
        {
            t = t.substring(1, t.length()-1).replace("\"\"", "\"");
        }
        return t;
    }

    private static String get(String[] a, int idx)
    {
        return (idx>=0 && idx<a.length)? a[idx] : "";
    }

    private static int parseIntSafe(String s, int def)
    {
        try 
        { 
            return Integer.parseInt(s.trim()); 
        } catch(Exception e){ return def; }
    }

    private static double parseDoubleSafe(String s, double def)
    {
        try 
        { 
            return Double.parseDouble(s.trim()); 
        } catch(Exception e){ return def; }
    }

    private static void skipHeader(BufferedReader br) throws IOException 
    {
        br.readLine(); 
    }
}

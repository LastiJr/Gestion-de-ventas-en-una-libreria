package com.mycompany.gestionlibreria;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class ServicioCorreo {

    private final String smtpHost;
    private final int smtpPort;
    private final boolean startTls;
    private final String remitente;
    private final String clave; 

    public ServicioCorreo(String smtpHost, int smtpPort, boolean startTls, String remitente, String clave) 
    {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.startTls = startTls;
        this.remitente = remitente;
        this.clave = clave;
    }

    public void enviarVenta(Venta venta) 
    {
        if (venta == null || venta.getCliente() == null) 
            return;
        String destinatario = venta.getCliente().getCorreo();
        if (destinatario == null || destinatario.isBlank()) 
            return;
        try 
        {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", String.valueOf(startTls));
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));

            Session session = Session.getInstance(props, new Authenticator() 
            {
                @Override protected PasswordAuthentication getPasswordAuthentication() 
                {
                    return new PasswordAuthentication(remitente, clave);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(remitente, "Librería"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            msg.setSubject("Comprobante de compra — " + safe(venta.getIdVenta()));
            msg.setContent(construirHtml(venta), "text/html; charset=UTF-8");

            Transport.send(msg);
            System.out.println("[ServicioCorreo] Venta " + venta.getIdVenta() + " enviada a " + destinatario);
        } catch (Exception e) 
        {
            System.err.println("[ServicioCorreo] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String construirHtml(Venta v) 
    {
        NumberFormat clp = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:Arial,Helvetica,sans-serif'>");
        sb.append("<h2>¡Gracias por tu compra, ").append(safe(v.getCliente().getNombre())).append("!</h2>");
        sb.append("<p><b>N° de venta:</b> ").append(safe(v.getIdVenta()))
          .append("<br/><b>Fecha:</b> ").append(safe(String.valueOf(v.getFecha()))).append("</p>");

        sb.append("<table cellpadding='8' cellspacing='0' border='1' style='border-collapse:collapse;width:100%'>");
        sb.append("<tr>")
          .append("<th style='text-align:left'>ISBN</th>")
          .append("<th style='text-align:left'>Título</th>")
          .append("<th style='text-align:left'>Autor</th>")
          .append("<th style='text-align:right'>Precio</th>")
          .append("<th style='text-align:right'>Cant.</th>")
          .append("<th style='text-align:right'>Total libro</th>")
          .append("</tr>");

        long total = 0L;
        for (Libro l : v.getItems()) 
        {
            int cant = Math.max(0, l.getStock());        
            long precio = Math.round(l.getPrecio());     
            long linea  = precio * cant;                 
            total += linea;

            sb.append("<tr>")
              .append(td(l.getIsbn()))
              .append(td(l.getTitulo()))
              .append(td(l.getAutor()))
              .append(tdRight(clp.format(precio)))
              .append(tdRight(String.valueOf(cant)))
              .append(tdRight(clp.format(linea)))
              .append("</tr>");
        }

        sb.append("<tr><td colspan='5' style='text-align:right'><b>Total</b></td>")
          .append("<td style='text-align:right'><b>").append(clp.format(total)).append("</b></td></tr>");
        sb.append("</table>");
        sb.append("<p>Ante cualquier duda, responde este correo.</p>");
        sb.append("</div>");
        return sb.toString();
    }

    private static String td(String s) 
    { 
        return "<td>" + safe(s) + "</td>"; 
    }
    private static String tdRight(String s) 
    { 
        return "<td style='text-align:right'>" + safe(s) + "</td>"; 
    }
    private static String safe(String x) 
    { 
        return x == null ? "" : x; 
    }
}

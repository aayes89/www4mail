/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package www4mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import javax.net.ssl.HttpsURLConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author IT
 */
public class Www4mail {

    static Properties emailProps = null;
    static Session emailSession = null;
    static Folder emailFolder = null;
    static Store store = null;
    static String mailServer = "";// servidor entrada
    static String smtp = "";// servidor salida
    //static String mailStoreType = "imap";//"pop3" //imap
    static String userBuzon = "";// si procede
    static String passBuzon = "";//3c0m*2019";// si procede
    static String username = "";// si procede
    static String password = "";// si procede
    static String redirect = "";//user@dom;user2@domX";
    static int tipo = 1;
    static Scanner in = new Scanner(System.in);
    static String ipProx = "192.168.2.3";
    static int pPort = 3128;
    static int tipoProxy = 2;
    static String[] auth = {username, password};
    static URL url;
    static HttpsURLConnection hurlc;
    static URLConnection con;
    static boolean debug = false;
    static int cantMess = 0;
    static String ulink = "";//"https://www.google.com";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Inicializando las propiedades del servidor        
        if (loadConf() == true) {
            //initMailProps(mailServer, 110, false, smtp, 25, true, false);
            //Chequeo si hay correo
            try {
                fetchMails();
                } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private static HttpsURLConnection initConS(String link, String ipProx, int pPort, int tipoProxy) throws IOException {
        url = new URL(link);
        switch (tipoProxy) {
            case 0://No proxy
                hurlc = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
                break;
            case 1:
                Proxy p = new Proxy(Proxy.Type.DIRECT, new InetSocketAddress(ipProx, pPort));
                hurlc = (HttpsURLConnection) url.openConnection(p);
                break;
            case 2:
                Proxy ps = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipProx, pPort));
                hurlc = (HttpsURLConnection) url.openConnection(ps);
                break;
            /*case 3:
             SocksProxy sp = SocksProxy.create(new InetSocketAddress(ipProxy, pPort), pPort);
             con = urlink.openConnection(sp);
             break;*/
        }
        hurlc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36");
        return hurlc;
    }

    static URLConnection initCon(String urlink, String ipProxy, int pPort, int tipoProxy) throws IOException {

        url = new URL(urlink);
        switch (tipoProxy) {
            case 0://No proxy
                con = url.openConnection(Proxy.NO_PROXY);
                break;
            case 1:
                Proxy p = new Proxy(Proxy.Type.DIRECT, new InetSocketAddress(ipProxy, pPort));
                con = url.openConnection(p);
                break;
            case 2:
                Proxy ps = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ipProxy, pPort));
                con = url.openConnection(ps);
                break;
            /*case 3:
             SocksProxy sp = SocksProxy.create(new InetSocketAddress(ipProxy, pPort), pPort);
             con = urlink.openConnection(sp);
             break;*/
        }
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0");
        return con;
    }

    static void createAttachFile(String url, String data) throws IOException {
        FileWriter fw = new FileWriter("raw_data");
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (data.contains("<!doctype html>")) {
            fw = new FileWriter("pag.html");
        } else if (url.endsWith("[php|xhtml|html|htm|asp|aspx|jar|java|exe|com|bat|bin|tar|gz|tgz|zip|rar|7z|mp3|mp4|mkv|avi|mpg|mpeg|jpg|jpeg|bmp|gif|png|txt|pdf|doc|docx|xls|xlsx|ppt|pptx|db|access|sql]")) {
            String[] urlPath = url.split("/");
            fw = new FileWriter(urlPath[urlPath.length - 1]);
        }
        fw.write(data);
        fw.close();
    }
    
    static boolean nextVal(String line) {
        if (line.split("=").length == 2) {
            return true;
        }
        return false;
    }

    static boolean findMatches(String line) {
        //System.out.println(line);
        if (line.charAt(0) == '#' || line.charAt(0) == ';' || line.charAt(0) == '[' || line.charAt(0) == '\\' || line.charAt(0) == '/') {
            return true;
        }
        return false;
        //return Pattern.matches("[|#|/|\\|;]", line);
    }

    static boolean loadConf() {
        File config = new File("settings.conf");
        try {
            in = new Scanner(config);
            while (in.hasNext()) {
                String line = in.nextLine();
                //System.out.println(line);
                if (findMatches(line)) {
                    continue;
                } else if (line.contains("debug=")) {
                    if (nextVal(line)) {
                        debug = Boolean.valueOf(line.split("=")[1]);
                    }
                } else if (line.contains("mailServer=")) {

                    if (nextVal(line)) {
                        mailServer = line.split("=")[1];
                    }
                } else if (line.contains("smtp=")) {
                    if (nextVal(line)) {
                        smtp = line.split("=")[1];
                    }
                } else if (line.contains("usrb=")) {
                    if (nextVal(line)) {
                        userBuzon = line.split("=")[1];
                    }
                } else if (line.contains("login=")) {
                    if (nextVal(line)) {
                        username = line.split("=")[1];
                    }
                } else if (line.contains("passb=")) {
                    if (nextVal(line)) {
                        passBuzon = line.split("=")[1];
                    }
                } else if (line.contains("passwd=")) {
                    if (nextVal(line)) {
                        password = line.split("=")[1];
                    }
                }/* else if (line.contains("redirect=")) {
                 if (nextVal(line)) {
                 redirect = line.split("=")[1];
                 }
                 } */ else if (line.contains("type=")) {
                    if (nextVal(line)) {
                        tipo = Integer.valueOf(line.split("=")[1]);
                    }
                } else if (line.contains("proxy=")) {
                    if (nextVal(line)) {
                        ipProx = line.split("=")[1];
                    }
                } else if (line.contains("pport=")) {
                    if (nextVal(line)) {
                        pPort = Integer.valueOf(line.split("=")[1]);
                    }
                } else if (line.contains("proxyType=")) {
                    if (nextVal(line)) {
                        tipoProxy = Integer.valueOf(line.split("=")[1]);
                    }
                }
            }
            return allNeedOK();

        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
            return false;
        }

    }

    static boolean allNeedOK() {
        if (!mailServer.isEmpty() && !smtp.isEmpty() && !userBuzon.isEmpty() && !username.isEmpty() && !passBuzon.isEmpty() && !password.isEmpty()) {//&& !redirect.isEmpty()) {
            return true;
        }
        return false;
    }

    static void initMailProps(String mailServer, int popPort, boolean popTLS, String smtpServer, int smtpPort, boolean auth, boolean smtpTLS) {
        emailProps = new Properties();
//Si hay proxy
        /* 
         props.setProperty("http.proxyHost", "192.168.2.3");
         props.setProperty("http.proxyPort", "3128"); */
        //emailProps.put("mail.pop3.host", popServer);
        //emailProps.put("mail.pop3.port", popPort);//995
        emailProps.put("mail.pop3.rsetbeforequit", false);
        emailProps.put("mail.pop3.disabletop", false);
        emailProps.put("mail.pop3.forgettopheaders", false);
        emailProps.put("mail.pop3.cachewriteto", false);
        emailProps.put("mail.pop3.filecache.enable", false);
        emailProps.put("mail.pop3.keepmessagecontent", false);
        emailProps.put("mail.pop3.starttls.enable", false);
        emailProps.put("mail.pop3.starttls.required", false);
        emailProps.put("mail.pop3.finalizecleanclose", false);
        emailProps.put("mail.pop3.apop.enable", false);
        emailProps.put("mail.pop3.disablecapa", false);
        //emailProps.put("mail.imap.host", imap);
        //emailProps.put("mail.smtp.host", smtpServer);//586
        //emailProps.put("mail.smtp.port", smtpPort);//586
        emailProps.put("mail.smtp.auth", auth);

        /*emailProps.put("mail.smtp.auth", "true
         emailProps.put("mail.imap.ssl.enable", "true"); // required for Gmail
         emailProps.put("mail.imap.sasl.enable", "true");
         emailProps.put("mail.imap.sasl.mechanisms", "XOAUTH2");
         emailProps.put("mail.imap.auth.login.disable", "true");
         emailProps.put("mail.imap.auth.plain.disable", "true");
         emailProps.put("mail.smtp.auth.mechanisms", "XOAUTH2");*/
        //emailProps.put("mail.smtp.starttls.enable", smtpTLS);
    }

    static Message proccessMessage(Message message) throws MessagingException, IOException {

        System.out.println("Bandera: " + message.getFlags());
        //Cambia la bandera a visto y respondido, para evitar procesarlo más de una vez                    
        message.setFlag(Flags.Flag.ANSWERED, true);
        message.setFlag(Flags.Flag.SEEN, true);
        message.setFlag(Flags.Flag.DELETED, true);
        /*System.out.println("---------------------------------");
         //System.out.println("Email Number " + (i + 1));
         System.out.println("Asunto: " + message.getSubject());
         System.out.println("De: " + message.getFrom()[0]);
         System.out.println("Mensaje: ");*/

        if (message.getContent() instanceof MimeMultipart) {
            MimeMultipart part = (MimeMultipart) message.getContent();
            int cParts = part.getCount();

            if (cParts <= 2) {
                ulink = parseBodyMessage(part.getBodyPart(0).getContent().toString());
            } else {
                for (int j = 0; j < cParts; j++) {
                    System.out.println(part.getBodyPart(j).getContent().toString());
                }
            }
        } else {
            ulink = message.getContent().toString().split("\n")[0];
            if (ulink.contains("google")) {
                ulink = ulink.split("&")[0];
            }
        }
        System.out.println("URL capturada: " + ulink);

        if (ulink.length() >= 1) {
            System.out.println("Download mode 1");
            if (ulink.startsWith("https://") || ulink.startsWith("HTTPS://")) {
                hurlc = initConS(ulink, ipProx, pPort, tipoProxy);
            } else if (ulink.startsWith("http://") || ulink.startsWith("HTTP://")) {
                con = initCon(ulink, ipProx, pPort, tipoProxy);
            }

            //}
            if (hurlc != null) {
                in = new Scanner(hurlc.getInputStream());
            } else if (con != null) {
                in = new Scanner(con.getInputStream());
                //En caso de que se ingrese una URL como HTTP y se resuelva como HTTPS
                String tlink = con.getHeaderField("Location");
                if (tlink != null) {
                    System.out.println("Redireccionando a " + tlink);
                    //in=new Scanner(initCon(tlink, ipProx, pPort, tipoProxy).getInputStream());
                    if (tlink.startsWith("https://")) {
                        hurlc = initConS(tlink, ipProx, pPort, tipoProxy);
                        in = new Scanner(hurlc.getInputStream());
                    }
                }
            }
            System.out.println("Request Properties");
            con.getRequestProperties().values().forEach(System.out::println);
            System.out.println("Header Fields KEYS");
            con.getHeaderFields().keySet().forEach(System.out::println);
            System.out.println("Header Fields VALUES");
            con.getHeaderFields().values().forEach(System.out::println);
            StringBuffer sb = new StringBuffer();
            while (in.hasNext()) {
                //System.out.println(in.nextLine());
                sb.append(in.nextLine());
            }
            createAttachFile(ulink, sb.toString());
            for (Address add : message.getFrom()) {
                redirect += add.toString() + ";";
            }
            sendMail(redirect, "POST", sb.toString());
        } else {
            System.out.println("No hay mensajes nuevos");
        }

        return message;
    }

    static void fetchMails() throws MessagingException {//recursion en caso de excepcion
        Message[] messages = new Message[1];
        try {
            emailSession = Session.getDefaultInstance(new Properties());
            emailSession.setDebug(debug);
            //Crea un repositorio del servicio invocado            
            if (tipo == 1) {
                store = emailSession.getStore("pop3");//"pop3,pop3s,imap,imaps,smtp,smtps"
                store.connect(mailServer, userBuzon, passBuzon);
            } else if (tipo == 2) {
                store = emailSession.getStore("pop3s");
                store.connect(mailServer, userBuzon, passBuzon);
            } else if (tipo == 3) {
                store = emailSession.getStore("imap");
                store.connect(mailServer, userBuzon, passBuzon);
            } else if (tipo == 4) {
                store = emailSession.getStore("imaps");
                store.connect(mailServer, userBuzon, passBuzon);
            }

            //Obtiene el directorio del buzon o el especifico
            emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);

            SearchTerm terms = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            SearchTerm bySubject = new SubjectTerm("GET");
            if (bySubject == null) {
                bySubject = new SubjectTerm("get");
            }
            // Obtiene los mensajes en el directorio en un arreglo para mostrarlos
            System.out.println("Mensajes en buzón: " + emailFolder.getUnreadMessageCount());
            messages = emailFolder.search(terms);
            messages = emailFolder.search(bySubject);
            System.out.println("Cantidad de mensajes en el buzon con parámetros deseados: " + messages.length);

            while (cantMess < messages.length) {
                Message message = messages[cantMess];
                if (message.getSubject().contains("GET") || message.getSubject().contains("get")) {
                    proccessMessage(message);

                    cantMess++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Cierra y sale del directorio
            emailFolder.close(true);
            store.close();
        }

    }

    static void sendMail(String para, String asunto, String msg) throws NoSuchProviderException, MessagingException {
        Transport transport = emailSession.getTransport("smtp");
        //Autenticar con el servidor
        transport.connect(smtp, username, password);
        //Crea un borrador del mensaje
        MimeMessage emailMessage = crearMensajeBorrador(para, asunto, msg);
        //Envia el mensaje
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        //Cierra el flujo 
        transport.close();
        System.out.println("Correo enviado correctamente");
    }

    private static MimeMessage crearMensajeBorrador(String dest, String asunto, String mensaje) throws AddressException, MessagingException {
        String[] toEmails = dest.split(";");
        String emailSubject = asunto;
        String emailBody = mensaje;
        MimeMessage emailMessage = new MimeMessage(emailSession);
        emailMessage.addFrom(new Address[]{new InternetAddress(username)});
        //Establece los destinatarios
        for (int i = 0; i < toEmails.length; i++) {
            emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmails[i]));
        }
        //Cabecera para formato del correo        
        emailMessage.addHeader("Content-type", "text/html; charset=UTF-8");
        emailMessage.addHeader("format", "flowed");
        emailMessage.addHeader("Content-Transfer-Encoding", "8bit");
        //Asunto del correo
        emailMessage.setSubject(emailSubject);
        //Para enviar correo con formato HTML
        emailMessage.setContent(emailBody, "text/html;charset=UTF-8");
        //Para enviar solo texto sin formato
        //emailMessage.setText(emailBody);// for a text email
        return emailMessage;
    }

    private static String parseBodyMessage(String body) {
        return (body.split("\n")[0]);
    }

}

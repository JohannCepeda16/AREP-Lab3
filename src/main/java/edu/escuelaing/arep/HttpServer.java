package edu.escuelaing.arep;

import java.net.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer {

	private static HttpServer _instance = new HttpServer();
    private final HashMap<String, Method> services = new HashMap<String, Method>();

    private HttpServer() {

    }

    public static HttpServer getInstance() {
        return _instance;
    }

    public void startServer(List<String> args) throws IOException, URISyntaxException {
        int port = 35000;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
            System.exit(1);
        }
        loadComponents(args);
        Socket clientSocket = null;
        boolean running = true;
        while (running) {
            try {
                System.out.println("Listo para recibir en puerto ..." + port);
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            messageConnection(clientSocket);
        }

        serverSocket.close();
    }

    private void loadComponents(List<String> componentsList) {
        for (String component : componentsList) {
            Class<?> c = null;
            try {
                c = Class.forName(component);
                for (Method m : c.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(GetMapping.class)) {
                        String uri = m.getAnnotation(GetMapping.class).value();
                        services.put(uri, m);
                    }
                }
            } catch (ClassNotFoundException e) {
                Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, "Component not found", e);
            }
        }
		System.out.println(services);
    }

    public void messageConnection(Socket clientSocket) throws IOException, URISyntaxException {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String inputLine, outputLine;
        ArrayList<String> request = new ArrayList<String>();
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Received: " + inputLine);
            request.add(inputLine);
            if (!in.ready()) {
                break;
            }
        }
        String uriStr = request.get(0).split(" ")[1];
        URI resourceURI = new URI(uriStr);
        System.out.println("uri path " + resourceURI.getPath());
        System.out.println("uri query " + resourceURI.getQuery());
        if (resourceURI.getPath().startsWith("/do/")) {
            outputLine = getServiceResponse(resourceURI);
            // outputLine = defaultHttpMessage(); ;
            out.println(outputLine);
        } else {
            outputLine = getHtmlResource(resourceURI);
            // outputLine = defaultHttpMessage(); ;
            out.println(outputLine);
        }

        out.close();

        in.close();

        clientSocket.close();
    }

    public String getServiceResponse(URI serviceURI) {
        String response = "";
        // the path has the form: "/do/*"
        Method m = services.get(serviceURI.getPath().substring(3));
        if(m == null){
            return default404HTMLResponse("Service Not found");
        }
        try {
            response = m.invoke(null,serviceURI.getQuery()).toString();
        } catch (IllegalAccessException e) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, "Component not found", e);
        } catch (InvocationTargetException e) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, "Component not found", e);
        }
        response = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" + "\r\n" + response;
        return response;
    }

    private String getHtmlResource(URI resourceURI) {
        String type = "text/html";
        if (resourceURI.getPath().endsWith(".css")) {
            type = "text/css";
        } else if (resourceURI.getPath().endsWith(".js")) {
            type = "text/javascript";
        } else if (resourceURI.getPath().endsWith(".jpeg")) {
            type = "image/jpeg";
        } else if (resourceURI.getPath().endsWith(".png")) {
            type = "image/png";
        }
        // para leer archivos
        Path file = Paths.get("public_html" + resourceURI.getPath());
        Charset charset = Charset.forName("UTF-8");
        String response = "";
        try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                response = "HTTP/1.1 200 OK\r\n" + "Content-Type: " + type + "\r\n" + "\r\n" + line;
                ;
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
        return response;
    }

    public String defaultHttpMessage(){
        String outputLine =
                "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "<!DOCTYPE html>"
                        + "<html>"
                        + " <head>"
                        + " <title>TODO supply a title</title>"
                        + " <meta charset=\"UTF-8\">"
                        + " <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                        + " </head>"
                        + " <body>"
                        + " <div><h1>My first page.</h1></div>"
                        + " <img src=\"https://hubblesite.org/files/live/sites/hubble/files/home/resource-gallery/articles/_images/STSCI-H-p1427a-2300x2100.jpg?t=tn1200\">"
                        + " </body>"
                        + "</html>";
        return outputLine;
    }

    public String default404HTMLResponse(String msg){
        String outputLine =
            "HTTP/1.1 404 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "\r\n"
                    + "<!DOCTYPE html>"
                    + "<html>"
                    + " <head>"
                    + " <title>404</title>"
                    + " <meta charset=\"UTF-8\">"
                    + " <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                    + " </head>"
                    + " <body>"
                    + " <div><h1>"+msg+"</h1></div>"
                    + " </body>"
                    + "</html>";
        return outputLine;        
    }
}

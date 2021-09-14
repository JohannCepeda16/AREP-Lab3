package edu.escuelaing.arep;

public class WebAppStart {
    public static void main(String[] args) {
        try {
            HttpServer.getInstance().startServer(args);;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }    
}

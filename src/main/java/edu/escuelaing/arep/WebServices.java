package edu.escuelaing.arep;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WebAppStart {
    public static void main(String[] args) {
        try {
            HttpServer.getInstance().startServer(findComponents());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static List<String> findComponents() {
        List<String> javaFiles = new ArrayList<String>();
        try {
            javaFiles = Files.walk(Paths.get("./src/main/java/co/org/escuelaing/networking")).map(Path::getFileName)
                    .map(Path::toString).filter(n -> n.endsWith(".java"))
                    .filter(c -> c.getClass().isAnnotationPresent(Component.class)).collect(Collectors.toList());

            System.out.println(javaFiles);
        } catch (Exception e) {
            Logger.getLogger(WebAppStart.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }

        return javaFiles;
    }
}

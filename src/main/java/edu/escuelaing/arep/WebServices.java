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

public class WebServices {
    public static void main(String[] args) {
        // findComponents();
        try {
            HttpServer.getInstance().startServer(findComponents());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static List<String> findComponents() {
        List<String> javaFiles = new ArrayList<String>();
        List<String> components = new ArrayList<String>();
        String path = "./src/main/java/edu/escuelaing/arep";
        try {
            javaFiles = Files.walk(Paths.get(path)).map(Path::getFileName).map(Path::toString)
                    .filter(n -> n.endsWith(".java")).collect(Collectors.toList());

            for (String name : javaFiles) {
                Class<?> cls = Class.forName("edu.escuelaing.arep." + name.substring(0, name.length() - 5));
                if (cls.isAnnotationPresent(Component.class)) {
                    components.add("edu.escuelaing.arep." + name.substring(0, name.length() - 5));
                }
            }
        } catch (Exception e) {
            Logger.getLogger(WebServices.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }

        return components;
    }
}

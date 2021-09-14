package edu.escuelaing.arep;

import java.time.LocalTime;

import edu.escuelaing.arep.anotations.Component;

@Component
public class WebServices {

    @GetMapping("/fecha")
    public static String date(String s) {
        return LocalTime.now().toString();
    }

}

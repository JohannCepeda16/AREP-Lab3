package edu.escuelaing.arep;

@Component
public class MathServices {

	@GetMapping("/square")
	public static Double square(String n) {
		return 5.5 * 5.0;
	}

	@GetMapping("PI")
	public static Double PI(String n){
		return Math.PI;
	}

	@GetMapping("/strcount")
	public static String length(String s){
		return "The length of the string is: " +  s.length();
	}
}

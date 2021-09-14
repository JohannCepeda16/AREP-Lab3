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

	public void startServer(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(35000);
		} catch (IOException e) {
			System.err.println("Could not listen on port: 35000.");
			System.exit(1);
		}
		Socket clientSocket = null;
		boolean running = true;
		while (running) {
			try {
				System.out.println("Listo para recibir ...");
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				System.err.println("Accept failed.");
				System.exit(1);
			}
		}
		loadComponents(args);
		processRequest(clientSocket);
		serverSocket.close();
	}

	public void processRequest(Socket clientSocket) throws IOException {
		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String inputLine, outputLine;
		String method = "", path = "", version = "";
		List<String> headers = new ArrayList<String>();
		while ((inputLine = in.readLine()) != null) {
			if (method.isEmpty()) {
				String[] requestInfo = inputLine.split(" ");
				method = requestInfo[0];
				path = requestInfo[1];
				version = requestInfo[2];
				System.out.println("Request: " + method + " " + path + " " + version);
			} else {
				System.out.println("Header: " + inputLine);
				headers.add(inputLine);
			}

			System.out.println("Received: " + inputLine);
			if (!in.ready()) {
				break;
			}
		}

		outputLine = getResponse(path);
		out.println(outputLine);
		out.close();
		in.close();

		clientSocket.close();
	}

	private void loadComponents(String[] componentsList) {

		for (String component : componentsList) {
			try {
				Class<?> c = Class.forName(component);
				for (Method m : c.getDeclaredMethods()) {
					if (m.isAnnotationPresent(GetMapping.class)) {
						String uri = m.getAnnotation(GetMapping.class).value();
						services.put(uri, m);
					}
				}
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, "Component not found.", ex);
			}
		}
	}

	public String getServiceResponse(URI serviceURI) {
		String response = "";
		// The path has the form: "/do/*"
		Method m = services.get(serviceURI.getPath().substring(3));
		try {
			if (m == null) {
				return "Service not found";
			}
			response = m.invoke(null).toString();
		} catch (IllegalAccessException ex) {
			Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InvocationTargetException ex) {
			Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
		}
		response = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html\r\n" + "\r\n" + response;
		return response;
	}

	public String getResponse(String path) {
		String type = "text/html";
		if (path.endsWith(".css")) {
			type = "text/css";
		} else if (path.endsWith(".js")) {
			type = "text/javascript";
		}

		Path file = Paths.get("./www" + path);
		Charset charset = Charset.forName("UTF-8");
		String outMsg = "";
		try {
			BufferedReader reader = Files.newBufferedReader(file, charset);
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				outMsg += "\r\n" + line;
			}
		} catch (IOException x) {
			System.err.format("IOException: %s%n", x);
		}

		return "HTTP/1.1 200 OK\r\n" + "Content-Type: " + type + "\r\n" + "\r\n" + "<!DOCTYPE html>" + outMsg;
	}

	public static HttpServer getInstance() {
		return _instance;
	}
}

package com.akarsha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

@SpringBootApplication
public class AkarshaApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(AkarshaApplication.class, args);
        } catch (Throwable e) {
            startDummyServer(e);
        }
    }

    private static void startDummyServer(Throwable originalException) {
        try {
            String portStr = System.getenv("PORT");
            int port = (portStr != null && !portStr.isEmpty()) ? Integer.parseInt(portStr) : 8080;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", exchange -> {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                originalException.printStackTrace(pw);
                
                // If there's a cause, print it too
                if (originalException.getCause() != null) {
                    pw.println("\n--- CAUSE ---");
                    originalException.getCause().printStackTrace(pw);
                }
                
                String response = "SERVER CRASHED ON STARTUP:\n" + sw.toString();
                
                // Add CORS headers
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });
            server.setExecutor(null);
            server.start();
            System.out.println("Dummy server started on port " + port + " to broadcast crash stacktrace.");
        } catch (Exception dummyEx) {
            dummyEx.printStackTrace();
            System.exit(1);
        }
    }
}

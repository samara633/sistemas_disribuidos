import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.net.InetSocketAddress;

public class SimpleHttpServer {
    public static void main(String[] args) throws IOException {
        int serverPort = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/calcular", new CalculadoraHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + serverPort);
    }

    static class CalculadoraHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseFormData(exchange.getRequestBody());
                int arraySize = Integer.parseInt(params.get("arraySize"));
                int numThreads = Integer.parseInt(params.get("numThreads"));
                int[] arreglo = new int[arraySize];
                for (int i = 0; i < arreglo.length; i++) {
                    arreglo[i] = i + 1;
                }

                // Calcular la suma del arreglo como antes
                int sumaTotal = 0;
                for (int valor : arreglo) {
                    sumaTotal += valor;
                }

                String response = "La suma total es: " + sumaTotal;

                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.length());

                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private Map<String, String> parseFormData(InputStream input) throws IOException {
            Map<String, String> formData = new HashMap<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] keyValue = line.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    formData.put(key, value);
                }
            }
            return formData;
        }
    }
}

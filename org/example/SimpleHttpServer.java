package org.example;

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
        server.setExecutor(null); // creates a default executor
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

                int parteArreglo = arreglo.length / numThreads;
                int resto = arreglo.length % numThreads;

                SumadorHilo[] sumadores = new SumadorHilo[numThreads];
                int inicio = 0;
                int fin = 0;

                for (int i = 0; i < numThreads; i++) {
                    if (i < resto) {
                        fin = inicio + parteArreglo + 1;
                    } else {
                        fin = inicio + parteArreglo;
                    }
                    sumadores[i] = new SumadorHilo(arreglo, inicio, fin);
                    inicio = fin;
                }

                int sumaTotal = 0;
                for (SumadorHilo sumador : sumadores) {
                    sumador.start();
                }

                for (SumadorHilo sumador : sumadores) {
                    try {
                        sumador.join();
                        sumaTotal += sumador.getSuma();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                String response = String.valueOf(sumaTotal);

                // Configuramos la respuesta como texto plano
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                // Indicamos el código de estado 200 OK y el tamaño de la respuesta
                exchange.sendResponseHeaders(200, response.length());
                // Enviamos la respuesta al cliente
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private Map<String, String> parseFormData(InputStream input) throws IOException {
            Map<String, String> formData = new HashMap<>();
            InputStreamReader isr = new InputStreamReader(input);
            BufferedReader reader = new BufferedReader(isr);
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

    static class SumadorHilo extends Thread {
        private int[] arreglo;
        private int inicio;
        private int fin;
        private int suma;

        public SumadorHilo(int[] arreglo, int inicio, int fin) {
            this.arreglo = arreglo;
            this.inicio = inicio;
            this.fin = fin;
        }

        @Override
        public void run() {
            suma = 0;
            for (int i = inicio; i < fin; i++) {
                suma += arreglo[i];
            }
        }

        public int getSuma() {
            return suma;
        }
    }
}

import spark.Request;
import spark.Response;
import spark.Spark;
import java.util.Scanner;

public class SumaArreglo {

    public static void main(String[] args) {
        Spark.port(4567);
        Spark.post("/calcular", (request, response) -> {
            Scanner scanner = new Scanner(request.body());
            int arraySize = scanner.nextInt();
            int[] arreglo = new int[arraySize];
            for (int i = 0; i < arreglo.length; i++) {
                arreglo[i] = i + 1;
            }

            int numHilos = scanner.nextInt();

            int parteArreglo = arreglo.length / numHilos;
            int resto = arreglo.length % numHilos;

            SumadorHilo[] sumadores = new SumadorHilo[numHilos];
            int inicio = 0;
            int fin = 0;

            for (int i = 0; i < numHilos; i++) {
                if (i < resto) {
                    fin = inicio + parteArreglo + 1;
                } else {
                    fin = inicio + parteArreglo;
                }
                sumadores[i] = new SumadorHilo(arreglo, inicio, fin);
                inicio = fin;
            }

            for (SumadorHilo sumador : sumadores) {
                sumador.start();
            }

            int sumaTotal = 0;
            for (SumadorHilo sumador : sumadores) {
                try {
                    sumador.join();
                    sumaTotal += sumador.getSuma();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "La suma total es: " + sumaTotal;
        });
    }
}

class SumadorHilo extends Thread {
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

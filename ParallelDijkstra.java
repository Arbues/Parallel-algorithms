import java.util.Random;

public class ParallelDijkstra {

    // Definición de "infinito" (se usa para inicializar distancias)
    private static final int INF = 1000000000;

    // Número de nodos del grafo (lo definimos en un int para usarlo en la medición)
    private static final int NUM_NODOS = 1000; // Puedes ajustar este valor para realizar pruebas

    public static void main(String[] args) {
        // Generar un grafo aleatorio (matriz de adyacencia)
        int[][] grafo = generarGrafoAleatorio(NUM_NODOS);

        // Ejecutar la versión secuencial para todos los nodos
        System.out.println("Ejecutando algoritmo Dijkstra secuencial para todos los nodos...");
        long inicioSerial = System.nanoTime();
        int[][] resultadoSerial = dijkstraAllPairsSerial(grafo);
        long finSerial = System.nanoTime();
        long tiempoSerialMs = (finSerial - inicioSerial) / 1000000;
        System.out.println("Tiempo secuencial (ms): " + tiempoSerialMs);

        // Ejecutar la versión paralela para todos los nodos
        System.out.println("\nEjecutando algoritmo Dijkstra en paralelo (con hilos) para todos los nodos...");
        long inicioParalelo = System.nanoTime();
        int[][] resultadoParalelo = dijkstraAllPairsParalelo(grafo);
        long finParalelo = System.nanoTime();
        long tiempoParaleloMs = (finParalelo - inicioParalelo) / 1000000;
        System.out.println("Tiempo paralelo (ms): " + tiempoParaleloMs);

        // Comparar ambas matrices (resultado serial vs paralelo)
        boolean iguales = compararMatrices(resultadoSerial, resultadoParalelo);
        System.out.println("\n¿Las matrices de resultados son iguales? " + iguales);

        if (tiempoParaleloMs < tiempoSerialMs) {
            System.out.println("La versión paralela fue más rápida.");
        } else {
            System.out.println("La versión paralela NO fue más rápida que la secuencial.");
        }
    }

    /**
     * Genera un grafo aleatorio representado mediante una matriz de adyacencia.
     * Cada nodo (i,j) con i != j tendrá un peso aleatorio entre 1 y 10.
     * La diagonal se establece en 0.
     */
    private static int[][] generarGrafoAleatorio(int n) {
        int[][] grafo = new int[n][n];
        Random rnd = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    grafo[i][j] = 0;
                } else {
                    // En este ejemplo, generamos un grafo denso.
                    grafo[i][j] = rnd.nextInt(10) + 1; // pesos entre 1 y 10
                }
            }
        }
        return grafo;
    }

    /**
     * Algoritmo de Dijkstra para fuente única (implementación O(n^2)).
     * Se toma como entrada la matriz de adyacencia y el nodo fuente 'src'.
     * Retorna un array con las distancias mínimas desde 'src' a todos los demás nodos.
     */
    private static int[] dijkstra(int[][] grafo, int src) {
        int n = grafo.length;
        int[] dist = new int[n];
        boolean[] visitado = new boolean[n];

        // Inicializar distancias a "infinito" y visitado en false.
        for (int i = 0; i < n; i++) {
            dist[i] = INF;
            visitado[i] = false;
        }
        dist[src] = 0;

        // Se realizan n-1 iteraciones
        for (int count = 0; count < n - 1; count++) {
            // Seleccionar el vértice no visitado con la distancia mínima
            int u = -1;
            int minDist = INF;
            for (int i = 0; i < n; i++) {
                if (!visitado[i] && dist[i] < minDist) {
                    minDist = dist[i];
                    u = i;
                }
            }

            if (u == -1) {
                break; // No se encontró ningún vértice alcanzable
            }
            visitado[u] = true;

            // Actualizar las distancias de los vecinos de u
            for (int v = 0; v < n; v++) {
                if (!visitado[v] && dist[u] + grafo[u][v] < dist[v]) {
                    dist[v] = dist[u] + grafo[u][v];
                }
            }
        }
        return dist;
    }

    /**
     * Función que ejecuta Dijkstra para cada nodo como fuente (all-pairs)
     * de forma secuencial.
     */
    private static int[][] dijkstraAllPairsSerial(int[][] grafo) {
        int n = grafo.length;
        int[][] matrizResultado = new int[n][n];
        for (int src = 0; src < n; src++) {
            matrizResultado[src] = dijkstra(grafo, src);
        }
        return matrizResultado;
    }

    /**
     * Función que ejecuta Dijkstra para cada nodo como fuente (all-pairs)
     * de forma paralela. Se utiliza un arreglo de hilos para distribuir el cómputo.
     */
    private static int[][] dijkstraAllPairsParalelo(final int[][] grafo) {
        final int n = grafo.length;
        final int[][] matrizResultado = new int[n][];
        // Número de hilos a utilizar (por ejemplo, los núcleos disponibles)
        final int numHilos = Runtime.getRuntime().availableProcessors();
        Thread[] hilos = new Thread[numHilos];
        // Dividir las fuentes entre los hilos (división por bloques)
        final int fuentesPorHilo = (n + numHilos - 1) / numHilos; // división entera hacia arriba

        for (int t = 0; t < numHilos; t++) {
            final int inicio = t * fuentesPorHilo;
            // Aseguramos que no sobrepase el número de nodos
            final int fin = Math.min(n, inicio + fuentesPorHilo);
            hilos[t] = new Thread(() -> {
                for (int src = inicio; src < fin; src++) {
                    matrizResultado[src] = dijkstra(grafo, src);
                }
            });
            hilos[t].start();
        }
        // Esperar a que todos los hilos terminen
        for (int t = 0; t < numHilos; t++) {
            try {
                hilos[t].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return matrizResultado;
    }

    /**
     * Función que compara dos matrices de enteros y retorna true si son idénticas (mismo tamaño y mismos valores en cada posición).
     */
    private static boolean compararMatrices(int[][] m1, int[][] m2) {
        if (m1.length != m2.length) {
            return false;
        }
        for (int i = 0; i < m1.length; i++) {
            if (m1[i].length != m2[i].length) {
                return false;
            }
            for (int j = 0; j < m1[i].length; j++) {
                if (m1[i][j] != m2[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}

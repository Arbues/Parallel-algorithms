package Parallel_Dijkstra;

public class DijkstraAlgorithm {
    // Definición de "infinito" (se usa para inicializar distancias)
    private static final int INF = 1000000000;

    /**
     * Algoritmo de Dijkstra para fuente única (implementación O(n^2)).
     * Se toma como entrada la matriz de adyacencia y el nodo fuente 'src'.
     * Retorna un array con las distancias mínimas desde 'src' a todos los demás nodos.
     */
    public static int[] dijkstra(int[][] grafo, int src) {
        int n = grafo.length;
        int[] dist = new int[n];
        boolean[] visitado = new boolean[n];

        // Inicializar distancias a "infinito" y visitado en false.
        for (int i = 0; i < n; i++) {
            dist[i] = INF;
            visitado[i] = false;
        }
        dist[src] = 0;

        // Se realizan n-1 iteraciones porque cada nodo se visita una vez.
        for (int count = 0; count < n - 1; count++) {
            // Seleccionar el vértice no visitado con la distancia mínima
            int u = -1; // Inicializamos u como -1 para indicar que no se ha encontrado un vértice
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
    public static int[][] dijkstraAllPairsSerial(int[][] grafo) {
        int n = grafo.length;
        //
        int[][] matrizResultado = new int[n][n];
        // se iteranando sobre cada nodo como fuente
        for (int src = 0; src < n; src++) {
            matrizResultado[src] = dijkstra(grafo, src);
        }
        return matrizResultado;
    }

    /**
     * Función que ejecuta Dijkstra para cada nodo como fuente (all-pairs)
     * de forma paralela. Se utiliza un arreglo de hilos para distribuir el cómputo.
     */
    public static int[][] dijkstraAllPairsParalelo(final int[][] grafo) {
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
}
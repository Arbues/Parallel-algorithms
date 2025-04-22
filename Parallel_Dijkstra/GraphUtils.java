package Parallel_Dijkstra;

import java.util.Random;

public class GraphUtils {
    /**
     * Genera un grafo aleatorio representado mediante una matriz de adyacencia.
     * Cada nodo (i,j) con i != j tendrá un peso aleatorio entre 1 y 10.
     * La diagonal se establece en 0.
     */
    public static int[][] generarGrafoAleatorio(int n) {
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
}
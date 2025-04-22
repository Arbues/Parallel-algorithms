package Parallel_Dijkstra;

import java.util.Random;

public class ParallelDijkstra {

    // Definición de "infinito" (se usa para inicializar distancias)
    private static final int INF = 1000000000;

    // Número de nodos del grafo (lo definimos en un int para usarlo en la medición)
    private static final int NUM_NODOS = 2000; // Puedes ajustar este valor para realizar pruebas

    public static void main(String[] args) {
        // Generar un grafo aleatorio (matriz de adyacencia)
        int[][] grafo = GraphUtils.generarGrafoAleatorio(NUM_NODOS);

        // Ejecutar la versión secuencial para todos los nodos
        System.out.println("Ejecutando algoritmo Dijkstra secuencial para todos los nodos...");
        long inicioSerial = System.nanoTime();
        int[][] resultadoSerial = DijkstraAlgorithm.dijkstraAllPairsSerial(grafo);
        long finSerial = System.nanoTime();
        long tiempoSerialMs = (finSerial - inicioSerial) / 1000000;
        System.out.println("Tiempo secuencial (ms): " + tiempoSerialMs);

        // Ejecutar la versión paralela para todos los nodos
        System.out.println("\nEjecutando algoritmo Dijkstra en paralelo (con hilos) para todos los nodos...");
        long inicioParalelo = System.nanoTime();
        int[][] resultadoParalelo = DijkstraAlgorithm.dijkstraAllPairsParalelo(grafo);
        long finParalelo = System.nanoTime();
        long tiempoParaleloMs = (finParalelo - inicioParalelo) / 1000000;
        System.out.println("Tiempo paralelo (ms): " + tiempoParaleloMs);

        // Comparar ambas matrices (resultado serial vs paralelo)
        boolean iguales = MatrixUtils.compararMatrices(resultadoSerial, resultadoParalelo);
        System.out.println("\n¿Las matrices de resultados son iguales? " + iguales);

        if (tiempoParaleloMs < tiempoSerialMs) {
            System.out.println("La versión paralela fue más rápida.");
        } else {
            System.out.println("La versión paralela NO fue más rápida que la secuencial.");
        }
    }
}
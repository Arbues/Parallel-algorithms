package Parallel_Dijkstra;

public class MatrixUtils {
    /**
     * Función que compara dos matrices de enteros y retorna true si son idénticas (mismo tamaño y mismos valores en cada posición).
     */
    public static boolean compararMatrices(int[][] m1, int[][] m2) {
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
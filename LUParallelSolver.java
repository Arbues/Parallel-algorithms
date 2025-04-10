import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LUParallelSolver {
    static final int L = 2048;
    static final int h = 10;

    static double[][] A;
    static double[] b;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Tamaño de matriz (L): " + L);
        System.out.println("Número de hilos (h): " + h);

        A = generarMatrizAleatoria(L);
        b = generarVectorAleatorio(L);

        // === Serial ===
        double[][] Aserial = copyMatrix(A);
        long startSerial = System.nanoTime();
        double[][][] luSerial = luSerial(Aserial);
        double[] xSerial = resolverLU(luSerial[0], luSerial[1], b);
        long endSerial = System.nanoTime();
        double tiempoSerial = (endSerial - startSerial) / 1e9;

        // === Paralelo ===
        double[][] Aparallel = copyMatrix(A);
        long startParalelo = System.nanoTime();
        double[][][] luParalelo = luParalelo(Aparallel, h);
        double[] xParalelo = resolverLU(luParalelo[0], luParalelo[1], b);
        long endParalelo = System.nanoTime();
        double tiempoParalelo = (endParalelo - startParalelo) / 1e9;

        // === Resultados ===
        System.out.printf("Tiempo Serial:   %.5f segundos\n", tiempoSerial);
        System.out.printf("Tiempo Paralelo: %.5f segundos\n", tiempoParalelo);
        System.out.println("¿Soluciones similares?: " + compararVectores(xSerial, xParalelo));
    }

    public static double[][][] luSerial(double[][] A) {
        int n = A.length;
        double[][] L = new double[n][n];
        double[][] U = new double[n][n];

        for (int i = 0; i < n; i++) {
            L[i][i] = 1;
            for (int j = i; j < n; j++) {
                double sum = 0;
                for (int k = 0; k < i; k++) {
                    sum += L[i][k] * U[k][j];
                }
                U[i][j] = A[i][j] - sum;
            }
            for (int j = i + 1; j < n; j++) {
                double sum = 0;
                for (int k = 0; k < i; k++) {
                    sum += L[j][k] * U[k][i];
                }
                L[j][i] = (A[j][i] - sum) / U[i][i];
            }
        }
        return new double[][][]{L, U};
    }

    public static double[][][] luParalelo(double[][] A, int numThreads) throws InterruptedException {
        int n = A.length;
        double[][] L = new double[n][n];
        double[][] U = new double[n][n];

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < n; i++) {
            final int currentI = i;
            L[currentI][currentI] = 1;

            for (int j = currentI; j < n; j++) {
                double sum = 0;
                for (int k = 0; k < currentI; k++) {
                    sum += L[currentI][k] * U[k][j];
                }
                U[currentI][j] = A[currentI][j] - sum;
            }

            int range = n - (currentI + 1);
            if (range > 0) {
                int numTasks = Math.min(numThreads, range);
                List<Callable<Void>> tasks = new ArrayList<>();

                for (int t = 0; t < numTasks; t++) {
                    final int threadId = t;
                    tasks.add(() -> {
                        for (int j = currentI + 1 + threadId; j < n; j += numTasks) {
                            double sum = 0;
                            for (int k = 0; k < currentI; k++) {
                                sum += L[j][k] * U[k][currentI];
                            }
                            L[j][currentI] = (A[j][currentI] - sum) / U[currentI][currentI];
                        }
                        return null;
                    });
                }

                executor.invokeAll(tasks);
            }
        }

        executor.shutdown();
        return new double[][][]{L, U};
    }

    public static double[] resolverLU(double[][] L, double[][] U, double[] b) {
        int n = b.length;
        double[] y = new double[n];
        double[] x = new double[n];

        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < i; j++) {
                sum += L[i][j] * y[j];
            }
            y[i] = b[i] - sum;
        }

        for (int i = n - 1; i >= 0; i--) {
            double sum = 0;
            for (int j = i + 1; j < n; j++) {
                sum += U[i][j] * x[j];
            }
            x[i] = (y[i] - sum) / U[i][i];
        }

        return x;
    }

    public static double[][] generarMatrizAleatoria(int n) {
        double[][] M = new double[n][n];
        Random rand = new Random(0);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                M[i][j] = 1 + rand.nextDouble() * 10;
        return M;
    }

    public static double[] generarVectorAleatorio(int n) {
        double[] v = new double[n];
        Random rand = new Random(1);
        for (int i = 0; i < n; i++)
            v[i] = 1 + rand.nextDouble() * 10;
        return v;
    }

    public static double[][] copyMatrix(double[][] original) {
        int n = original.length;
        double[][] copy = new double[n][n];
        for (int i = 0; i < n; i++)
            System.arraycopy(original[i], 0, copy[i], 0, n);
        return copy;
    }

    public static boolean compararVectores(double[] a, double[] b) {
        double tol = 1e-6;
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++)
            if (Math.abs(a[i] - b[i]) > tol)
                return false;
        return true;
    }
}

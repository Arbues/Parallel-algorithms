import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.CountDownLatch;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Implementación del algoritmo de Coppersmith-Winograd para multiplicación de matrices
 * Incluye versión secuencial y dos versiones paralelas (ForkJoin y Threads tradicionales)
 */
public class CoppersmithWinogradMatrixMultiplication {
    
    // Umbral para cambiar a multiplicación regular cuando las matrices son pequeñas
    private static final int THRESHOLD = 128;
    
    // ForkJoinPool para la versión paralela
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool();
    
    /**
     * Implementación secuencial de Coppersmith-Winograd
     */
    public static double[][] multiplySequential(double[][] A, double[][] B) {
        int n = A.length;
        
        // Caso base: matrices pequeñas
        if (n <= THRESHOLD) {
            return multiplyDirectly(A, B);
        }
        
        // Dividir las matrices en submatrices
        int newSize = n / 2;
        double[][] a11 = subMatrix(A, 0, 0, newSize);
        double[][] a12 = subMatrix(A, 0, newSize, newSize);
        double[][] a21 = subMatrix(A, newSize, 0, newSize);
        double[][] a22 = subMatrix(A, newSize, newSize, newSize);
        
        double[][] b11 = subMatrix(B, 0, 0, newSize);
        double[][] b12 = subMatrix(B, 0, newSize, newSize);
        double[][] b21 = subMatrix(B, newSize, 0, newSize);
        double[][] b22 = subMatrix(B, newSize, newSize, newSize);
        
        // Calcular matrices intermedias (optimizaciones de Coppersmith-Winograd)
        double[][] s1 = subtract(a21, a11);
        double[][] s2 = add(a21, a22);
        double[][] s3 = subtract(a12, a22);
        double[][] s4 = subtract(b12, b11);
        double[][] s5 = subtract(b22, b12);
        double[][] s6 = subtract(b22, b21);
        double[][] s7 = add(a11, a12);
        double[][] s8 = add(a21, a22);
        
        // Multiplicaciones recursivas (7 en lugar de 8 como en el algoritmo clásico)
        double[][] p1 = multiplySequential(a11, b11);
        double[][] p2 = multiplySequential(a12, b21);
        double[][] p3 = multiplySequential(s1, s4);
        double[][] p4 = multiplySequential(s2, s5);
        double[][] p5 = multiplySequential(s3, s6);
        double[][] p6 = multiplySequential(s7, b22);
        double[][] p7 = multiplySequential(s8, b11);
        
        // Calcular submatrices del resultado
        double[][] c11 = add(subtract(add(p1, p2), p3), p4);
        double[][] c12 = add(p5, p6);
        double[][] c21 = add(p3, p7);
        double[][] c22 = subtract(add(p4, p5), p7);
        
        // Combinar las submatrices en el resultado
        return combineMatrices(c11, c12, c21, c22, n);
    }
    
    /**
     * Implementación paralela de Coppersmith-Winograd usando ForkJoinPool
     */
    public static double[][] multiplyParallel(double[][] A, double[][] B) {
        CWMultiplyTask task = new CWMultiplyTask(A, B);
        FORK_JOIN_POOL.invoke(task);
        return task.getResult();
    }
    
    /**
     * Clase para tareas recursivas en ForkJoinPool
     */
    private static class CWMultiplyTask extends RecursiveAction {
        private double[][] A;
        private double[][] B;
        private double[][] result;
        
        public CWMultiplyTask(double[][] A, double[][] B) {
            this.A = A;
            this.B = B;
            this.result = new double[A.length][B[0].length];
        }
        
        public double[][] getResult() {
            return result;
        }
        
        @Override
        protected void compute() {
            int n = A.length;
            
            // Caso base: matrices pequeñas
            if (n <= THRESHOLD) {
                result = multiplyDirectly(A, B);
                return;
            }
            
            // Dividir las matrices en submatrices
            int newSize = n / 2;
            double[][] a11 = subMatrix(A, 0, 0, newSize);
            double[][] a12 = subMatrix(A, 0, newSize, newSize);
            double[][] a21 = subMatrix(A, newSize, 0, newSize);
            double[][] a22 = subMatrix(A, newSize, newSize, newSize);
            
            double[][] b11 = subMatrix(B, 0, 0, newSize);
            double[][] b12 = subMatrix(B, 0, newSize, newSize);
            double[][] b21 = subMatrix(B, newSize, 0, newSize);
            double[][] b22 = subMatrix(B, newSize, newSize, newSize);
            
            // Calcular matrices intermedias
            double[][] s1 = subtract(a21, a11);
            double[][] s2 = add(a21, a22);
            double[][] s3 = subtract(a12, a22);
            double[][] s4 = subtract(b12, b11);
            double[][] s5 = subtract(b22, b12);
            double[][] s6 = subtract(b22, b21);
            double[][] s7 = add(a11, a12);
            double[][] s8 = add(a21, a22);
            
            // Crear tareas para las multiplicaciones
            CWMultiplyTask p1Task = new CWMultiplyTask(a11, b11);
            CWMultiplyTask p2Task = new CWMultiplyTask(a12, b21);
            CWMultiplyTask p3Task = new CWMultiplyTask(s1, s4);
            CWMultiplyTask p4Task = new CWMultiplyTask(s2, s5);
            CWMultiplyTask p5Task = new CWMultiplyTask(s3, s6);
            CWMultiplyTask p6Task = new CWMultiplyTask(s7, b22);
            CWMultiplyTask p7Task = new CWMultiplyTask(s8, b11);
            
            // Invocar las tareas en paralelo
            invokeAll(p1Task, p2Task, p3Task, p4Task, p5Task, p6Task, p7Task);
            
            // Obtener resultados
            double[][] p1 = p1Task.getResult();
            double[][] p2 = p2Task.getResult();
            double[][] p3 = p3Task.getResult();
            double[][] p4 = p4Task.getResult();
            double[][] p5 = p5Task.getResult();
            double[][] p6 = p6Task.getResult();
            double[][] p7 = p7Task.getResult();
            
            // Calcular submatrices del resultado
            double[][] c11 = add(subtract(add(p1, p2), p3), p4);
            double[][] c12 = add(p5, p6);
            double[][] c21 = add(p3, p7);
            double[][] c22 = subtract(add(p4, p5), p7);
            
            // Combinar las submatrices en el resultado
            result = combineMatrices(c11, c12, c21, c22, n);
        }
    }
    
    /**
     * IMPLEMENTACIÓN CORRECTA: Paralela con hilos tradicionales
     * Utilizando el mismo algoritmo de Coppersmith-Winograd pero con hilos tradicionales
     */
    public static double[][] multiplyParallelTraditional(double[][] A, double[][] B) {
        int n = A.length;
        
        // Para matrices pequeñas usar el método directo
        if (n <= THRESHOLD) {
            return multiplyDirectly(A, B);
        }
        
        // Dividir las matrices en submatrices
        int newSize = n / 2;
        double[][] a11 = subMatrix(A, 0, 0, newSize);
        double[][] a12 = subMatrix(A, 0, newSize, newSize);
        double[][] a21 = subMatrix(A, newSize, 0, newSize);
        double[][] a22 = subMatrix(A, newSize, newSize, newSize);
        
        double[][] b11 = subMatrix(B, 0, 0, newSize);
        double[][] b12 = subMatrix(B, 0, newSize, newSize);
        double[][] b21 = subMatrix(B, newSize, 0, newSize);
        double[][] b22 = subMatrix(B, newSize, newSize, newSize);
        
        // Calcular matrices intermedias
        double[][] s1 = subtract(a21, a11);
        double[][] s2 = add(a21, a22);
        double[][] s3 = subtract(a12, a22);
        double[][] s4 = subtract(b12, b11);
        double[][] s5 = subtract(b22, b12);
        double[][] s6 = subtract(b22, b21);
        double[][] s7 = add(a11, a12);
        double[][] s8 = add(a21, a22);
        
        // Arreglos para almacenar resultados
        final double[][][] results = new double[7][][];
        
        // Usar CountDownLatch para sincronización
        final CountDownLatch latch = new CountDownLatch(7);
        
        // Crear e iniciar hilos para las 7 multiplicaciones
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                results[0] = multiplySequential(a11, b11);
                latch.countDown();
            }
        });
        
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                results[1] = multiplySequential(a12, b21);
                latch.countDown();
            }
        });
        
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                results[2] = multiplySequential(s1, s4);
                latch.countDown();
            }
        });
        
        Thread t4 = new Thread(new Runnable() {
            @Override
            public void run() {
                results[3] = multiplySequential(s2, s5);
                latch.countDown();
            }
        });
        
        Thread t5 = new Thread(new Runnable() {
            @Override
            public void run() {
                results[4] = multiplySequential(s3, s6);
                latch.countDown();
            }
        });
        
        Thread t6 = new Thread(new Runnable() {
            @Override
            public void run() {
                results[5] = multiplySequential(s7, b22);
                latch.countDown();
            }
        });
        
        Thread t7 = new Thread(new Runnable() {
            @Override
            public void run() {
                results[6] = multiplySequential(s8, b11);
                latch.countDown();
            }
        });
        
        // Iniciar todos los hilos
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        
        // Esperar a que todos los hilos terminen
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Extraer resultados
        double[][] p1 = results[0];
        double[][] p2 = results[1];
        double[][] p3 = results[2];
        double[][] p4 = results[3];
        double[][] p5 = results[4];
        double[][] p6 = results[5];
        double[][] p7 = results[6];
        
        // Calcular submatrices del resultado
        double[][] c11 = add(subtract(add(p1, p2), p3), p4);
        double[][] c12 = add(p5, p6);
        double[][] c21 = add(p3, p7);
        double[][] c22 = subtract(add(p4, p5), p7);
        
        // Combinar las submatrices en el resultado
        return combineMatrices(c11, c12, c21, c22, n);
    }
    
    /**
     * Multiplicación directa de matrices (algoritmo tradicional O(n³))
     */
    private static double[][] multiplyDirectly(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        
        return C;
    }
    
    /**
     * Extrae una submatriz de una matriz
     */
    private static double[][] subMatrix(double[][] matrix, int rowStart, int colStart, int size) {
        double[][] subMatrix = new double[size][size];
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                subMatrix[i][j] = matrix[rowStart + i][colStart + j];
            }
        }
        
        return subMatrix;
    }
    
    /**
     * Combina cuatro submatrices en una matriz cuadrada
     */
    private static double[][] combineMatrices(double[][] c11, double[][] c12, double[][] c21, double[][] c22, int n) {
        double[][] result = new double[n][n];
        int newSize = n / 2;
        
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                result[i][j] = c11[i][j];
                result[i][j + newSize] = c12[i][j];
                result[i + newSize][j] = c21[i][j];
                result[i + newSize][j + newSize] = c22[i][j];
            }
        }
        
        return result;
    }
    
    /**
     * Suma dos matrices
     */
    private static double[][] add(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        
        return C;
    }
    
    /**
     * Resta dos matrices
     */
    private static double[][] subtract(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        
        return C;
    }
    
    /**
     * Imprime una matriz
     */
    private static void printMatrix(double[][] matrix) {
        int n = matrix.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.printf("%.2f\t", matrix[i][j]);
            }
            System.out.println();
        }
    }
    
    /**
     * Programa principal para demostrar el funcionamiento
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Pedir tamaño de matrices
        System.out.print("Introduce el tamaño de las matrices (debe ser potencia de 2): ");
        int n = 1024; // Valor por defecto
        
        try {
            n = scanner.nextInt();
            // Asegurar que sea potencia de 2
            if ((n & (n - 1)) != 0) {
                int nextPow2 = 1;
                while (nextPow2 < n) nextPow2 <<= 1;
                System.out.println("El tamaño debe ser potencia de 2. Ajustando " + n + " a " + nextPow2);
                // 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768
                n = nextPow2;
            }
        } catch (Exception e) {
            System.out.println("Entrada inválida. Usando tamaño predeterminado: " + n);
            scanner.nextLine(); // Limpiar buffer
        }
        
        System.out.println("\nMultiplicando matrices de tamaño " + n + "x" + n);
        
        // Crear matrices
        double[][] A = new double[n][n];
        double[][] B = new double[n][n];
        
        // Llenar matrices con valores aleatorios
        System.out.println("Inicializando matrices con valores aleatorios...");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = Math.random() * 10;
                B[i][j] = Math.random() * 10;
            }
        }
        
        // Preguntar si imprimir matrices de entrada
        System.out.print("¿Quieres imprimir las matrices de entrada? (S/N): ");
        String printInput = scanner.next();
        
        if (printInput.equalsIgnoreCase("S")) {
            System.out.println("\nMatriz A:");
            printMatrix(A);
            System.out.println("\nMatriz B:");
            printMatrix(B);
        }
        
        // Variables para medir tiempos
        long startTime, endTime, seqTime, parTime, parTraditionalTime;
        double[][] C1, C2, C3;
        
        // Calentar la JVM para mediciones más precisas
        System.out.println("\nCalentando JVM...");
        multiplySequential(new double[128][128], new double[128][128]);
        multiplyParallel(new double[128][128], new double[128][128]);
        multiplyParallelTraditional(new double[128][128], new double[128][128]);
        
        // Ejecutar versión secuencial
        System.out.println("\nEjecutando versión secuencial...");
        startTime = System.currentTimeMillis();
        C1 = multiplySequential(A, B);
        endTime = System.currentTimeMillis();
        seqTime = endTime - startTime;
        System.out.println("  - Tiempo versión secuencial: " + seqTime + " ms");
        
        // Ejecutar versión paralela con ForkJoin
        System.out.println("\nEjecutando versión paralela (ForkJoin)...");
        startTime = System.currentTimeMillis();
        C2 = multiplyParallel(A, B);
        endTime = System.currentTimeMillis();
        parTime = endTime - startTime;
        System.out.println("  - Tiempo versión paralela (ForkJoin): " + parTime + " ms");
        
        // Ejecutar versión paralela con hilos tradicionales
        System.out.println("\nEjecutando versión paralela (Hilos tradicionales)...");
        startTime = System.currentTimeMillis();
        C3 = multiplyParallelTraditional(A, B);
        endTime = System.currentTimeMillis();
        parTraditionalTime = endTime - startTime;
        System.out.println("  - Tiempo versión paralela (Hilos): " + parTraditionalTime + " ms");
        
        // Calcular y mostrar aceleraciones
        double speedupFJ = (double) seqTime / parTime;
        double speedupTrad = (double) seqTime / parTraditionalTime;
        
        System.out.println("\nResultados de rendimiento:");
        System.out.println("  - Aceleración (ForkJoin): " + String.format("%.2f", speedupFJ) + "x");
        System.out.println("  - Aceleración (Hilos): " + String.format("%.2f", speedupTrad) + "x");
        System.out.println("  - Mejora (ForkJoin): " + String.format("%.2f", (speedupFJ - 1) * 100) + "%");
        System.out.println("  - Mejora (Hilos): " + String.format("%.2f", (speedupTrad - 1) * 100) + "%");
        
        // Verificar resultados
        boolean equalFJ = true;
        boolean equalTrad = true;
        double maxDiffFJ = 0;
        double maxDiffTrad = 0;
        
        for (int i = 0; i < n && (equalFJ || equalTrad); i++) {
            for (int j = 0; j < n; j++) {
                double diffFJ = Math.abs(C1[i][j] - C2[i][j]);
                double diffTrad = Math.abs(C1[i][j] - C3[i][j]);
                
                maxDiffFJ = Math.max(maxDiffFJ, diffFJ);
                maxDiffTrad = Math.max(maxDiffTrad, diffTrad);
                
                if (diffFJ > 1e-9 && equalFJ) {
                    equalFJ = false;
                    System.out.println("Diferencia ForkJoin encontrada en [" + i + "][" + j + "]: " + 
                                      C1[i][j] + " vs " + C2[i][j]);
                }
                
                if (diffTrad > 1e-9 && equalTrad) {
                    equalTrad = false;
                    System.out.println("Diferencia Hilos encontrada en [" + i + "][" + j + "]: " + 
                                      C1[i][j] + " vs " + C3[i][j]);
                }
            }
        }
        
        System.out.println("\nVerificación de resultados:");
        System.out.println("  - Resultados ForkJoin iguales a secuencial: " + equalFJ);
        System.out.println("  - Resultados Hilos iguales a secuencial: " + equalTrad);
        System.out.println("  - Diferencia máxima ForkJoin: " + maxDiffFJ);
        System.out.println("  - Diferencia máxima Hilos: " + maxDiffTrad);
        
        // Información del sistema
        System.out.println("\nInformación del sistema:");
        System.out.println("  - Procesadores disponibles: " + Runtime.getRuntime().availableProcessors());
        System.out.println("  - Memoria máxima: " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        
        // Preguntar si desea imprimir la matriz resultante
        System.out.print("\n¿Quieres imprimir la matriz resultante? (S/N): ");
        String printResult = scanner.next();
        
        if (printResult.equalsIgnoreCase("S")) {
            System.out.println("\nMatriz Resultante (de versión secuencial):");
            printMatrix(C1);
        }
        
        scanner.close();
    }
}
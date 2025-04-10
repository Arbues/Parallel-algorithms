import java.util.Random;

public class ParallelPSO {

    // Parámetros del PSO
    public static final int NUM_PARTICLES = 10000;   // cantidad de partículas
    public static final int DIMENSION = 30;           // dimensión del problema
    public static final int MAX_ITERATIONS = 1000;    // número máximo de iteraciones
    public static final double W = 0.7298;            // peso de inercia
    public static final double C1 = 1.49618;          // coeficiente cognitivo
    public static final double C2 = 1.49618;          // coeficiente social
    public static final double MIN_POS = -100;
    public static final double MAX_POS = 100;
    public static final double MIN_VEL = -1;
    public static final double MAX_VEL = 1;

    // Función objetivo: Sphere function (mínimo en 0)
    public static double objective(double[] position) {
        double sum = 0;
        for (double x : position) {
            sum += x * x;
        }
        return sum;
    }

    // Clase que representa una partícula del swarm
    public static class Particle {
        double[] position;
        double[] velocity;
        double[] pBest;       // Mejor posición personal
        double pBestFitness;  // Fitness correspondiente a pBest

        public Particle() {
            position = new double[DIMENSION];
            velocity = new double[DIMENSION];
            pBest = new double[DIMENSION];
        }
    }

    // Clase para almacenar y actualizar de forma segura el global best (solución global)
    public static class GlobalBest {
        double bestFitness;
        double[] bestPosition;

        public GlobalBest() {
            bestFitness = Double.MAX_VALUE;
            bestPosition = new double[DIMENSION];
        }
        
        // Método sincronizado para actualizar si se encuentra una solución mejor
        public synchronized void updateIfBetter(Particle p, double fitness) {
            if (fitness < bestFitness) {
                bestFitness = fitness;
                System.arraycopy(p.position, 0, bestPosition, 0, DIMENSION);
            }
        }
    }

    // Clase para encapsular el resultado del PSO
    public static class PSOResult {
        double bestFitness;
        double[] bestPosition;

        public PSOResult(double fitness, double[] position) {
            this.bestFitness = fitness;
            this.bestPosition = position;
        }
    }

    // --------------------------------------------------------------
    // Versión secuencial del PSO
    public static PSOResult psoSerial(Particle[] swarm) {
        GlobalBest gBest = new GlobalBest();
        Random rnd = new Random();

        // Inicialización: cada partícula tiene como pBest su posición inicial
        for (Particle p : swarm) {
            double fitness = objective(p.position);
            p.pBestFitness = fitness;
            System.arraycopy(p.position, 0, p.pBest, 0, DIMENSION);
            if (fitness < gBest.bestFitness) {
                gBest.bestFitness = fitness;
                System.arraycopy(p.position, 0, gBest.bestPosition, 0, DIMENSION);
            }
        }

        // Iterar las actualizaciones (PSO)
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            // Para cada partícula se actualizan velocity y position, y se evalúa la aptitud
            for (Particle p : swarm) {
                for (int d = 0; d < DIMENSION; d++) {
                    double r1 = rnd.nextDouble();
                    double r2 = rnd.nextDouble();
                    // Actualizar la velocidad según la fórmula PSO
                    p.velocity[d] = W * p.velocity[d] +
                                    C1 * r1 * (p.pBest[d] - p.position[d]) +
                                    C2 * r2 * (gBest.bestPosition[d] - p.position[d]);
                    // Limitar la velocidad
                    if (p.velocity[d] < MIN_VEL) p.velocity[d] = MIN_VEL;
                    if (p.velocity[d] > MAX_VEL) p.velocity[d] = MAX_VEL;
                    // Actualizar la posición
                    p.position[d] += p.velocity[d];
                    // Limitar la posición
                    if (p.position[d] < MIN_POS) p.position[d] = MIN_POS;
                    if (p.position[d] > MAX_POS) p.position[d] = MAX_POS;
                }
                // Evaluar la nueva aptitud
                double fitness = objective(p.position);
                // Actualizar el pBest personal si la nueva solución es mejor
                if (fitness < p.pBestFitness) {
                    p.pBestFitness = fitness;
                    System.arraycopy(p.position, 0, p.pBest, 0, DIMENSION);
                }
                // Actualizar la solución global (no requiere sincronización en versión secuencial)
                if (fitness < gBest.bestFitness) {
                    gBest.bestFitness = fitness;
                    System.arraycopy(p.position, 0, gBest.bestPosition, 0, DIMENSION);
                }
            }
        }
        return new PSOResult(gBest.bestFitness, gBest.bestPosition);
    }

    // --------------------------------------------------------------
    // Versión paralela del PSO usando hilos
    public static PSOResult psoParallel(Particle[] swarm) {
        GlobalBest gBest = new GlobalBest();
        Random rnd = new Random();

        // Inicialización similar: cada partícula comienza con su posición inicial como pBest
        for (Particle p : swarm) {
            double fitness = objective(p.position);
            p.pBestFitness = fitness;
            System.arraycopy(p.position, 0, p.pBest, 0, DIMENSION);
            if (fitness < gBest.bestFitness) {
                gBest.bestFitness = fitness;
                System.arraycopy(p.position, 0, gBest.bestPosition, 0, DIMENSION);
            }
        }

        // Definir el número de hilos a utilizar (por ejemplo, los núcleos disponibles)
        int numThreads = Runtime.getRuntime().availableProcessors();
        int blockSize = (swarm.length + numThreads - 1) / numThreads;

        // Para cada iteración se lanzan hilos que actualizan un bloque del swarm
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Thread[] threads = new Thread[numThreads];
            for (int t = 0; t < numThreads; t++) {
                final int startIndex = t * blockSize;
                final int endIndex = Math.min(swarm.length, startIndex + blockSize);
                threads[t] = new Thread(() -> {
                    // Cada hilo usa su propia instancia de Random
                    Random threadRnd = new Random();
                    for (int i = startIndex; i < endIndex; i++) {
                        Particle p = swarm[i];
                        for (int d = 0; d < DIMENSION; d++) {
                            double r1 = threadRnd.nextDouble();
                            double r2 = threadRnd.nextDouble();
                            p.velocity[d] = W * p.velocity[d] +
                                            C1 * r1 * (p.pBest[d] - p.position[d]) +
                                            C2 * r2 * (gBest.bestPosition[d] - p.position[d]);
                            if (p.velocity[d] < MIN_VEL) p.velocity[d] = MIN_VEL;
                            if (p.velocity[d] > MAX_VEL) p.velocity[d] = MAX_VEL;
                            p.position[d] += p.velocity[d];
                            if (p.position[d] < MIN_POS) p.position[d] = MIN_POS;
                            if (p.position[d] > MAX_POS) p.position[d] = MAX_POS;
                        }
                        double fitness = objective(p.position);
                        if (fitness < p.pBestFitness) {
                            p.pBestFitness = fitness;
                            System.arraycopy(p.position, 0, p.pBest, 0, DIMENSION);
                        }
                        // Actualización del global best de forma sincronizada
                        gBest.updateIfBetter(p, fitness);
                    }
                });
                threads[t].start();
            }
            // Esperar a que todos los hilos terminen la iteración
            for (int t = 0; t < numThreads; t++) {
                try {
                    threads[t].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return new PSOResult(gBest.bestFitness, gBest.bestPosition);
    }

    // --------------------------------------------------------------
    // Métodos auxiliares
    // Genera un swarm (arreglo de partículas) con condiciones iniciales aleatorias
    public static Particle[] generarSwarm(int numParticles) {
        Particle[] swarm = new Particle[numParticles];
        Random rnd = new Random();
        for (int i = 0; i < numParticles; i++) {
            Particle p = new Particle();
            for (int d = 0; d < DIMENSION; d++) {
                p.position[d] = MIN_POS + (MAX_POS - MIN_POS) * rnd.nextDouble();
                p.velocity[d] = MIN_VEL + (MAX_VEL - MIN_VEL) * rnd.nextDouble();
                p.pBest[d] = p.position[d];
            }
            p.pBestFitness = objective(p.position);
            swarm[i] = p;
        }
        return swarm;
    }

    // Método para clonar el swarm (para que ambas versiones tengan las mismas condiciones iniciales)
    public static Particle[] clonarSwarm(Particle[] swarm) {
        Particle[] clone = new Particle[swarm.length];
        for (int i = 0; i < swarm.length; i++) {
            Particle p = new Particle();
            System.arraycopy(swarm[i].position, 0, p.position, 0, DIMENSION);
            System.arraycopy(swarm[i].velocity, 0, p.velocity, 0, DIMENSION);
            System.arraycopy(swarm[i].pBest, 0, p.pBest, 0, DIMENSION);
            p.pBestFitness = swarm[i].pBestFitness;
            clone[i] = p;
        }
        return clone;
    }

    // Compara dos resultados del PSO (global best), verificando que la diferencia en fitness y posiciones
    // sea menor a la tolerancia indicada.
    public static boolean compararResultados(PSOResult r1, PSOResult r2, double tol) {
        if (Math.abs(r1.bestFitness - r2.bestFitness) > tol) return false;
        for (int d = 0; d < DIMENSION; d++) {
            if (Math.abs(r1.bestPosition[d] - r2.bestPosition[d]) > tol) {
                return false;
            }
        }
        return true;
    }

    // --------------------------------------------------------------
    // Método main: ejecuta ambas versiones (serial y paralela), mide su tiempo y compara los resultados
    public static void main(String[] args) {
        // Generar el swarm inicial aleatorio
        Particle[] swarmInicial = generarSwarm(NUM_PARTICLES);
        // Clonar para que la versión serial y la paralela tengan las mismas condiciones iniciales
        Particle[] swarmSerial = clonarSwarm(swarmInicial);
        Particle[] swarmParalelo = clonarSwarm(swarmInicial);

        System.out.println("Ejecutando PSO secuencial...");
        long inicioSerial = System.nanoTime();
        PSOResult resultadoSerial = psoSerial(swarmSerial);
        long finSerial = System.nanoTime();
        long tiempoSerialMs = (finSerial - inicioSerial) / 1000000;
        System.out.println("Tiempo de ejecución secuencial (ms): " + tiempoSerialMs);
        System.out.println("Mejor fitness (serial): " + resultadoSerial.bestFitness);

        System.out.println("\nEjecutando PSO en paralelo con hilos...");
        long inicioParalelo = System.nanoTime();
        PSOResult resultadoParalelo = psoParallel(swarmParalelo);
        long finParalelo = System.nanoTime();
        long tiempoParaleloMs = (finParalelo - inicioParalelo) / 1000000;
        System.out.println("Tiempo de ejecución paralelo (ms): " + tiempoParaleloMs);
        System.out.println("Mejor fitness (paralelo): " + resultadoParalelo.bestFitness);

        double tolerancia = 1e-6;
        boolean iguales = compararResultados(resultadoSerial, resultadoParalelo, tolerancia);
        System.out.println("\n¿Los resultados de ambas versiones son iguales (dentro de tolerancia " + tolerancia + ")? " + iguales);

        if (tiempoParaleloMs < tiempoSerialMs) {
            System.out.println("La versión paralela fue más rápida.");
        } else {
            System.out.println("La versión paralela NO fue más rápida que la secuencial.");
        }
    }
}

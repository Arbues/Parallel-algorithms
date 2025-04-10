# Conceptos Fundamentales de Ejecución de Programas

## Concurrencia
- Las tareas **progresan** simultáneamente
- Puede ocurrir en un solo núcleo
- El CPU alterna rápidamente entre tareas
- Ejemplo: Un chef cocinando varios platos, alternando entre ellos

### Características
- No requiere múltiples núcleos
- Útil para tareas con tiempos de espera (E/S, red, etc.)
- El orden de ejecución no está garantizado

## Paralelismo
- Las tareas se ejecutan **literalmente** al mismo tiempo
- Requiere múltiples núcleos en la misma máquina
- Comparten memoria y recursos del sistema

### Características
- Todos los núcleos están en la misma máquina física
- Comunicación rápida entre tareas
- Acceso a memoria compartida
- Ideal para tareas computacionalmente intensivas

## Sistemas Distribuidos
- Las tareas se ejecutan en diferentes máquinas físicas
- Cada máquina tiene sus propios recursos
- Se comunican a través de la red

### Características
- Cada máquina tiene su propia memoria y recursos
- Comunicación más lenta (a través de red)
- Mayor escalabilidad
- Tolerancia a fallos
- Ideal para sistemas que requieren alta disponibilidad

## Comparación Rápida

| Aspecto | Concurrencia | Paralelismo | Distribuido |
|---------|-------------|-------------|-------------|
| Ejecución | Alternada | Simultánea | Simultánea |
| Hardware | 1+ núcleos | 2+ núcleos | 2+ máquinas |
| Memoria | Compartida | Compartida | Separada |
| Comunicación | Muy rápida | Muy rápida | A través de red |
| Escalabilidad | Limitada | Limitada a núcleos | Alta |

## Fundamentos de Programación Concurrente en Java

Esta sección presenta los conceptos fundamentales de concurrencia en Java, con ejemplos extraídos del código proporcionado.

### 1. Creación de Hilos en Java

Existen dos formas principales para crear hilos en Java:

#### a) Implementando la Interfaz `Runnable`

```java
// Ejemplo básico de un Runnable
public class HelloRunnable implements Runnable {
    public void run() {
        System.out.println("Hello from a thread01!");
    }
    
    public static void main(String args[]) {
        (new Thread(new HelloRunnable())).start();
    }
}
```

Este enfoque es más flexible porque:
- Permite que la clase extienda otra clase
- Separa la tarea (lo que hace el hilo) del mecanismo del hilo
- Favorece la reutilización de código

#### b) Extendiendo la Clase `Thread`

```java
// Ejemplo básico extendiendo Thread
public class HelloThread extends Thread {
    public void run() {
        System.out.println("Hello from thread!");
    }
    
    public static void main(String args[]) {
        (new HelloThread()).start();
    }
}
```

Este enfoque es más directo pero menos flexible porque:
- La clase ya no puede extender otra clase
- Mezcla la tarea con el mecanismo del hilo

### 2. Ciclo de Vida de un Hilo

```java
// Creación e inicio de un hilo
Thread t = new Thread(new Runnable() { ... });
t.start();  // Cambia el estado de NEW a RUNNABLE

// Esperar a que un hilo termine
t.join();  // El hilo actual espera hasta que t termine
```

### 3. Manejo del Hilo Actual

```java
// Obtener referencia al hilo actual
Thread currThread = Thread.currentThread();
```java
// Pausar un hilo por un tiempo determinado
try {
    Thread.sleep(1000);  // Pausa por 1 segundo
} catch (InterruptedException e) {
    // Manejo de la interrupción
    // Aquí una interrupción se puede dar porque otro hilo
    // llamó al método interrupt() sobre este hilo.
    System.out.println("El hilo fue interrumpido mientras dormía.");
}
```

### 4. Ejecución Concurrente con Múltiples Hilos

```java
// Crear un conjunto de hilos para dividir trabajo
Thread[] hilos = new Thread[NUM_HILOS];
for (int i = 0; i < NUM_HILOS; i++) {
    // Crear e iniciar cada hilo
    hilos[i] = new Thread(new TareaEspecifica(i));
    hilos[i].start();
}

// Esperar a que todos los hilos terminen
for (int i = 0; i < NUM_HILOS; i++) {
    try {
        hilos[i].join();
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
```

### 5. Clases Anidadas para Tareas Concurrentes

```java
public class ejemploSuma {
    // Arreglo compartido para almacenar resultados parciales
    static int[] sumahilos = new int[4];
    
    // Clase interna que implementa la tarea concurrente
    class tarea implements Runnable {
        private int id;
        
        public tarea(int id) {
            this.id = id;
        }
        
        @Override
        public void run() {
            // Código que ejecutará cada hilo
            // Almacena resultado en sumahilos[id]
        }
    }
    
    public static void inicio() {
        // Crear e iniciar los hilos usando la clase interna
    }
}
```

Beneficios de usar clases internas:
- Acceso directo a los miembros de la clase externa
- Mayor encapsulación y organización del código
- La clase interna está estrechamente relacionada con su contexto

### 6. Patrones Comunes

#### Patrón de División de Trabajo

```java
// División del trabajo entre múltiples hilos
int totalElementos = 1000;
int elementosPorHilo = totalElementos / NUM_HILOS;

for (int i = 0; i < NUM_HILOS; i++) {
    int inicio = i * elementosPorHilo;
    int fin = (i == NUM_HILOS-1) ? totalElementos : (i+1) * elementosPorHilo;
    
    hilos[i] = new Thread(new Trabajador(inicio, fin));
    hilos[i].start();
}
```

#### Sincronización de Resultados

```java
// Sumar resultados después de que todos los hilos terminen
int resultadoFinal = 0;
for (int i = 0; i < NUM_HILOS; i++) {
    hilos[i].join();  // Esperar que termine el hilo i
    resultadoFinal += resultadosParciales[i];
}
```

### Progresión de Complejidad

A medida que avances en los ejemplos, notarás:
1. Primero trabajamos con hilos simples y directos
2. Luego aplicamos hilos para dividir tareas computacionales
3. Finalmente implementamos patrones más sofisticados con sincronización

Esta progresión te ayudará a entender desde lo básico hasta las implementaciones más complejas de concurrencia en Java.


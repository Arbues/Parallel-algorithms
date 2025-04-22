# Instrucciones para compilar y ejecutar en WSL2

## Estructura de archivos
He dividido el código original en cuatro clases independientes:

1. `ParallelDijkstra.java` - Clase principal con el método `main()`
2. `GraphUtils.java` - Utilidades para generar grafos aleatorios
3. `DijkstraAlgorithm.java` - Implementación de los algoritmos de Dijkstra (secuencial y paralelo)
4. `MatrixUtils.java` - Utilidades para operaciones con matrices

## Pasos para compilar y ejecutar en WSL2

1. Crea un directorio para el proyecto:
```bash
mkdir dijkstra_project
cd dijkstra_project
```

2. Crea cada uno de los archivos Java usando un editor de texto como nano, vim o cualquier otro:
```bash
nano ParallelDijkstra.java
nano GraphUtils.java
nano DijkstraAlgorithm.java
nano MatrixUtils.java
```

3. Copia y pega el código correspondiente en cada archivo.

4. Compila todos los archivos Java:
```bash
javac *.java
```

5. Ejecuta la clase principal (ParallelDijkstra):
```bash
java ParallelDijkstra
```

## Requisitos
- Java Development Kit (JDK) instalado en tu WSL2.
- Si no tienes Java instalado, puedes instalarlo con:
```bash
sudo apt update
sudo apt install default-jdk
```

## Verificación de la instalación de Java
Puedes verificar que Java está correctamente instalado ejecutando:
```bash
java -version
javac -version
```

## Notas adicionales
- El valor `NUM_NODOS = 4000` puede ser ajustado en la clase `ParallelDijkstra.java` si deseas realizar pruebas con un número diferente de nodos.
- La ejecución puede tomar tiempo dependiendo del número de nodos y la capacidad de tu sistema.
- El algoritmo paralelo utilizará automáticamente el número de núcleos disponibles en tu sistema para optimizar el rendimiento.
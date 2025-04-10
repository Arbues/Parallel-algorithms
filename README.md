# Documentation for Parallel Algorithms Project

### conceptos_concurrencia.md
This markdown file provides an overview of concurrency, parallelism, and distributed systems. Key topics include:
- **Definitions**: Explains concurrency, parallelism, and distributed systems with examples.
- **Comparison Table**: Highlights differences in execution, hardware, memory, and scalability.
- **Java Concurrency**: Covers thread creation, lifecycle, and common patterns in Java.
- **Synchronization**: Discusses techniques for managing shared resources and results.

### ParallelPSO.java
This file implements the Particle Swarm Optimization (PSO) algorithm in both sequential and parallel versions. Key features include:
- **Objective Function**: Sphere function for optimization.
- **Particle Class**: Represents individual particles in the swarm.
- **GlobalBest Class**: Manages the global best solution with thread-safe updates.
- **Sequential PSO**: Implements the PSO algorithm in a single-threaded manner.
- **Parallel PSO**: Uses multithreading to divide the workload among available processors.
- **Utility Methods**: Includes methods for swarm initialization, cloning, and result comparison.
- **Main Method**: Compares the performance and results of sequential and parallel implementations.

### ParallelDijkstra.java
This file implements the Dijkstra algorithm for finding shortest paths in a graph, with both sequential and parallel versions. Key features include:
- **Graph Representation**: Uses an adjacency matrix.
- **Sequential Dijkstra**: Computes shortest paths for all nodes in a single-threaded manner.
- **Parallel Dijkstra**: Divides the computation across multiple threads for efficiency.
- **Utility Methods**: Includes graph generation and result comparison.
- **Main Method**: Measures and compares the performance of sequential and parallel implementations.

### CoppersmithWinogradMatrixMultiplication.java
This file implements the Coppersmith-Winograd algorithm for matrix multiplication, with sequential and parallel versions. Key features include:
- **Sequential Implementation**: Uses recursive matrix partitioning for efficient multiplication.
- **Parallel Implementation**: Includes ForkJoin and traditional threading approaches.
- **Matrix Operations**: Provides utility methods for addition, subtraction, and submatrix extraction.
- **Performance Comparison**: Measures execution time and verifies the correctness of results.
- **Main Method**: Demonstrates the algorithm with user-defined matrix sizes.

### LUParallelSolver.java
This file implements LU decomposition with both serial and parallel approaches. Key features include:
- **Serial LU Decomposition**: Factorizes a matrix into lower and upper triangular matrices.
- **Parallel LU Decomposition**: Uses a thread pool to parallelize the computation.
- **Forward and Backward Substitution**: Solves the system of equations after decomposition.
- **Utility Methods**: Includes matrix generation, copying, and result comparison.
- **Main Method**: Compares the performance and accuracy of serial and parallel implementations.


## README.md
This file is currently empty but is intended to provide an overview of the project, its purpose, and instructions for usage.
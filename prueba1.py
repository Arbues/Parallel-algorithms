import numpy as np
import threading
import time

# ===========================
# Función para crear un sistema Ax = b
# ===========================
def crear_sistema(L):
    np.random.seed(0)
    A = np.random.rand(L, L) * 10
    b = np.random.rand(L)
    return A, b

# ===========================
# Descomposición LU Serial
# ===========================
def lu_serial(A):
    L = np.zeros_like(A)
    U = np.zeros_like(A)
    n = A.shape[0]
    
    for i in range(n):
        L[i][i] = 1
        for j in range(i, n):
            U[i][j] = A[i][j] - sum(L[i][k] * U[k][j] for k in range(i))
        for j in range(i+1, n):
            L[j][i] = (A[j][i] - sum(L[j][k] * U[k][i] for k in range(i))) / U[i][i]
    
    return L, U

# ===========================
# Descomposición LU Paralela
# ===========================
def lu_paralela(A, h):
    n = A.shape[0]
    L = np.zeros_like(A)
    U = np.zeros_like(A)

    def procesar_columna(i):
        L[i][i] = 1
        for j in range(i, n):
            U[i][j] = A[i][j] - sum(L[i][k] * U[k][j] for k in range(i))
        for j in range(i+1, n):
            L[j][i] = (A[j][i] - sum(L[j][k] * U[k][i] for k in range(i))) / U[i][i]

    for i in range(n):
        threads = []
        for t in range(h):
            hilo = threading.Thread(target=procesar_columna, args=(i,))
            threads.append(hilo)
            hilo.start()
        for hilo in threads:
            hilo.join()
    
    return L, U

# ===========================
# Sustitución hacia adelante y atrás
# ===========================
def resolver_LU(L, U, b):
    n = len(b)
    y = np.zeros_like(b)
    x = np.zeros_like(b)

    # Sustitución hacia adelante: Ly = b
    for i in range(n):
        y[i] = b[i] - np.dot(L[i,:i], y[:i])
    
    # Sustitución hacia atrás: Ux = y
    for i in range(n-1, -1, -1):
        x[i] = (y[i] - np.dot(U[i,i+1:], x[i+1:])) / U[i,i]
    
    return x

# ===========================
# Prueba y comparación
# ===========================
def prueba(L_dim=200, h=4):
    A, b = crear_sistema(L_dim)

    # ====== Serial ======
    A_serial = A.copy()
    t0 = time.time()
    Ls, Us = lu_serial(A_serial)
    xs = resolver_LU(Ls, Us, b)
    t1 = time.time()

    # ====== Paralelo ======
    A_paralelo = A.copy()
    t2 = time.time()
    Lp, Up = lu_paralela(A_paralelo, h)
    xp = resolver_LU(Lp, Up, b)
    t3 = time.time()

    print("Tiempo Serial:   {:.5f} segundos".format(t1 - t0))
    print("Tiempo Paralelo: {:.5f} segundos".format(t3 - t2))
    print("¿Soluciones similares?:", np.allclose(xs, xp, atol=1e-6))

# Ejecutamos la prueba
prueba(L_dim=1000, h=4)

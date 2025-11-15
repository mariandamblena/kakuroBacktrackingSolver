import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de prueba para ejecutar múltiples tableros de Kakuro.
 * 
 * Esta clase permite probar el solver con diferentes casos:
 * - Tablero simple (2x2, 4 celdas)
 * - Tablero medio (3x4, ~6 celdas)
 * - Tablero difícil (5x5, ~15 celdas)
 * - Tablero imposible (sin solución válida)
 * - Tablero original del TPO (7x7)
 * 
 * Para cada caso se reporta:
 * - Tiempo de ejecución
 * - Cantidad de llamadas recursivas
 * - Si encontró solución o no
 */
public class TestKakuro {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     TEST COMPLETO - SOLVER DE KAKURO CON BACKTRACKING     ║");
        System.out.println("║              Programación III - TPO Tema 2                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        // Array con todos los casos de prueba
        String[] archivos = {
            "tablerostest/kakuro_simple.txt",
            "tablerostest/kakuro_medio.txt",
            "tablerostest/kakuro.txt",
            "tablerostest/kakuro_dificil.txt",
            "tablerostest/kakuro_imposible.txt",
            "tablerostest/kakuro_8x8.txt",
            "tablerostest/kakuro_example.txt"
        };
        
        String[] descripciones = {
            "TABLERO SIMPLE (2x2, 4 celdas)",
            "TABLERO MEDIO (3x4, ~6 celdas)",
            "TABLERO ORIGINAL TPO (7x7, ~17 celdas)",
            "TABLERO DIFÍCIL (5x5, ~15 celdas)",
            "TABLERO IMPOSIBLE (sin solución válida)",
            "TABLERO 8x8 (8x8, 16 celdas)",
            "TABLERO EXAMPLE (7x7, imagen ejemplo)"
        };
        
        // Mostrar menú de opciones
        System.out.println("Seleccione una opción:");
        System.out.println("  0. Ejecutar TODOS los tests");
        for (int i = 0; i < archivos.length; i++) {
            System.out.println("  " + (i + 1) + ". " + descripciones[i]);
        }
        System.out.print("\nIngrese su opción: ");
        
        Scanner scanner = new Scanner(System.in);
        int opcion = -1;
        
        try {
            opcion = scanner.nextInt();
        } catch (Exception e) {
            System.out.println("Opción inválida. Ejecutando todos los tests.");
            opcion = 0;
        }
        
        System.out.println();
        
        // Determinar qué tests ejecutar
        List<Integer> testsAEjecutar = new ArrayList<>();
        if (opcion == 0 || opcion < 0 || opcion > archivos.length) {
            // Ejecutar todos
            for (int i = 0; i < archivos.length; i++) {
                testsAEjecutar.add(i);
            }
        } else {
            // Ejecutar uno específico
            testsAEjecutar.add(opcion - 1);
        }
        
        int totalPruebas = 0;
        int exitosas = 0;
        int fallidas = 0;
        long tiempoTotal = 0;
        long llamadasTotales = 0;
        
        // Ejecutar cada prueba
        for (int idx : testsAEjecutar) {
            totalPruebas++;
            System.out.println("┌────────────────────────────────────────────────────────────┐");
            System.out.println("│ TEST #" + (idx + 1) + ": " + descripciones[idx]);
            System.out.println("│ Archivo: " + archivos[idx]);
            System.out.println("└────────────────────────────────────────────────────────────┘");
            
            ResultadoTest resultado = ejecutarTest(archivos[idx]);
            
            if (resultado != null) {
                tiempoTotal += resultado.tiempo;
                llamadasTotales += resultado.llamadas;
                
                System.out.println("\nRESULTADOS:");
                System.out.println("   Tiempo: " + String.format("%.3f", resultado.tiempo / 1e6) + " ms");
                System.out.println("   Llamadas recursivas: " + resultado.llamadas);
                System.out.println("   Estado: " + (resultado.exito ? "SOLUCION ENCONTRADA" : "SIN SOLUCION"));
                
                if (resultado.exito) {
                    exitosas++;
                    System.out.println("\n   Solucion:");
                    resultado.tablero.imprimir();
                } else {
                    fallidas++;
                }
            } else {
                System.out.println("   ERROR: No se pudo cargar el tablero");
                fallidas++;
            }
            
            System.out.println("\n════════════════════════════════════════════════════════════\n");
        }
        
        // Reporte final
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RESUMEN FINAL                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("  Total de pruebas ejecutadas: " + totalPruebas);
        System.out.println("  Soluciones encontradas: " + exitosas);
        System.out.println("  Sin solucion / Error: " + fallidas);
        System.out.println("  Tiempo total: " + String.format("%.3f", tiempoTotal / 1e6) + " ms");
        System.out.println("  Total llamadas recursivas: " + llamadasTotales);
        System.out.println("  Complejidad observada: O(9^n) - Exponencial");
        System.out.println("  Clase de problema: NP-Completo");
        System.out.println("════════════════════════════════════════════════════════════\n");
        
        scanner.close();
    }
    
    /**
     * Ejecuta un test individual sobre un archivo de tablero.
     * 
     * @param archivo Ruta del archivo con el tablero
     * @return Resultado del test con métricas, o null si hubo error
     */
    private static ResultadoTest ejecutarTest(String archivo) {
        try {
            // Cargar tablero
            Tablero tablero = Tablero.leerDesdeArchivo(archivo);
            if (tablero == null) {
                return null;
            }
            
            int numCeldas = tablero.getCeldasBlancas().size();
            System.out.println("   Celdas blancas a completar: " + numCeldas);
            System.out.println("   Complejidad teórica: O(9^" + numCeldas + ")");
            System.out.println("\n   Ejecutando backtracking...");
            
            // Resolver
            KakuroSolver solver = new KakuroSolver(tablero);
            long inicio = System.nanoTime();
            boolean exito = solver.resolver();
            long fin = System.nanoTime();
            
            return new ResultadoTest(
                tablero,
                exito,
                fin - inicio,
                solver.getContadorLlamadas()
            );
            
        } catch (Exception e) {
            System.out.println("   ✗ EXCEPCIÓN: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Clase auxiliar para almacenar resultados de un test.
     */
    private static class ResultadoTest {
        Tablero tablero;
        boolean exito;
        long tiempo; // nanosegundos
        int llamadas;
        
        ResultadoTest(Tablero tablero, boolean exito, long tiempo, int llamadas) {
            this.tablero = tablero;
            this.exito = exito;
            this.tiempo = tiempo;
            this.llamadas = llamadas;
        }
    }
}

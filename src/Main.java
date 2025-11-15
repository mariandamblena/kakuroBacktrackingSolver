/** 
 * El programa lee un tablero desde un archivo, aplica el algoritmo de backtracking
 * para encontrar una solución válida, y muestra estadísticas de ejecución.
 */
public class Main {
    public static void main(String[] args) {
        // Archivo de entrada con el tablero inicial
        String archivoEntrada = "tablerostest/kakuro_example.txt";

        // 1. Cargo tablero desde txt
        System.out.println("Cargando tablero desde: " + archivoEntrada);
        Tablero tablero = Tablero.leerDesdeArchivo(archivoEntrada);
        
        if (tablero == null) {
            System.err.println("ERROR: No se pudo construir el tablero desde '" + archivoEntrada + "'.");
            return;
        }
        
        System.out.println("Tablero cargado, Celdas blancas a completar:"+ tablero.getCeldasBlancas().size() + "\n");

        // 2. una vez cargado, resolver con backtracking
        KakuroSolver solver = new KakuroSolver(tablero);
        
        System.out.println("Iniciando resolución con Backtracking...\n");
        
        // Medir tiempo de ejecución
        long inicio = System.nanoTime();
        boolean exito = solver.resolver();
        long fin = System.nanoTime();
        
        // 3. MOSTRAR RESULTADOS
        System.out.println("=================================================");
        System.out.println("               RESULTADOS                        ");
        System.out.println("=================================================");
        System.out.println("Tiempo de ejecución: " + (fin - inicio) / 1e6 + " ms");
        System.out.println("Llamadas recursivas: " + solver.getContadorLlamadas());
        System.out.println();

        if (exito) {
            System.out.println("✓ SOLUCIÓN ENCONTRADA:\n");
            tablero.imprimir();
        } else {
            System.out.println("✗ No se encontró solución para este Kakuro.");
        }
        
        System.out.println("\n=================================================");
    }
}
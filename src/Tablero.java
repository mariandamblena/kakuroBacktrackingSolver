// Archivo: Tablero.java
import java.io.*;
import java.util.*;

/**
 * Representa el tablero de Kakuro y gestiona la carga desde archivo.
 * 
 * El tablero almacena:
 * - Matriz de celdas (blancas para completar, negras para claves/pistas)
 * - Runs horizontales y verticales (grupos de celdas que deben sumar un objetivo)
 * - Mapa para acceso rápido a los runs de cada celda
 * 
 * Formato del archivo de entrada (kakuro.txt):
 * - "X" = celda negra (no se completa)
 * - "." o "0" = celda blanca (vacía, se debe completar con 1-9)
 * - "n/m" = celda con claves: n=suma vertical (hacia abajo), m=suma horizontal (derecha)
 * 
 * Ejemplo:
 * X X 16/0 24/0
 * X 0/17 . .
 * 0/15 . . X
 */
public class Tablero {
    private Celda[][] matriz;
    private String[][] tokens;  // Almacena los tokens originales del archivo
    private List<Run> runsHorizontales = new ArrayList<>();
    private List<Run> runsVerticales = new ArrayList<>();
    private List<Celda> celdasBlancas = new ArrayList<>();
    private Map<String, Run> mapaRuns = new HashMap<>();

    /**
     * Lee y construye un tablero de Kakuro desde un archivo de texto.
     * 
     * Proceso:
     * 1. Parsea cada línea del archivo separando tokens por espacios
     * 2. Crea celdas blancas para los "."
     * 3. Identifica celdas con claves (formato "n/m")
     * 4. Construye runs horizontales (hacia la derecha) y verticales (hacia abajo)
     * 5. Valida que cada celda blanca pertenezca exactamente a 1 run H y 1 run V
     * 
     * @param archivo Ruta del archivo con el tablero (ej: "src/kakuro.txt")
     * @return Tablero construido, o null si hay errores de formato/parsing
     */
    public static Tablero leerDesdeArchivo(String archivo) {
        try {
            // PASO 1: Leer todas las líneas del archivo
            List<String[]> lineas = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(archivo));
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea.trim().split("\\s+"));
                }
            }
            br.close();

            // PASO 2: Inicializar matriz y crear celdas blancas
            int filas = lineas.size();
            int columnas = lineas.get(0).length;
            Tablero t = new Tablero();
            t.matriz = new Celda[filas][columnas];
            t.tokens = new String[filas][columnas];  // Almacenar tokens originales

            // Crear solo las celdas blancas (las que se deben completar)
            for (int i = 0; i < filas; i++) {
                for (int j = 0; j < columnas; j++) {
                    String token = lineas.get(i)[j];
                    t.tokens[i][j] = token;  // Guardar token original
                    if (token.equals(".") || token.equals("0")) {  // Reconocer tanto . como 0
                        Celda celda = new Celda(i, j);
                        t.matriz[i][j] = celda;
                        t.celdasBlancas.add(celda);
                    } else {
                        // Celda negra (X) o con claves (n/m)
                        t.matriz[i][j] = null;
                    }
                }
            }

            // PASO 3: Crear runs desde celdas con claves (formato "sumaVertical/sumaHorizontal")
            for (int i = 0; i < filas; i++) {
                for (int j = 0; j < columnas; j++) {
                    String token = lineas.get(i)[j];
                    if (token.contains("/")) {
                        String[] partes = token.split("/");
                        // Convertir "-" a "0" antes de parsear
                        int sumaV = partes[0].equals("-") ? 0 : Integer.parseInt(partes[0]);
                        int sumaH = partes[1].equals("-") ? 0 : Integer.parseInt(partes[1]);

                        // RUN HORIZONTAL: recolectar celdas hacia la DERECHA
                        if (sumaH > 0) {
                            List<Celda> celdas = new ArrayList<>();
                            int col = j + 1;
                            while (col < columnas && (lineas.get(i)[col].equals(".") || lineas.get(i)[col].equals("0"))) {
                                Celda celda = t.matriz[i][col];
                                if (celda != null) celdas.add(celda);
                                col++;
                            }
                            if (!celdas.isEmpty()) {
                                Run run = new Run(sumaH, celdas);
                                t.runsHorizontales.add(run);
                                // Mapear cada celda a su run horizontal
                                for (Celda c : celdas) {
                                    t.mapaRuns.put("H" + c.fila + "," + c.col, run);
                                }
                            }
                        }

                        // RUN VERTICAL: recolectar celdas hacia ABAJO
                        if (sumaV > 0) {
                            List<Celda> celdas = new ArrayList<>();
                            int fil = i + 1;
                            while (fil < filas && (lineas.get(fil)[j].equals(".") || lineas.get(fil)[j].equals("0"))) {
                                Celda celda = t.matriz[fil][j];
                                if (celda != null) celdas.add(celda);
                                fil++;
                            }
                            if (!celdas.isEmpty()) {
                                Run run = new Run(sumaV, celdas);
                                t.runsVerticales.add(run);
                                // Mapear cada celda a su run vertical
                                for (Celda c : celdas) {
                                    t.mapaRuns.put("V" + c.fila + "," + c.col, run);
                                }
                            }
                        }
                    }
                }
            }

            // PASO 4: Validar integridad del tablero
            // Cada celda blanca DEBE pertenecer exactamente a 1 run H y 1 run V
            for (Celda c : t.celdasBlancas) {
                String keyH = "H" + c.fila + "," + c.col;
                String keyV = "V" + c.fila + "," + c.col;
                if (!t.mapaRuns.containsKey(keyH) || !t.mapaRuns.containsKey(keyV)) {
                    System.err.println("Error: celda sin run asignada en (" + c.fila + "," + c.col + ")");
                    return null;
                }
            }

            return t;
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la lista de todas las celdas blancas del tablero.
     * Estas son las celdas que se deben completar con valores 1-9.
     * 
     * @return Lista de celdas blancas (vacías al inicio)
     */
    public List<Celda> getCeldasBlancas() {
        return celdasBlancas;
    }

    /**
     * Obtiene el run horizontal al que pertenece una celda.
     * 
     * @param c Celda de la cual obtener el run horizontal
     * @return Run horizontal que contiene esta celda
     */
    public Run getRunHorizontal(Celda c) {
        return mapaRuns.get("H" + c.fila + "," + c.col);
    }

    /**
     * Obtiene el run vertical al que pertenece una celda.
     * 
     * @param c Celda de la cual obtener el run vertical
     * @return Run vertical que contiene esta celda
     */
    public Run getRunVertical(Celda c) {
        return mapaRuns.get("V" + c.fila + "," + c.col);
    }

    /**
     * Valida que todos los runs del tablero cumplan sus restricciones:
     * - La suma de las celdas debe igualar el objetivo
     * - No puede haber números repetidos en un run
     * 
     * Se usa al final del backtracking para verificar la solución completa.
     * 
     * @return true si todas las sumas son válidas, false en caso contrario
     */
    /**
     * Valida que todos los runs del tablero cumplan sus restricciones:
     * - La suma de las celdas debe igualar el objetivo
     * - No puede haber números repetidos en un run
     * 
     * Se usa al final del backtracking para verificar la solución completa.
     * 
     * @return true si todas las sumas son válidas, false en caso contrario
     */
    public boolean validarSumasCompletas() {
        // Verificar todos los runs horizontales
        for (Run run : runsHorizontales) {
            if (!Validador.sumaEsValida(run)) return false;
        }
        // Verificar todos los runs verticales
        for (Run run : runsVerticales) {
            if (!Validador.sumaEsValida(run)) return false;
        }
        return true;
    }

    /**
     * Imprime el tablero actual en consola.
     * 
     * Formato de salida:
     * - "X" = celda negra
     * - "0" = celda blanca vacía (valor 0)
     * - Números 1-9 = celdas completadas
     * - Pistas con formato "n/-" o "-/m" (reemplazando 0 con -)
     */
    public void imprimir() {
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                if (matriz[i][j] != null) {
                    // Celda blanca: mostrar valor o 0
                    int val = matriz[i][j].valor;
                    System.out.print((val == 0 ? "0" : val) + " ");
                } else {
                    // Celda negra o con pistas: usar token original
                    String token = tokens[i][j];
                    if (token.contains("/")) {
                        // Es una pista: reemplazar 0 con -
                        String[] partes = token.split("/");
                        String parte1 = partes[0].equals("0") ? "-" : partes[0];
                        String parte2 = partes[1].equals("0") ? "-" : partes[1];
                        System.out.print(parte1 + "/" + parte2 + " ");
                    } else {
                        // Es una X o celda negra
                        System.out.print(token + " ");
                    }
                }
            }
            System.out.println();
        }
    }
}
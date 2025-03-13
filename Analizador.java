import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Analizador {

    private static final Map<String, TipoToken> operadores = new HashMap<>();
    private static final Map<Character, TipoToken> signosPuntuacion = new HashMap<>();
    private static final Map<String, TipoToken> palabrasReservadas = new HashMap<>();
    private final String fuente;
    private int linea;
    private boolean esperaPuntoYComa = false;

    static {
        operadores.put("+", TipoToken.MAS);
        operadores.put("-", TipoToken.MENOS);
        operadores.put("*", TipoToken.PRODUCTO);
        operadores.put("/", TipoToken.ENTRE);
        operadores.put("<", TipoToken.MENORQUE);
        operadores.put("<=", TipoToken.MENORIGUAL);
        operadores.put(">", TipoToken.MAYORQUE);
        operadores.put(">=", TipoToken.MAYORIGUAL);
        operadores.put("==", TipoToken.IGUALIGUAL);
        operadores.put("!=", TipoToken.DISTINTO);
        operadores.put("and", TipoToken.AND);
        operadores.put("or", TipoToken.OR);
        operadores.put("=", TipoToken.IGUAL);
        operadores.put("!", TipoToken.INVERSOR);

        signosPuntuacion.put('(', TipoToken.IZQ_PARENTESIS);
        signosPuntuacion.put(')', TipoToken.DER_PARENTESIS);
        signosPuntuacion.put(',', TipoToken.COMA);
        signosPuntuacion.put(';', TipoToken.PUNTO_COMA);
        signosPuntuacion.put('{', TipoToken.IZQ_LLAVE);
        signosPuntuacion.put('}', TipoToken.DER_LLAVE);

        palabrasReservadas.put("and", TipoToken.AND);
        palabrasReservadas.put("else", TipoToken.ELSE);
        palabrasReservadas.put("false", TipoToken.FALSE);
        palabrasReservadas.put("for", TipoToken.FOR);
        palabrasReservadas.put("fun", TipoToken.FUN);
        palabrasReservadas.put("if", TipoToken.IF);
        palabrasReservadas.put("null", TipoToken.NULL);
        palabrasReservadas.put("or", TipoToken.OR);
        palabrasReservadas.put("print", TipoToken.PRINT);
        palabrasReservadas.put("return", TipoToken.RETURN);
        palabrasReservadas.put("true", TipoToken.TRUE);
        palabrasReservadas.put("var", TipoToken.VAR);
        palabrasReservadas.put("while", TipoToken.WHILE);
        palabrasReservadas.put("int", TipoToken.INT);
        palabrasReservadas.put("float", TipoToken.FLOAT);
        palabrasReservadas.put("double", TipoToken.DOUBLE);
        palabrasReservadas.put("string", TipoToken.CADENA);
    }

    public Analizador(String fuente, int linea) {
        this.fuente = fuente;
        this.linea = linea; // El contador de línea se pasa desde el main
    }

    public void escanear() {
        int estado = 0;
        String lexema = "";
        for (int i = 0; i < fuente.length(); i++) {
            char c = fuente.charAt(i);

            switch (estado) {
                case 0:
                    if (c == '>') {
                        estado = 1;
                        lexema += c;
                    } else if (c == '<') {
                        estado = 4;
                        lexema += c;
                    } else if (c == '=') {
                        estado = 7;
                        lexema += c;
                        esperaPuntoYComa = true; // Se espera un punto y coma después de una asignación
                    } else if (Character.isLetter(c)) {
                        estado = 13;
                        lexema += c;
                    } else if (Character.isDigit(c)) {
                        estado = 15;
                        lexema += c;
                    } else if (c == '"') {
                        estado = 24;
                        lexema += c;
                    }
                    break;
                case 1:
                    if (c == '=') {
                        lexema += c;
                        generarToken(TipoToken.MAYORIGUAL, lexema, lexema);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarToken(TipoToken.MAYORQUE, lexema, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 4:
                    if (c == '=') {
                        lexema += c;
                        generarToken(TipoToken.MENORIGUAL, lexema, lexema);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarToken(TipoToken.MENORQUE, lexema, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 7:
                    if (c == '=') {
                        lexema += c;
                        generarToken(TipoToken.IGUALIGUAL, lexema, lexema);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarToken(TipoToken.IGUAL, lexema, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 10:
                    if (c == '=') {
                        lexema += c;
                        generarToken(TipoToken.DISTINTO, lexema, lexema);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarToken(TipoToken.INVERSOR, lexema, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 13:
                    if (Character.isLetterOrDigit(c)) {
                        estado = 13;
                        lexema += c;
                    } else {
                        TipoToken tokenPalabraReservada = obtenerPalabraReservada(lexema);
                        if (tokenPalabraReservada == null) {
                            generarToken(TipoToken.IDENTIFICADOR, lexema, lexema);
                        } else {
                            generarToken(tokenPalabraReservada, lexema, lexema);
                        }
                        estado = 0;
                        lexema = "";
                        i--;  // Retrocede el índice para procesar el siguiente carácter
                    }
                    break;
                case 15:
                    if (Character.isDigit(c)) {
                        estado = 15;
                        lexema += c;
                    } else if (c == '.') {
                        estado = 16;
                        lexema += c;
                    } else if (c == 'E') {
                        estado = 17;
                        lexema += c;
                    } else {
                        generarToken(TipoToken.INT, lexema, lexema);
                        estado = 0;
                        lexema = "";
                    }
                    break;
                case 16:
                    if (Character.isDigit(c)) {
                        estado = 16;
                        lexema += c;
                    } else if (c == '.') {
                        estado = 16;
                        lexema += c;
                    } else if (c == 'E') {
                        estado = 17;
                        lexema += c;
                    } else {
                        generarToken(TipoToken.FLOAT, lexema, lexema);
                        estado = 0;
                        lexema = "";
                    }
                    break;
                case 17:
                    if (Character.isDigit(c)) {
                        estado = 17;
                        lexema += c;
                    } else if (c == '+') {
                        estado = 17;
                        lexema += c;
                    } else if (c == '-') {
                        estado = 17;
                        lexema += c;
                    } else {
                        generarToken(TipoToken.DOUBLE, lexema, lexema);
                        estado = 0;
                        lexema = "";
                    }
                    break;
                case 24:
                    if (c == '"') {
                        lexema += c;
                        generarToken(TipoToken.CADENA, lexema, lexema);
                        estado = 0;
                        lexema = "";
                    } else if (i == fuente.length() - 1) {
                        // Si llegamos al final de la línea, continuamos en la siguiente línea
                        System.out.println("ERROR: Se detectó una cadena sin cerrar en la línea " + linea);
                        lexema += "\n"; // Agregar salto de línea para mantener la cadena
                    } else {
                        lexema += c; // Seguir acumulando caracteres
                    }
                    break;
            }

            if (esperaPuntoYComa && c != ';' && i == fuente.length() - 1) {
                // Si llegamos al final de la línea y se esperaba un punto y coma
                System.out.println("ERROR: Se detectó una sentencia sin punto y coma en la línea " + linea);
                esperaPuntoYComa = false; // Resetear la bandera
            }

            if (c == ';') {
                esperaPuntoYComa = false; // Se encontró el punto y coma, ya no se espera uno
            }
        }
    }

    private void generarToken(TipoToken tipo, String lexema, String literal) {
        System.out.println("< " + tipo + ", " + lexema + ", " + literal + ", " + linea + " >");
    }

    public static TipoToken obtenerOperador(String lexema) {
        return operadores.get(lexema);
    }

    public static TipoToken obtenerSignoPuntuacion(char c) {
        return signosPuntuacion.get(c);
    }

    public static TipoToken obtenerPalabraReservada(String lexema) {
        return palabrasReservadas.get(lexema);
    }

    // Método para leer desde un archivo
    public static void procesarArchivo(String archivo) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(archivo));
        String linea;
        int numLinea = 1;
        while ((linea = reader.readLine()) != null) {
            Analizador analizador = new Analizador(linea, numLinea);
            analizador.escanear();
            numLinea++;  // Incrementamos el contador de líneas después de cada entrada
        }
        reader.close();
    }

    // Modo REPL (lectura interactiva)
    public static void modoREPL() {
        Scanner scanner = new Scanner(System.in);
        int numLinea = 1;
        while (true) {
            System.out.print(">>> ");
            String linea = scanner.nextLine();

            if (linea.equalsIgnoreCase("salir")) {
                break;
            }

            Analizador analizador = new Analizador(linea, numLinea);
            analizador.escanear();
            numLinea++;  // Incrementamos el contador de líneas después de cada entrada
        }

        scanner.close();
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            // Si no se pasa un archivo, entramos al modo REPL
            modoREPL();
        } else {
            // Si se pasa un archivo como argumento, procesamos el archivo
            String archivo = args[0];
            procesarArchivo(archivo);
        }
    }
}

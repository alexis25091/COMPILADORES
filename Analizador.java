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

    static {
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

        signosPuntuacion.put('+', TipoToken.MAS);
        signosPuntuacion.put('-', TipoToken.MENOS);
        signosPuntuacion.put('*', TipoToken.PRODUCTO);
        signosPuntuacion.put(';', TipoToken.PUNTO_COMA);
        signosPuntuacion.put(',', TipoToken.COMA);

        signosPuntuacion.put('(', TipoToken.IZQ_PARENTESIS);
        signosPuntuacion.put(')', TipoToken.DER_PARENTESIS);
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
        char previous = ' ';
        // int Primerlinea = 0;
        boolean ultimoPuntoYComa = false;
        for (int i = 0; i < fuente.length(); i++) {
            char c = fuente.charAt(i);

            switch (estado) {


                case 0: // Estado general de análisis
                    TipoToken tokenSignoPuntuacion = obtenerSignoPuntuacion(c);
                    if (c == '>') {
                        estado = 1;
                        lexema += c;
                    } else if (c == '<') {
                        estado = 4;
                        lexema += c;
                    } else if (c == '=') {
                        estado = 7;
                        lexema += c;
                    } else if (c == '!') {
                        estado = 10;
                        lexema += c;
                    } else if (Character.isLetter(c)) {
                        estado = 13;
                        lexema += c;
                    } else if (Character.isDigit(c)) {
                        estado = 15;
                        lexema += c;
                    } else if (c == '"') {
                        estado = 24;
                        lexema += c;
                    } else if (c == '/') {
                        // Si encuentra '/', entra al estado de comentario
                        estado = 26;
                        lexema += c;
                    } else if (tokenSignoPuntuacion != null) {
                        if (c == ';') {
                            if (ultimoPuntoYComa) {
                                System.out.println("Error: punto y coma consecutivo en línea " + linea);
                            } else {
                                generarTokenSimple(tokenSignoPuntuacion);
                                ultimoPuntoYComa = true;
                            }
                        } else {
                            generarTokenSimple(tokenSignoPuntuacion);
                            ultimoPuntoYComa = false;
                        }
                    } else if (c == '.') {
                        System.out.println("Carácter no válido, error en línea: " + linea);
                    }
                    break;

                case 1:
                    if (c == '=') {
                        lexema += c;
                        generarTokenSimple(TipoToken.MAYORIGUAL);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarTokenSimple(TipoToken.MAYORQUE);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 4:
                    if (c == '=') {
                        lexema += c;
                        generarTokenSimple(TipoToken.MENORIGUAL);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarTokenSimple(TipoToken.MENORQUE);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 7:
                    if (c == '=') {
                        lexema += c;
                        generarTokenSimple(TipoToken.IGUALIGUAL);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarTokenSimple(TipoToken.IGUAL);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;
                case 10:
                    if (c == '=') {
                        lexema += c;
                        generarTokenSimple(TipoToken.DISTINTO);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarTokenSimple(TipoToken.INVERSOR);
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
                        TipoToken tokenPalabraReservada = obtenerPalabraReservada(String.valueOf(lexema));
                        if (tokenPalabraReservada == null) {
                            generarTokenMediano(TipoToken.IDENTIFICADOR, lexema);
                            estado = 0;
                            lexema = "";
                        } else {
                            generarTokenSimple(tokenPalabraReservada);
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
                        estado = 0;
                        generarToken(TipoToken.INT, lexema);
                        lexema = "";
                        i--;
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
                        estado = 0;
                        generarToken(TipoToken.FLOAT, lexema);
                        lexema = "";
                        i--;
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
                        estado = 0;
                        generarToken(TipoToken.DOUBLE, lexema);
                        lexema = "";
                        i--;
                    }
                    break;

                case 24:

                    if (c == '"') {
                        estado = 0;
                        lexema += c;
                        generarToken(TipoToken.CADENA, lexema);
                        lexema = "";
                    } else if (i == fuente.length() - 1) {
                        System.out.println("Cadena sin cerrar en línea " + linea);
                        lexema += "\n"; // agregar salto de linea para mantener la cadena
                    } else {
                        lexema += c; // seguir acumulando caracteres
                    }
                    break;

                case 26: // comentario de línea
                    if (c == '\n') {
                        estado = 0; // fin del comentario de línea volver al estado inicial
                    }
                    break;

                case 27: // comentario de bloque
                    if (c == '*') {
                        estado = 28; // potencial fin de comentario
                    }
                    break;

                case 28: // verificar si es fin de comentario de bloque
                    if (c == '/') {
                        estado = 0; // Fin del comentario de bloque volver al estado inicial
                    } else if (c != '*') {
                        estado = 27; // continuar leyendo el comentario de bloque
                    }
                    break;

                case 30: // estado para comentarios de linea
                    if (c == '\n') {
                        estado = 0;
                    }
                    break;

            }
            if (i == fuente.length() - 1 && !ultimoPuntoYComa && estado != 26 && estado != 27) {
                System.out.println("Error: falta el punto y coma al final de la línea " + linea);
            }

        }

    }

    private void generarTokenSimple(TipoToken tipo) {
        System.out.println("< " + tipo + ", " + linea + " >");
    }

    private void generarTokenMediano(TipoToken tipo, String lexema) {
        System.out.println("< " + tipo + ", " + lexema + ", " + linea + " >");
    }

    private void generarToken(TipoToken tipo, String lexema) {
        String literal = convertirALiteral(tipo, lexema);
        System.out.println("< "
                + tipo
                + ", lexema: " + lexema
                + ", literal: " + literal
                + ", linea: " + linea
                + " >");
    }

    private String convertirALiteral(TipoToken tipo, String lexema) {
        try {
            switch (tipo) {
                case INT:

                    return String.valueOf(Integer.parseInt(lexema));

                case FLOAT:
                case DOUBLE:
                    // Aquí parseamos a double
                    double valorDouble = Double.parseDouble(lexema);



                    if (valorDouble == (long) valorDouble) {

                        return String.valueOf((long) valorDouble);
                    } else {
                        return String.valueOf(valorDouble);
                    }

                case CADENA:
                    // Quita comillas inicial y final
                    if (lexema.length() >= 2
                            && lexema.startsWith("\"")
                            && lexema.endsWith("\"")) {
                        return lexema.substring(1, lexema.length() - 1);
                    }
                    return lexema;

                case TRUE:
                    return "true";

                case FALSE:
                    return "false";

                case NULL:
                    return "null";

                default:
                    return lexema;
            }
        } catch (NumberFormatException e) {
            // Si falla el parseo, devolvemos el lexema
            return lexema;
        }
    }

    // depura
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
        System.out.println("<EOF, lexema: $>");
        reader.close();
    }

    // Modo REPL (lectura interactiva)
    public static void modoREPL() {
        Scanner scanner = new Scanner(System.in);

        int numLinea = 1;
        while (true) {
            System.out.print(">> ");

            if (!scanner.hasNextLine()) {

                break;
            }
            String linea = scanner.nextLine();

            Analizador analizador = new Analizador(linea, numLinea);
            analizador.escanear();
            numLinea++;
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

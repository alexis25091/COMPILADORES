import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Analizador {

    // Mapa de operadores
    private static final Map<String, TipoToken> operadores = new HashMap<>();
    // Mapa de signos de puntuación
    private static final Map<Character, TipoToken> signosPuntuacion = new HashMap<>();
    // Mapa de palabras reservadas
    private static final Map<String, TipoToken> palabrasReservadas = new HashMap<>();

    private final String fuente;
    private int linea;

    // Bloque estático para inicializar mapas
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
        signosPuntuacion.put('.', TipoToken.PUNTO);
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
        // int Primerlinea = 0;  // no parece usarse
        for (int i = 0; i < fuente.length(); i++) {
            char c = fuente.charAt(i);

            switch (estado) {
                case 0:
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
                        estado = 26;
                        lexema += c;
                    } else if (tokenSignoPuntuacion != null) {
                        generarToken(tokenSignoPuntuacion, String.valueOf(c));
                    }
                    break;

                case 1:
                    if (c == '=') {
                        lexema += c;
                        generarToken(TipoToken.MAYORIGUAL, lexema);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarToken(TipoToken.MAYORQUE, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;

                case 4:
                    if (c == '=') {
                        lexema += c;
                        generarToken(TipoToken.MENORIGUAL, lexema);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarToken(TipoToken.MENORQUE, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;

                case 7:
                    if (c == '=') {
                        lexema += c;
                        generarToken(TipoToken.IGUALIGUAL, lexema);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarToken(TipoToken.IGUAL, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;

                case 10:
                    if (c == '=') {
                        lexema += c;
                        generarToken(TipoToken.DISTINTO, lexema);
                        estado = 0;
                        lexema = "";
                    } else {
                        generarToken(TipoToken.INVERSOR, lexema);
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
                        // Verificamos si es palabra reservada
                        TipoToken tokenPalabraReservada = obtenerPalabraReservada(lexema);
                        if (tokenPalabraReservada == null) {
                            generarToken(TipoToken.IDENTIFICADOR, lexema);
                        } else {
                            generarToken(tokenPalabraReservada, lexema);
                        }
                        estado = 0;
                        lexema = "";
                        i--;  // Retrocede para procesar el siguiente carácter en estado 0
                    }
                    break;

                case 15:
                    // Estamos leyendo dígitos (posible entero)
                    if (Character.isDigit(c)) {
                        estado = 15;
                        lexema += c;
                    } else if (c == '.') {
                        estado = 16;
                        lexema += c;
                    } else if (c == 'E' || c == 'e') {
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
                    // Estamos leyendo un número con punto decimal
                    if (Character.isDigit(c)) {
                        estado = 16;
                        lexema += c;
                    } else if (c == '.') {
                        // Depende si quieres permitir dos puntos seguidos.
                        // Aquí lo dejo como en el código base, aunque es algo inusual.
                        estado = 16;
                        lexema += c;
                    } else if (c == 'E' || c == 'e') {
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
                    // Notación científica (E, e, con signo o dígitos)
                    if (Character.isDigit(c) || c == '+' || c == '-') {
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
                    // Leyendo una cadena
                    if (c == '"') {
                        // Cierra la cadena
                        estado = 0;
                        lexema += c;
                        generarToken(TipoToken.CADENA, lexema);
                        lexema = "";
                    } else if (i == fuente.length() - 1) {
                        // Llegamos al final sin cerrar la cadena
                        System.out.println("Cadena sin cerrar en línea: " + linea);
                        lexema += "\n";
                    } else {
                        // Seguimos acumulando caracteres en la cadena
                        lexema += c;
                    }
                    break;

                // INICIO DE MANEJO DE COMENTARIOS
                case 26:
                    // Verificamos si viene '//' o '/*'
                    if (c != '/') {
                        // No es ni // ni /*
                        i--;
                        System.out.println("SLASH SIMPLE");
                        estado = 0;
                        lexema = "";
                    } else {
                        // Tenemos "/?"
                        if (c == '*') {
                            estado = 27;
                            lexema += c;
                            previous = c;
                        }
                        if (c == '/') {
                            // Comentario de línea
                            estado = 30;
                        }
                    }
                    break;

                case 27:
                    // Estado para comentarios de bloque '/* ...'
                    if (c == '*' && previous == '*') {
                        lexema += c;
                        estado = 28;
                    } else {
                        lexema += c;
                        previous = c;
                    }
                    break;

                case 28:
                    // Verificar si realmente es el fin del comentario de bloque "*/"
                    if (c == '/') {
                        lexema += c;
                        estado = 0;
                        System.out.println("COMENTARIO BLOQUE COMPLETO: " + lexema);
                        lexema = "";
                    } else if (c != '*') {
                        estado = 27;
                        lexema += c;
                        previous = c;
                    } else {
                        // sigue en 28 pero si no es '/', volvemos al 27
                        lexema += c;
                        previous = c;
                    }
                    break;

                case 30:
                    // Estado para comentarios de línea '//...'
                    if (c == '\n') {
                        System.out.println("COMENTARIO DE UNA SOLA LINEA: " + lexema);
                        estado = 0;
                        lexema = "";
                    } else {
                        lexema += c;
                    }
                    break;
            }
        }
    }

    /**
     * Genera un token con el lexema dado.
     * Se parsea un “literal” si corresponde (números, boolean, null, cadenas).
     */
    private void generarToken(TipoToken tipo, String lexema) {
        String literal = convertirALiteral(tipo, lexema);
        System.out.println("< "
                + tipo
                + ", lexema: " + lexema
                + ", literal: " + literal
                + ", linea: " + linea
                + " >");
    }

    /**
     * Convierte el lexema a su valor “literal” real, si corresponde:
     * - INT -> se parsea como entero
     * - FLOAT, DOUBLE -> se parsea como double
     * - CADENA -> se quitan comillas inicial y final
     * - TRUE, FALSE, NULL -> se devuelven cadenas "true", "false", "null"
     * - Si no es uno de los anteriores, se devuelve el lexema tal cual
     */
    private String convertirALiteral(TipoToken tipo, String lexema) {
        try {
            switch (tipo) {
                case INT:
                    // Ejemplo: "321" -> 321
                    return String.valueOf(Integer.parseInt(lexema));

                case FLOAT:
                case DOUBLE:
                    // Aquí parseamos a double
                    double valorDouble = Double.parseDouble(lexema);

                    // Si el número es un entero (por ejemplo, 5E3 -> 5000.0),
                    // podemos forzar la impresión sin ".0":
                    if (valorDouble == (long) valorDouble) {
                        // Esto quita el ".0"
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


    // Métodos para obtener tokens según tablas
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
            System.out.print(">> ");

            // Verificamos si hay una línea disponible
            if (!scanner.hasNextLine()) {
                // Si no hay más líneas, es EOF (Ctrl+D o Ctrl+Z)
                break;
            }
            String linea = scanner.nextLine();

            Analizador analizador = new Analizador(linea, numLinea);
            analizador.escanear();
            numLinea++;
        }

        scanner.close();
    }

    // Método principal
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

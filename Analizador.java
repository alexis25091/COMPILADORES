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
        //signosPuntuacion.put('.', TipoToken.PUNTO);
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
        this.linea = linea; // Valor inicial de la línea
    }

    public void escanear() {
        int estado = 0;
        String lexema = "";

        for (int i = 0; i < fuente.length(); i++) {
            char c = fuente.charAt(i);

            // Contamos líneas para reportar correctamente
            if (c == '\n') {
                linea++;
            }

            switch (estado) {
                case 0:
                    TipoToken tokenSignoPuntuacion = obtenerSignoPuntuacion(c);

                    if (c == '>') {
                        estado = 1;
                        lexema = String.valueOf(c);
                    } else if (c == '<') {
                        estado = 4;
                        lexema = String.valueOf(c);
                    } else if (c == '=') {
                        estado = 7;
                        lexema = String.valueOf(c);
                    } else if (c == '!') {
                        estado = 10;
                        lexema = String.valueOf(c);
                    } else if (Character.isLetter(c)) {
                        estado = 13;
                        lexema = String.valueOf(c);
                    } else if (Character.isDigit(c)) {
                        estado = 15;
                        lexema = String.valueOf(c);
                    } else if (c == '"') {
                        estado = 24;
                        lexema = String.valueOf(c);
                    } else if (c == '/') {
                        // Podría ser división o inicio de comentario
                        estado = 26;
                        lexema = ""; // Limpio para distinguir bien
                    } else if (tokenSignoPuntuacion != null) {
                        generarToken(tokenSignoPuntuacion, String.valueOf(c));
                    }else if (c == '.') {
                        System.out.println("Carácter no válido, error en línea: " + linea);
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
                        i--;
                    }
                    break;

                case 15:
                    if (Character.isDigit(c)) {
                        lexema += c;
                    } else if (c == '.') {
                        estado = 16;
                        lexema += c;
                    } else if (c == 'E' || c == 'e') {
                        estado = 17;
                        lexema += c;
                    } else {
                        // Ya no es parte del número
                        generarToken(TipoToken.INT, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;

                case 16:
                    if (Character.isDigit(c)) {
                        lexema += c;
                    } else if (c == '.') {
                        lexema += c;  // casos de 12.34.?
                    } else if (c == 'E' || c == 'e') {
                        estado = 17;
                        lexema += c;
                    } else {
                        generarToken(TipoToken.FLOAT, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;

                case 17:
                    if (Character.isDigit(c) || c == '+' || c == '-') {
                        lexema += c;
                    } else {
                        generarToken(TipoToken.DOUBLE, lexema);
                        estado = 0;
                        lexema = "";
                        i--;
                    }
                    break;

                case 24:
                    // Leyendo una cadena
                    if (c == '"') {
                        lexema += c;
                        generarToken(TipoToken.CADENA, lexema);
                        estado = 0;
                        lexema = "";
                    } else if (i == fuente.length() - 1) {
                        // Llegamos al final sin cerrar comillas
                        System.out.println("Cadena sin cerrar en línea: " + linea);
                        lexema += "\n";
                    } else {
                        lexema += c;
                    }
                    break;

                case 26:
                    // Acabamos de leer '/'
                    if (c == '/') {
                        // Comentario de línea
                        estado = 30;
                        // Aquí podríamos guardar lexema si quisiéramos mostrarlo
                        lexema = "";
                    } else if (c == '*') {
                        // Comentario de bloque
                        estado = 27;
                        lexema = "";
                    } else {
                        // No es comentario, es el operador de división
                        generarToken(TipoToken.ENTRE, "/");
                        estado = 0;
                        i--;
                    }
                    break;

                case 27:
                    // Dentro de un comentario de bloque
                    if (c == '*') {
                        estado = 28; // posible cierre
                    }
                    // Se ignora el resto
                    break;

                case 28:
                    // Posible cierre de comentario de bloque
                    if (c == '/') {
                        // Cierra el comentario
                        estado = 0;
                        // Si quisieras imprimir algo:
                        // System.out.println("Fin comentario de bloque en línea " + linea);
                    } else if (c == '*') {
                        // Pueden venir varios '*' seguidos
                        // seguimos en 28
                    } else {
                        // No se cerró, regresamos al estado 27
                        estado = 27;
                    }
                    break;

                case 30:
                    // Comentario de línea: ignorar todo hasta '\n'
                    if (c == '\n') {
                        estado = 0;
                        // Si quisieras imprimir algo:
                        // System.out.println("Fin comentario de línea en " + linea);
                    }
                    // Ignoramos el resto
                    break;
            }
        }
    }

    /**
     * Genera un token con el lexema dado.
     */
    private void generarToken(TipoToken tipo, String lexema) {
        String literal = convertirALiteral(tipo, lexema);
        System.out.println("< " + tipo + ", lexema: " + lexema + ", literal: " + literal + ", linea: " + linea + " >");
    }

    /**
     * Convierte el lexema a su valor “literal” real, si corresponde.
     */
    private String convertirALiteral(TipoToken tipo, String lexema) {
        try {
            switch (tipo) {
                case INT:
                    return String.valueOf(Integer.parseInt(lexema));
                case FLOAT:
                case DOUBLE:
                    double valorDouble = Double.parseDouble(lexema);
                    if (valorDouble == (long) valorDouble) {
                        return String.valueOf((long) valorDouble);
                    } else {
                        return String.valueOf(valorDouble);
                    }
                case CADENA:
                    if (lexema.length() >= 2 && lexema.startsWith("\"") && lexema.endsWith("\"")) {
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
            // Si falla el parseo numérico, se devuelve el lexema tal cual
            return lexema;
        }
    }

    // Métodos para obtener tokens según las tablas definidas
    public static TipoToken obtenerOperador(String lexema) {
        return operadores.get(lexema);
    }

    public static TipoToken obtenerSignoPuntuacion(char c) {
        return signosPuntuacion.get(c);
    }

    public static TipoToken obtenerPalabraReservada(String lexema) {
        return palabrasReservadas.get(lexema);
    }

    /**
     * Procesa el archivo completo como una sola cadena,
     * para que los comentarios de bloque que abarcan múltiples líneas se manejen correctamente.
     */
    public static void procesarArchivo(String archivo) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(archivo));
        StringBuilder contenido = new StringBuilder();
        String lineaLeida;
        int numLinea = 1;
        while ((lineaLeida = reader.readLine()) != null) {
            contenido.append(lineaLeida).append("\n");
            numLinea++;
        }
        reader.close();

        // Analiza todo el contenido en una sola pasada
        Analizador analizador = new Analizador(contenido.toString(), 1);
        analizador.escanear();
    }

    // Modo REPL (lectura interactiva)
    public static void modoREPL() {
        Scanner scanner = new Scanner(System.in);
        StringBuilder entrada = new StringBuilder();
        int numLinea = 1;
        System.out.print(">> ");
        while (scanner.hasNextLine()) {
            String lineaLeida = scanner.nextLine();
            entrada.append(lineaLeida).append("\n");
            Analizador analizador = new Analizador(entrada.toString(), numLinea);
            analizador.escanear();
            entrada.setLength(0); // Limpiar entrada
            numLinea++;
            System.out.print(">> ");
        }
        scanner.close();
    }


    // Método principal
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            modoREPL();
        } else {
            String archivo = args[0];
            procesarArchivo(archivo);
        }
    }
}

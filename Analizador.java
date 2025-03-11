import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Analizador {

    private static final Map<String, TipoToken> operadores = new HashMap<>();
    private static final Map<Character, TipoToken> signosPuntuacion = new HashMap<>();
    private final String fuente;
    private int linea = 1;

    // mapas con los operadores y signos de puntuaicon
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
    }

    // Recibe el texto a analizar del documento que se le pasa
    public Analizador(String fuente) {
        this.fuente = fuente;
    }

    // aqui solo va analizando line a linea el documento que se le pasa hasta encontrar el /n
    public void escanear() {
        for (int i = 0; i < fuente.length(); i++) {
            char c = fuente.charAt(i);

            // Si encuentra un salto de línea, aumenta el contador
            if (c == '\n') {
                linea++;
                continue;
            }

            // ( ; { } ( ) )
            TipoToken tokenPuntuacion = obtenerSignoPuntuacion(c);
            if (tokenPuntuacion != null) {
                Token token = new Token(tokenPuntuacion, String.valueOf(c), null, linea);
                System.out.println(token);
                continue;
            }

            // (>=, <=, ==, !=)
            if (i < fuente.length() - 1) {
                String operador = "" + c + fuente.charAt(i + 1);
                TipoToken tokenOperador = obtenerOperador(operador);
                if (tokenOperador != null) {
                    Token token = new Token(tokenOperador, operador, null, linea);
                    System.out.println(token);
                    i++; // Salta el siguiente caracter
                    continue;
                }
            }

            // (+, -, *, /, =, !)
            TipoToken tokenOperadorSimple = obtenerOperador(String.valueOf(c));
            if (tokenOperadorSimple != null) {
                Token token = new Token(tokenOperadorSimple, String.valueOf(c), null, linea);
                System.out.println(token);
                continue;
            }
        }

        // aqui la funcion para que imprimamos el EOF de que ya leyo todo
        Token tokenEOF = new Token(TipoToken.EOF, "$", null, linea);
        System.out.println(tokenEOF);
    }

    public static TipoToken obtenerOperador(String lexema) {
        return operadores.get(lexema);
    }

    public static TipoToken obtenerSignoPuntuacion(char c) {
        return signosPuntuacion.get(c);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Analizador <archivo>");
            return;
        }

        File archivo = new File(args[0]);
        if (!archivo.exists()) {
            System.out.println("El archivo no existe.");
            return;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                Analizador analizador = new Analizador(linea);
                analizador.escanear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class Token {
        private final TipoToken tipo;
        private final String lexema;
        private final Object literal;
        private final int linea;

        public Token(TipoToken tipo, String lexema, Object literal, int linea) {
            this.tipo = tipo;
            this.lexema = lexema;
            this.literal = literal;
            this.linea = linea;
        }

        @Override
        public String toString() {
            if (literal == null) {
                return "<" + tipo + ", línea: " + linea + ">";
            }
            return "<" + tipo + ", lexema: " + lexema + ", literal: " + literal + ", línea: " + linea + ">";
        }
    }
}

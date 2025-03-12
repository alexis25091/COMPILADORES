    import java.io.*;
    import java.util.HashMap;
    import java.util.Map;

    public class Analizador {
        private static final Map<String, TipoToken> operadores = new HashMap<>();
        private static final Map<Character, TipoToken> signosPuntuacion = new HashMap<>();
        private static final Map<String, TipoToken> palabrasReservadas = new HashMap<>();
        private final String fuente;
        private int linea;

        static {
            // operadores
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

            // signos de puntuación
            signosPuntuacion.put('(', TipoToken.IZQ_PARENTESIS);
            signosPuntuacion.put(')', TipoToken.DER_PARENTESIS);
            signosPuntuacion.put(',', TipoToken.COMA);
            signosPuntuacion.put(';', TipoToken.PUNTO_COMA);
            signosPuntuacion.put('{', TipoToken.IZQ_LLAVE);
            signosPuntuacion.put('}', TipoToken.DER_LLAVE);

            // Definir palabras reservadas
            palabrasReservadas.put("if", TipoToken.IF);
            palabrasReservadas.put("else", TipoToken.ELSE);
            palabrasReservadas.put("while", TipoToken.WHILE);
            palabrasReservadas.put("for", TipoToken.FOR);
            palabrasReservadas.put("return", TipoToken.RETURN);
            palabrasReservadas.put("true", TipoToken.TRUE);
            palabrasReservadas.put("false", TipoToken.FALSE);
        }

        public Analizador(String fuente, int linea) {
            this.fuente = fuente;
            this.linea = linea; // El contador de línea se pasa desde el main
        }

        public void escanear() {
            int i = 0;
            boolean finLectura = false;

            while (i < fuente.length()) {
                char c = fuente.charAt(i);

                // Manejar comentarios
                if (c == '/' && i + 1 < fuente.length()) {
                    char siguiente = fuente.charAt(i + 1);
                    if (siguiente == '/') {
                        // Comentario de una línea saltar hasta el final de la línea
                        while (i < fuente.length() && fuente.charAt(i) != '\n') {
                            i++;
                        }
                        continue;
                    } else if (siguiente == '*') {
                        // Comentario multilinea
                        i += 2;
                        while (i + 1 < fuente.length() && !(fuente.charAt(i) == '*' && fuente.charAt(i + 1) == '/')) {
                            i++;
                        }
                        i += 2;
                        continue;
                    }
                }

                // Ignorar espacios y tabulaciones
                if (Character.isWhitespace(c)) {
                    if (c == '\n') {
                        linea++;  // Incrementamos la línea cuando encontramos un salto de línea
                    }
                    i++;
                    continue;
                }

                switch (c) {
                    // Manejo de signos de puntuacion
                    case '(': case ')': case '{': case '}': case ',': case ';':
                        imprimirToken(signosPuntuacion.get(c), String.valueOf(c));
                        i++;  // Avanzamos al siguiente caracter
                        break;

                    // (+, -, *, /, =, !=, <, >, )
                    case '+': case '-': case '*': case '/': case '=': case '!': case '<': case '>':
                        if (i < fuente.length() - 1) {
                            String doble = "" + c + fuente.charAt(i + 1);  // Verificar que el operador es de 2 caracteres
                            if (operadores.containsKey(doble)) {
                                imprimirToken(operadores.get(doble), doble);  // Si encontramos el operador de dos caracteres lo procesamos
                                i += 2;  // Avanzamos dos posiciones en el texto
                                break;
                            }
                        }
                        imprimirToken(operadores.get(String.valueOf(c)), String.valueOf(c));  // Si es operador de un solo caracter lo procesamos
                        i++;  // Avanzamos una posicion
                        break;

                    // case() aquí para booleanos (true, false)

                    // case() aquí para números (enteros, decimales)

                    // case() aquí para caracteres inesperados (errores lexicos)

                    // Manejo de identificadores (palabras reservadas o variables)
                    default:
                        if (Character.isLetter(c)) {
                            int inicio = i;  // Guardamos la posicion inicial de la palabra
                            while (i < fuente.length() && Character.isLetterOrDigit(fuente.charAt(i))) {
                                i++;  // Avanzamos por los caracteres que forman parte del identificador
                            }
                            String palabra = fuente.substring(inicio, i);  // Extraemos el identificador
                            TipoToken tipo = palabrasReservadas.getOrDefault(palabra, TipoToken.IDENTIFICADOR);  // Verificamos si es palabra reservada
                            imprimirToken(tipo, palabra);  // Imprimimos el token
                        }
                        break;
                }


                // Marca el final de la lectura solo cuando hemos procesado toda la fuente
                if (i == fuente.length() && !finLectura) {
                    finLectura = true;
                    imprimirToken(TipoToken.EOF, "$");
                }
                break;
            }
        }

        private void imprimirToken(TipoToken tipo, String lexema) {
            System.out.println(new Token(tipo, lexema, null, linea));
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
                int numLinea = 1;  // Control de la línea
                while ((linea = lector.readLine()) != null) {
                    Analizador analizador = new Analizador(linea, numLinea); // Pasamos el numero de línea al analizador
                    analizador.escanear();
                    numLinea++;  // Incrementamos el contador de líneas despues de cada lectura
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
                return literal == null ? "<" + tipo + ", línea: " + linea + ">"
                        : "<" + tipo + ", lexema: " + lexema + ", literal: " + literal + ", línea: " + linea + ">";
            }
        }
    }

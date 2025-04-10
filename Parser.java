import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0; // apuntador al token actual

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public boolean parse() {
        try {
            program();
            if (isAtEnd()) {
                System.out.println("parseo completado correctamente");
                return true;
            } else {
                System.out.println("error: no se consumieron todos los tokens");
                return false;
            }
            // Mandamos msj
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private void program() {
        //System.out.println("regla <program> llamada");
        while (!isAtEnd()) {
            declaration();
        }
    }


    // declaraciones
    private void declaration() {
        if (check(TipoToken.FUN)) {
            funDecl();
        } else if (check(TipoToken.VAR)) {
            varDecl();
        } else {
            statement(); // Agrega esta línea para aceptar sentencias como declaraciones
        }
    }

    private void funDecl() {
        consume(TipoToken.FUN, "Se esperaba 'fun'");
        consume(TipoToken.IDENTIFICADOR, "Se esperaba nombre de la función");
        consume(TipoToken.IZQ_PARENTESIS, "Se esperaba '('");
        parameters();
        consume(TipoToken.DER_PARENTESIS, "Se esperaba ')'");
        block();
    }

    private void parameters() {
        // implementacion minima por ahora :]
        if (check(TipoToken.IDENTIFICADOR)) {
            advance();
            while (check(TipoToken.COMA)) {
                advance();
                consume(TipoToken.IDENTIFICADOR, "Se esperaba otro parámetro");
            }
        }
    }

    private void block() {
        consume(TipoToken.IZQ_LLAVE, "Se esperaba '{'");
        while (!check(TipoToken.DER_LLAVE) && !isAtEnd()) {
            advance(); // aun falta terminar, podemos agregar otras funciones al bloque
        }
        consume(TipoToken.DER_LLAVE, "Se esperaba '}'");
    }

    private void varDecl() {
        consume(TipoToken.VAR, "Se esperaba 'var'");
        consume(TipoToken.IDENTIFICADOR, "Se esperaba nombre de la variable");
        // aqui inicia VAR_INIT
        if (check(TipoToken.IGUAL)) {
            advance();
            expression();
        }
        consume(TipoToken.PUNTO_COMA, "Se esperaba ';'");
    }

    private void statement() {
        if (check(TipoToken.FOR)) {
            forStmt();
        } else if (check(TipoToken.IF)) {
            ifStmt();
        } else if (check(TipoToken.PRINT)) {
            printStmt();
        } else if (check(TipoToken.RETURN)) {
            returnStmt();
        } else if (check(TipoToken.WHILE)) {
            whileStmt();
        } else if (check(TipoToken.IZQ_LLAVE)) {
            block();
        } else {
            exprStmt(); // expresión seguida de punto y coma
        }
    }

    private void exprStmt() {
        expression();
        consume(TipoToken.PUNTO_COMA, "Se esperaba ';' al final de la expresión");
    }

    private void forStmt() {
        consume(TipoToken.FOR, "Se esperaba 'for'");
        consume(TipoToken.IZQ_PARENTESIS, "Se esperaba '(' después de 'for'");

        // FOR_STMT_INIT
        if (check(TipoToken.VAR)) {
            varDecl();
        } else if (check(TipoToken.PUNTO_COMA)) {
            consume(TipoToken.PUNTO_COMA, "Se esperaba ';' después de init vacío");
        } else {
            exprStmt();
        }

        // FOR_STMT_COND
        if (!check(TipoToken.PUNTO_COMA)) {
            expression();
        }
        consume(TipoToken.PUNTO_COMA, "Se esperaba ';' después de la condición");

        // FOR_STMT_INC
        if (!check(TipoToken.DER_PARENTESIS)) {
            expression();
        }
        consume(TipoToken.DER_PARENTESIS, "Se esperaba ')' después de los incrementos");

        // cuerpo del for
        statement();
    }

    private void ifStmt() {
        consume(TipoToken.IF, "Se esperaba 'if'");
        consume(TipoToken.IZQ_PARENTESIS, "Se esperaba '(' después de 'if'");
        expression(); // condición
        consume(TipoToken.DER_PARENTESIS, "Se esperaba ')' después de la condición");
        statement(); // cuerpo del if
    }

    private void printStmt() {
        consume(TipoToken.PRINT, "Se esperaba 'print'");
        expression();
        consume(TipoToken.PUNTO_COMA, "Se esperaba ';' después de la impresión");
    }

    private void whileStmt() {
        consume(TipoToken.WHILE, "Se esperaba 'while'");
        consume(TipoToken.IZQ_PARENTESIS, "Se esperaba '(' después de 'while'");
        expression(); // condición
        consume(TipoToken.DER_PARENTESIS, "Se esperaba ')' después de la condición");
        statement(); // cuerpo
    }

    private void returnStmt() {
        consume(TipoToken.RETURN, "Se esperaba 'return'");
        if (!check(TipoToken.PUNTO_COMA)) {
            expression(); // retorno opcional
        }
        consume(TipoToken.PUNTO_COMA, "Se esperaba ';' después de return");
    }

    private void expression() {
        // expresión simplificada por ahora: solo acepta literales o identificadores
        if (check(TipoToken.IDENTIFICADOR) || check(TipoToken.INT) || check(TipoToken.FLOAT) || check(TipoToken.DOUBLE) || check(TipoToken.TRUE) || check(TipoToken.FALSE)) {
            advance();
        } else {
            throw error(peek(), "Se esperaba una expresión");
        }
    }

    // funciones del parser

    private boolean check(TipoToken tipo) {
        return !isAtEnd() && peek().getTipo() == tipo;
    }

    private Token consume(TipoToken tipo, String mensaje) {
        if (check(tipo)) return advance();
        throw error(peek(), mensaje);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().getTipo() == TipoToken.EOF;
    }

    // mensaje personalizado para cualquier error
    private RuntimeException error(Token token, String mensaje) {
        return new RuntimeException("Error sintáctico en la línea " + token.getLinea() + ": " + mensaje + ". Token recibido: " + token.getTipo());
    }
}
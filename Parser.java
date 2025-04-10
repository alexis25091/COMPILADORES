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
        if (check(TipoToken.IDENTIFICADOR)) {
            consume(TipoToken.IDENTIFICADOR, "Se esperaba nombre del parámetro");
            while (match(TipoToken.COMA)) {
                consume(TipoToken.IDENTIFICADOR, "Se esperaba nombre del parámetro");
            }
        }
    }



    private void block() {
        consume(TipoToken.IZQ_LLAVE, "Se esperaba '{'");
        while (!check(TipoToken.DER_LLAVE) && !isAtEnd()) {
            declaration(); // para declaraciones dentro del bloque
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


        if (check(TipoToken.VAR)) {
            varDecl();
        } else if (check(TipoToken.PUNTO_COMA)) {
            consume(TipoToken.PUNTO_COMA, "Se esperaba ';' después de init vacío");
        } else {
            exprStmt();
        }


        if (!check(TipoToken.PUNTO_COMA)) {
            expression();
        }
        consume(TipoToken.PUNTO_COMA, "Se esperaba ';' después de la condición");


        forStmtInc();

        consume(TipoToken.DER_PARENTESIS, "Se esperaba ')' después de los incrementos");
        statement();
    }

    private void forStmtInc() {
        if (!check(TipoToken.DER_PARENTESIS)) {
            expression();
        }

    }

    private void ifStmt() {
        consume(TipoToken.IF, "Se esperaba 'if'");
        consume(TipoToken.IZQ_PARENTESIS, "Se esperaba '(' después de 'if'");
        expression();
        consume(TipoToken.DER_PARENTESIS, "Se esperaba ')' después de la condición");
        statement();
        elseStmt();
    }

    private void elseStmt() {
        if (check(TipoToken.ELSE)) {
            advance();
            statement();
        }

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
            expression();
        }
        consume(TipoToken.PUNTO_COMA, "Se esperaba ';' después de return");
    }

    // EXPRESIONES

    private void expression() {
        assignment();
    }

    private void assignment() {
        logicOr();

        if (match(TipoToken.IGUAL)) {
            expression(); // lado derecho de la asignación
        }
    }

    private void logicOr() {
        logicAnd();
        while (match(TipoToken.OR)) {
            logicAnd(); // LOGIC_OR’
        }
    }

    private void logicAnd() {
        equality();
        while (match(TipoToken.AND)) {
            equality(); // LOGIC_AND’
        }
    }

    private void equality() {
        comparison();
        while (match(TipoToken.IGUALIGUAL, TipoToken.DISTINTO)) {
            comparison(); // == o !=
        }
    }

    private void comparison() {
        term();
        while (match(TipoToken.MAYORQUE, TipoToken.MAYORIGUAL, TipoToken.MENORQUE, TipoToken.MENORIGUAL)) {
            term();
        }
    }

    private void term() {
        factor();
        while (match(TipoToken.MAS, TipoToken.MENOS)) {
            factor();
        }
    }

    private void factor() {
        unary();
        while (match(TipoToken.PRODUCTO, TipoToken.ENTRE)) {
            unary();
        }
    }

    private void unary() {
        if (match(TipoToken.NOT, TipoToken.MENOS)) {
            unary(); // recursivo para unarios
        } else {
            call();
        }
    }

    private void call() {
        primary(); // parte izquierda de la llamada

        while (true) {
            if (match(TipoToken.IZQ_PARENTESIS)) {
                arguments();
                consume(TipoToken.DER_PARENTESIS, "Se esperaba ')' después de los argumentos");
            } else {
                break;
            }
        }
    }

    private void arguments() {
        if (!check(TipoToken.DER_PARENTESIS)) {
            expression();
            while (match(TipoToken.COMA)) {
                expression();
            }
        }
    }

    private void primary() {
        if (match(TipoToken.TRUE, TipoToken.FALSE, TipoToken.NULL, TipoToken.INT, TipoToken.FLOAT, TipoToken.CADENA)) {
            return;
        }
        if (match(TipoToken.IDENTIFICADOR)) {
            return;
        }
        if (match(TipoToken.IZQ_PARENTESIS)) {
            expression();
            consume(TipoToken.DER_PARENTESIS, "Se esperaba ')' después de la expresión agrupada");
            return;
        }
        throw error(peek(), "Se esperaba una expresión primaria");
    }

    private boolean match(TipoToken... tipos) {
        for (TipoToken tipo : tipos) {
            if (check(tipo)) {
                advance();
                return true;
            }
        }
        return false;
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
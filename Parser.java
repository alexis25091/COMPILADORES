import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0; // apuntador al token actual

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public boolean parse() {
        // llamamos a la regla principal
        program();

        // verificamos si hemos llegado al EOF
        if (isAtEnd()) {
            System.out.println("parseo completado correctamente");
            return true;
        } else {
            System.out.println("error: no se consumieron todos los tokens");
            return false;
        }
    }

    private void program() {
        System.out.println("regla <program> llamada");

        // mientras no lleguemos al final solo avanzamos el apuntador
        while (!isAtEnd()) {
            advance(); // mas adelante aqua poner reglas como declaracion(), sentencia(), etc.
        }
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || peek().getTipo() == TipoToken.EOF;
    }


    private Token peek() {
        if (current >= tokens.size()) {
            // Evita que intente acceder mas del final
            return tokens.get(tokens.size() - 1); // regresamos EOF aunque no este tan bonito
        }
        return tokens.get(current);
    }


    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}

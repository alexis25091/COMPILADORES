public class Token {
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

    public TipoToken getTipo() { return tipo; }
    public String getLexema() { return lexema; }
    public Object getLiteral() { return literal; }
    public int getLinea() { return linea; }

    public String toString() {
        return "< " + tipo + ", lexema: " + lexema + ", literal: " + literal + ", linea: " + linea + " >";
    }
}
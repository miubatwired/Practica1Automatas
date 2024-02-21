public class Token {
    private String lexema;
    private int token;
    private int posTabla;
    private int linea;

    public Token(String lexema, int token, int posTabla) {
        this.lexema = lexema;
        this.token = token;
        this.posTabla = posTabla;
    }

    public Token(String lexema, int token) {
        this.lexema = lexema;
        this.token = token;
    }

    public Token(){

    }

    public Token(String lexema, int token, int posTabla, int linea) {
        this.lexema = lexema;
        this.token = token;
        this.posTabla = posTabla;
        this.linea = linea;
    }

    public Token(String lexema) {
        this.lexema = lexema;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public int getPosTabla() {
        return posTabla;
    }

    public void setPosTabla(int posTabla) {
        this.posTabla = posTabla;
    }

    public int getLinea() {
        return linea;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }

    public String toString(){
        return lexema + " " + token + "  " + posTabla + " " + linea;
    }
}

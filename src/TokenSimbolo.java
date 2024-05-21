public class TokenSimbolo extends Token {

    private Object valor;
    private String ambito;
    public TokenSimbolo(){

    }

    public TokenSimbolo(String id, int token, Object valor, String ambito, int posTabla) {
        super(id, token);
        this.ambito = ambito;
        this.valor = valor;
    }

    public TokenSimbolo(String id, int token, Object valor, String ambito) {
        super(id, token);
        this.ambito = ambito;
        this.valor = valor;
    }


    public Object getValor() {
        return valor;
    }

    public void setValor(Object valor) {
        this.valor = valor;
    }

    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }

    @Override
    public String toString() {
        return
                super.getLexema() +
                "\t" + super.getToken() +
                "\t" + valor +
                "\t" + ambito;
    }
}

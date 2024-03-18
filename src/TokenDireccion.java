public class TokenDireccion extends Token{
    private int VCI;

    public TokenDireccion(String lexema, int token, int linea, int VCI){
        super(lexema, token);
        super.setLinea(linea);
        this.VCI = VCI;
    }

    public int getVCI() {
        return VCI;
    }

    public void setVCI(int VCI) {
        this.VCI = VCI;
    }
    public String toString(){
        return super.getLexema() + " " + super.getToken() + " " + super.getLinea() + " " + VCI;
    }
}

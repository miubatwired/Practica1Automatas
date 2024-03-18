import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Vci {
    public static void main(String[] args) throws IOException {
        String nombreArchivo = "triangulo.txt";
        File archivoTokens = new File(nombreArchivo);
        int inicio=0;
        List<Token> tablaTokens;
        List<Token> vci = new ArrayList<>();
        Stack<Token> pilaOp = new Stack<>();
        Stack<Token> pilaEstatuso = new Stack<>();
        Stack<Token> pilaDirecciones = new Stack<>();
        tablaTokens = AnalisisSemantico.procesarArchivo(archivoTokens);
        while(tablaTokens.get(inicio).getToken()!=-2){
            inicio++;
        }
        for (int i=inicio; i < tablaTokens.size(); i++) {
            Token tokenActual = tablaTokens.get(i);
            if(esOperador(tokenActual)){

            }else if(esEstructuraControl(tokenActual)){

            }else{
                switch (tokenActual.getToken()){
                    case -73:
                        pilaOp.push(tokenActual);
                    case -74:
                        Token tokenOp = pilaOp.pop();
                        while(tokenOp.getToken()!=-74){
                            vci.add(tokenOp);
                            tokenOp=pilaOp.pop();
                        }
                    case -75:
                        vaciarPila(pilaOp, vci);
                    default:
                        vci.add(tokenActual);
                }
            }
        }
    }

    public static boolean esOperador(Token token){
        return token.getToken() <= -21 && token.getToken() >= -43;
    }

    public static boolean esEstructuraControl(Token token){
        return token.getToken() <=-16 && token.getToken() >=-10 || token.getToken()!=-16 || token.getToken()!=-17;
    }

    public static void vaciarPila(Stack<Token> pila, List<Token> vci){
        while(!pila.isEmpty()){
            Token token = pila.pop();
            vci.add(token);
        }
    }
}

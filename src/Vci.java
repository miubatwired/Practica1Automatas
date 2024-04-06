import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Vci {
    public static void main(String[] args) throws IOException {
        String nombreArchivo = "vcitest2";
        File archivoTokens = new File(nombreArchivo);
        int inicio=0;
        int direccion=0;
        List<Token> tablaTokens;
        List<Token> vci = new ArrayList<>();
        Stack<Token> pilaOp = new Stack<>();
        Stack<Token> pilaEstatutos = new Stack<>();
        Stack<Integer> pilaDirecciones = new Stack<>();
        tablaTokens = AnalisisSemantico.procesarArchivo(archivoTokens);
        while(tablaTokens.get(inicio).getToken()!=-2){
            inicio++;
        }
        for (int i=inicio; i < tablaTokens.size(); i++) {
            Token tokenActual = tablaTokens.get(i);
            if(esOperador(tokenActual)){
                manejarOperador(tokenActual, pilaOp, vci);
            }else if(esEstructuraControl(tokenActual)){
                //Estructura de control if
                if(tokenActual.getToken()==-6){
                    pilaEstatutos.push(tokenActual);
                }
                //entonces
                if(tokenActual.getToken()==-16){
                    vaciarPila(pilaOp, vci);
                    pilaDirecciones.push(vci.size());
                    System.out.println(vci.size());
                    vci.add(new Token("Token Falso"));
                    vci.add(tokenActual);
                }
                //sino
                if(tokenActual.getToken()==-7){
                    pilaEstatutos.push(tokenActual);
                    vci.set(pilaDirecciones.pop(),new Token("Apuntador", vci.size()+2));
                    pilaDirecciones.push(vci.size());
                    vci.add(new Token("Token Falso"));
                    vci.add(tokenActual);
                }
                //fin
                if(tokenActual.getToken()==-3){
                    if(!pilaEstatutos.isEmpty()){
                        Token fin = pilaEstatutos.pop();
                            if(fin.getToken() == -6 || fin.getToken() ==-7){
                                if(tablaTokens.get(i+1).getToken()==-7){
                                    continue;
                                }else{
                                    vci.set(pilaDirecciones.pop(), new Token("Apuntador", vci.size()));
                                }
                            }
                    }
                }
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
            System.out.println(pilaDirecciones.toString());
            System.out.println(pilaEstatutos.toString());
            System.out.println(vci.toString());
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
            vci.add(pila.pop());
        }
    }

    public static void manejarOperador(Token token, Stack<Token> pilaOp, List<Token> vci) {
        // Aquí puedes definir la lógica para manejar los operadores
        while (!pilaOp.isEmpty() && prioridad(pilaOp.peek()) >= prioridad(token)) {
            vci.add(pilaOp.pop());
        }
        pilaOp.push(token);
    }

    public static int prioridad(Token token) {
        // Define la prioridad de los operadores
        // Puedes ajustar estos valores según la precedencia de los operadores
        int prioridad = 0;
        switch (token.getToken()) {
            case -21: case -22: // + -
                prioridad = 1;
                break;
            case -23: case -24: // * /
                prioridad = 2;
                break;
            // Define más casos según sea necesario
        }
        return prioridad;
    }
}

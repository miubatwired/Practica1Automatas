import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Vci {
    public static void main(String[] args) throws IOException {
        String nombreArchivo = "vcitest2";
        File archivoTokens = new File(nombreArchivo);
        int inicio = 0;
        int direccion = 0;
        List<Token> tablaTokens;
        List<Token> vci = new ArrayList<>();
        Stack<Token> pilaOp = new Stack<>();
        Stack<Token> pilaEstatutos = new Stack<>();
        Stack<Integer> pilaDirecciones = new Stack<>();
        tablaTokens = AnalisisSemantico.procesarArchivo(archivoTokens);
        do{
            inicio++;
        } while (tablaTokens.get(inicio).getToken() != -2);
        for (int i = inicio; i < tablaTokens.size(); i++) {
            Token tokenActual = tablaTokens.get(i);
            if (esOperador(tokenActual)) {
                manejarOperador(tokenActual, pilaOp, vci);
            } else if (esEstructuraControl(tokenActual)) {
                //Estructura de control if
                switch (tokenActual.getToken()) {
                    case -6://si
                        pilaEstatutos.push(tokenActual);
                        break;
                    case -16://entonces
                        vaciarPila(pilaOp, vci);
                        pilaDirecciones.push(vci.size());
                        System.out.println(vci.size());
                        vci.add(new Token("Token Falso"));
                        vci.add(tokenActual);
                        break;
                    case -7:
                        pilaEstatutos.push(tokenActual);
                        vci.set(pilaDirecciones.pop(), new Token(String.valueOf(vci.size() + 2)));
                        pilaDirecciones.push(vci.size());
                        vci.add(new Token("Token Falso"));
                        vci.add(tokenActual);
                        break;
                }
            } else {
                switch (tokenActual.getToken()) {
                    case -73:
                        pilaOp.push(tokenActual);
                        break;
                    case -74:
                        Token tokenOp = pilaOp.pop();
                        while (tokenOp.getToken() != -73) {
                            vci.add(tokenOp);
                            tokenOp = pilaOp.pop();
                        }
                        break;
                    case -75:
                        vaciarPila(pilaOp, vci);
                        break;
                    case -3:
                        if (!pilaEstatutos.isEmpty()) { //fin
                            Token fin = pilaEstatutos.pop();
                            if (fin.getToken() == -6 || fin.getToken() == -7) {
                                if (tablaTokens.get(i + 1).getToken() == -7) {
                                    continue;
                                } else {
                                    vci.set(pilaDirecciones.pop(), new Token(String.valueOf(vci.size())));
                                }
                            }
                        }
                        break;
                }
                if(esConstante(tokenActual) || esVariable(tokenActual)){
                    vci.add(tokenActual);
                }
            }
            System.out.println(pilaOp.toString());
            System.out.println(pilaDirecciones.toString());
            System.out.println(pilaEstatutos.toString());
            System.out.println(vci.toString());
        }
        AnalisisSemantico.imprimirTabla(vci,"pruebaVCI");
    }

    public static boolean esOperador(Token token) {
        return token.getToken() <= -21 && token.getToken() >= -43;
    }

    public static boolean esConstante(Token token) {
        return token.getToken() <= -61 && token.getToken() >= -65;
    }

    public static boolean esVariable(Token token){
        return token.getToken() <= -51 && token.getToken() >= -54;
    }

    public static boolean esEstructuraControl(Token token) {
        return token.getToken() <= -6 && token.getToken() >= -10 || token.getToken() == -16 || token.getToken() == -17;
    }

    public static void vaciarPila(Stack<Token> pila, List<Token> vci) {
        while (!pila.isEmpty()) {
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
            case -22: // + -
                prioridad = 50;
                break;
            case -23:
            case -24: // * /
                prioridad = 60;
                break;
            // Define más casos según sea necesario
            case -33:
            case -31:
                prioridad = 40;
                break;
            case -43:
                prioridad = 30;
                break;
            case -41:
                prioridad = 20;
                break;
            case -42:
                prioridad = 10;
                break;
            case -26:
                prioridad = 0;
                break;
        }
        return prioridad;
    }
}

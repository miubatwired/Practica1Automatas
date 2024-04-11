import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Vci {
    public static void main(String[] args) throws IOException {
        long  tiempo = System.currentTimeMillis();
        String nombreArchivo = "whiletest";
        File archivoTokens = new File(nombreArchivo);
        int inicio = 0;
        int direccion = 0;
        Token temporal = null;
        List<Token> tablaTokens;
        List<Token> vci = new ArrayList<>();
        Stack<Token> pilaOp = new Stack<>();
        Stack<Token> pilaEstatutos = new Stack<>();
        Stack<Integer> pilaDirecciones = new Stack<>();
        tablaTokens = AnalisisSemantico.procesarArchivo(archivoTokens);
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
                    case -7://sino
                        pilaEstatutos.push(tokenActual);
                        vci.set(pilaDirecciones.pop(), new Token(String.valueOf(vci.size() + 2)));
                        pilaDirecciones.push(vci.size());
                        vci.add(new Token("Token Falso"));
                        vci.add(tokenActual);
                        break;
                    case -8: //mientras
                        pilaEstatutos.push(tokenActual);
                        pilaDirecciones.push(vci.size());
                        break;
                    case -17: //hacer
                        vaciarPila(pilaOp, vci);
                        pilaDirecciones.push(vci.size());
                        vci.add(new Token("Token Falso"));
                        vci.add(tokenActual);
                        break;
                    case -9: //repetir
                        pilaEstatutos.push(tokenActual);
                        pilaDirecciones.push(vci.size());
                        break;
                    case -10: //primera parte until
                        temporal = tokenActual;
                        break;
                }
            } else {
                //Segunda parte until
                if (temporal != null) {
                    if(tokenActual.getToken()==-75){
                        vci.add(new Token(String.valueOf(pilaDirecciones.pop())));
                        vci.add(temporal);
                        temporal = null;
                    }
                }
                switch (tokenActual.getToken()) {
                    case -75:
                        vaciarPila(pilaOp, vci);
                        break;
                    case -3:
                        if (!pilaEstatutos.isEmpty()) { //fin
                            Token fin = pilaEstatutos.pop();
                            switch (fin.getToken()){
                                case -6:
                                case -7:
                                    if (tablaTokens.get(i + 1).getToken() == -7) {
                                        continue;
                                    } else {
                                        vci.set(pilaDirecciones.pop(), new Token(String.valueOf(vci.size())));
                                    }
                                    break;
                                case -8:
                                    vci.set(pilaDirecciones.pop(), new Token(String.valueOf(vci.size()+2)));
                                    vci.add(new Token(String.valueOf(pilaDirecciones.pop())));
                                    vci.add(new Token("end-while"));
                                    break;
                            }
                        }
                        break;
                }
                if(esConstante(tokenActual) || esVariable(tokenActual) || esFuncion(tokenActual)){
                    vci.add(tokenActual);
                }
            }
        }
        long tiempoFinal = System.currentTimeMillis();
        tiempoFinal = tiempoFinal - tiempo;
        AnalisisSemantico.imprimirTabla(vci,"pruebaVCI");
        System.out.println(tiempoFinal);
    }

    public static boolean esOperador(Token token) {
        return token.getToken() <= -21 && token.getToken() >= -43 || token.getToken()==-73 || token.getToken()==-74;
    }

    public static boolean esFuncion(Token token){
        return token.getToken() == -4 || token.getToken() == -5;
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
        if (token.getToken() == -73) { // Si es paréntesis de apertura
            pilaOp.push(token); // Empujar a la pila de operadores
        } else if (token.getToken() == -74) { // Si es paréntesis de cierre
            // Sacar operadores de la pila hasta encontrar el paréntesis de apertura correspondiente
            while (!pilaOp.isEmpty() && pilaOp.peek().getToken() != -73) {
                vci.add(pilaOp.pop());
            }
            // Sacar el paréntesis de apertura de la pila
            if (!pilaOp.isEmpty()) {
                pilaOp.pop();
            }
        } else {
            // Si es otro operador
            while (!pilaOp.isEmpty() && prioridad(pilaOp.peek()) >= prioridad(token)) {
                vci.add(pilaOp.pop());
            }
            pilaOp.push(token);
        }
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

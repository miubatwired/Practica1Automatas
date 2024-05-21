import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class EjecucionVCI {
    public static void main(String[] args) throws IOException {
        List<Token> vci;
        List<TokenSimbolo> tablaSimbolos;
        List<TokenDireccion> tablaDirecciones;

        vci = AnalisisSemantico.procesarArchivo(new File("prueba"));
        tablaDirecciones = procesarTablaDirecciones(new File("Tabla de Direcciones.txt"));
        tablaSimbolos = procesarTablaSimbolos(new File("Tabla de Símbolos.txt"));

        int direccionVCI = tablaDirecciones.getFirst().getVCI();
        Stack<Object> pilaEjecucion = new Stack<>();
        for(int i = direccionVCI; i < vci.size(); i++) {
            Token token = vci.get(i);
            if(esConstante(token) || esVariable(token)) {
                pilaEjecucion.push(obtenerValor(token, tablaSimbolos));
            } else if(esOperador(token)) {
                Object valorOpDos = pilaEjecucion.pop();
                Object valorOpUno = pilaEjecucion.pop();
                Object resultado = ejecutarOperacion(token, valorOpUno, valorOpDos);
                pilaEjecucion.push(resultado);
            }
        }
        Object resultadoFinal = pilaEjecucion.pop();
        System.out.println("Resultado final: " + resultadoFinal);
    }

    public static Object obtenerValor(Token operando, List<TokenSimbolo> tablaSimbolos) {
        if(esVariable(operando)) {
            return tablaSimbolos.get(operando.getPosTabla()).getValor();
        } else {
            return operando.getLexema();
        }
    }

    public static List<TokenSimbolo> procesarTablaSimbolos(File archivo) throws IOException {
        List<TokenSimbolo> tabla = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while((linea = br.readLine()) != null) {
                String[] lineaToken = linea.trim().split("\\s+");
                TokenSimbolo token = new TokenSimbolo();
                switch (Integer.parseInt(lineaToken[1])) {
                    case -51 ->
                            token = new TokenSimbolo(lineaToken[0], Integer.parseInt(lineaToken[1]), Integer.parseInt(lineaToken[2]), lineaToken[3]);
                    case -52 ->
                            token = new TokenSimbolo(lineaToken[0], Integer.parseInt(lineaToken[1]), Float.parseFloat(lineaToken[2]), lineaToken[3]);
                    case -53 ->
                            token = new TokenSimbolo(lineaToken[0], Integer.parseInt(lineaToken[1]), lineaToken[2], lineaToken[3]);
                    case -54 ->
                            token = new TokenSimbolo(lineaToken[0], Integer.parseInt(lineaToken[1]), Boolean.parseBoolean(lineaToken[2]), lineaToken[3]);
                }
                tabla.add(token);
            }
        }
        return tabla;
    }

    public static List<TokenDireccion> procesarTablaDirecciones(File archivo) throws IOException {
        List<TokenDireccion> tabla = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while((linea = br.readLine()) != null) {
                String[] lineaToken = linea.trim().split("\\s+");
                TokenDireccion token = new TokenDireccion(lineaToken[0], Integer.parseInt(lineaToken[1]), Integer.parseInt(lineaToken[2]), Integer.parseInt(lineaToken[3]));
                tabla.add(token);
            }
        }
        return tabla;
    }

    public static boolean esConstante(Token token) {
        return token.getToken() <= -61 && token.getToken() >= -65;
    }

    public static boolean esVariable(Token token) {
        return token.getToken() <= -51 && token.getToken() >= -54;
    }

    public static boolean esOperador(Token token) {
        return token.getToken() <= -21 && token.getToken() >= -43 || token.getToken() == -73 || token.getToken() == -74;
    }

    public static Object ejecutarOperacion(Token operador, Object op1, Object op2) {
        switch (operador.getToken()) {
            case -21: // *
                return (int) op1 * (int) op2;
            case -22: // /
                return (int) op1 / (int) op2;
            case -23: // %
                return (int) op1 % (int) op2;
            case -24: // +
                return (int) op1 + (int) op2;
            case -25: // -
                return (int) op1 - (int) op2;
            case -26: // =
                return op1.equals(op2);
            case -31: // <
                return (int) op1 < (int) op2;
            case -32: // <=
                return (int) op1 <= (int) op2;
            case -33: // >
                return (int) op1 > (int) op2;
            case -34: // >=
                return (int) op1 >= (int) op2;
            case -35: // ==
                return op1.equals(op2);
            case -36: // !=
                return !op1.equals(op2);
            case -41: // &&
                return (boolean) op1 && (boolean) op2;
            case -42: // ||
                return (boolean) op1 || (boolean) op2;
            case -43: // !
                return !(boolean) op1;
            default:
                return null;
        }
    }
}

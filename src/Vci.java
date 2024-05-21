import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Vci {
    public static void main(String[] args) throws IOException {
        String nombreArchivo = "vcitest";
        File archivoTokens = new File(nombreArchivo);
        int inicio = 0;
        int direccion = 0;
        Token temporal = null;
        List<Token> tablaTokens;
        List<Token> vci = new ArrayList<>();
        Stack<Token> pilaOp = new Stack<>();
        Stack<Token> pilaEstatutos = new Stack<>();
        Stack<Integer> pilaDirecciones = new Stack<>();
        tablaTokens = procesarArchivo(archivoTokens);
        do{
            inicio++;
        }while(tablaTokens.get(inicio).getToken()!=-2);
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
                //(Cuando encuentre el paréntesis significa que la expresión a evaluar ha terminado)
                if (temporal != null) {
                    if(tokenActual.getToken()==-75){
                        vci.add(new Token(String.valueOf(pilaDirecciones.pop())));
                        vci.add(temporal);
                        temporal = null;
                    }
                }
                switch (tokenActual.getToken()) {
                    //Si es punto y coma se vacía la pila
                    case -75:
                        vaciarPila(pilaOp, vci);
                        break;
                    case -3:
                        //Se verifica a que estructura de control le pertenece el fin
                        if (!pilaEstatutos.isEmpty()) { //fin
                            Token fin = pilaEstatutos.pop();
                            switch (fin.getToken()){
                                //si/sino
                                case -6:
                                case -7:
                                    if (tablaTokens.get(i + 1).getToken() == -7) {
                                        continue;
                                    } else {
                                        vci.set(pilaDirecciones.pop(), new Token(String.valueOf(vci.size())));
                                    }
                                    break;
                                case -8: //mientras
                                    vci.set(pilaDirecciones.pop(), new Token(String.valueOf(vci.size()+2)));
                                    vci.add(new Token(String.valueOf(pilaDirecciones.pop())));
                                    vci.add(new Token("end-while"));
                                    break;
                                case -9: //repeat
                                    continue;
                            }
                        }
                        break;
                }
                //Si es constante, variable o es una función (escribir o leer) también se agrega
                if(esConstante(tokenActual) || esVariable(tokenActual) || esFuncion(tokenActual)){
                    vci.add(tokenActual);
                }
            }
        }
            imprimirTabla(vci, "VCI");
    }

    /***
     *
     * @param token
     * @return True si es un operador, False si no lo es
     */
    public static boolean esOperador(Token token) {
        return token.getToken() <= -21 && token.getToken() >= -43 || token.getToken()==-73 || token.getToken()==-74;
    }

    /**
     *
     * @param token
     * @return True si es una función, False si no lo es
     */
    public static boolean esFuncion(Token token){
        return token.getToken() == -4 || token.getToken() == -5;
    }

    /**
     *
     * @param token
     * @return True si es una constante, False si no lo es
     */
    public static boolean esConstante(Token token) {
        return token.getToken() <= -61 && token.getToken() >= -65;
    }

    /**
     *
     * @param token
     * @return True si es una variable, False si no lo es
     */
    public static boolean esVariable(Token token){
        return token.getToken() <= -51 && token.getToken() >= -54;
    }

    /**
     *
     * @param token
     * @return True si es una constante, False si no lo es
     */
    public static boolean esEstructuraControl(Token token) {
        return token.getToken() <= -6 && token.getToken() >= -10 || token.getToken() == -16 || token.getToken() == -17;
    }

    /**
     *
     * @param pila Una pila la cual se vaciará
     * @param vci  El VCI en el que se meterá todo lo de la pila
     */
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


    /**
     *
     * @param token Un token operador
     * @return La prioridad del token
     */
    public static int prioridad(Token token) {
        int prioridad = 0;
        switch (token.getToken()) {
            case -21: // *
            case -22: // /
            case -23: // %
                prioridad = 60;
                break;
            case -24: // +
            case -25: // -
                prioridad = 50;
                break;
            case -26: // =
                prioridad = 0;
                break;
            case -31: // <
            case -32: // <=
            case -33: // >
            case -34: // >=
            case -35: // ==
            case -36: // !=
                prioridad = 40;
                break;
            case -41: // &&
                prioridad = 20;
                break;
            case -42: // ||
                prioridad = 10;
                break;
            case -43: // !
                prioridad = 30;
                break;
        }
        return prioridad;
    }

    /***
     *  Procesa el archivo en un ArrayList de Tokens
     * @param archivo Un archivo que contenga una tabla de tokens
     * @return Lista que contiene como tipo Token los tokens de la tabla de tokens
     * @throws IOException
     */
    public static List<Token> procesarArchivo(File archivo) throws IOException {
        List<Token> tablaTokens = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(archivo));
        //Patrón para encontrar strings entre comillas en el lexema
        Pattern pattern = Pattern.compile("\".*\"");
        String linea;
        while ((linea = br.readLine()) != null) {
            Matcher matcher = pattern.matcher(linea);
            boolean matchFound = matcher.find();
            Token token;
            if (matchFound) {
                linea = linea.replace(matcher.group(0), "");
                String[] lineaToken = linea.trim().split("\\s+");
                token = new Token(matcher.group(), Integer.parseInt(lineaToken[0]), Integer.parseInt(lineaToken[1]),
                        Integer.parseInt(lineaToken[2]));
            } else {
                String[] lineaToken = linea.trim().split("\\s+");
                token = new Token(lineaToken[0], Integer.parseInt(lineaToken[1]), Integer.parseInt(lineaToken[2]),
                        Integer.parseInt(lineaToken[3]));
            }
            tablaTokens.add(token);
        }
        br.close();
        return tablaTokens;
    }

    /***
     * Método que imprime una tabla de tokens
     *
     * @param lista ArrayList que contenga objetos Token
     * @param nombre Nombre del archivo
     * @throws IOException
     */
    public static void imprimirTabla(List<Token> lista, String nombre) throws IOException {
        File file = new File(nombre);
        FileWriter writer = new FileWriter(file);
        for (Token token : lista) {
            writer.write(token.toString());
            writer.write("\n");
        }
        writer.close();
    }
}
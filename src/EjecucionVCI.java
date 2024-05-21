import java.io.*;
import java.util.*;

public class EjecucionVCI {
    public static void main(String[] args) throws IOException {
        List<Token> vci;
        List<TokenSimbolo> tablaSimbolos;
        List<TokenDireccion> tablaDirecciones;
        vci = AnalisisSemantico.procesarArchivo(new File("prueba"));
        tablaDirecciones = procesarTablaDirecciones(new File("Tabla de Direcciones.txt"));
        tablaSimbolos = procesarTablaSimbolos(new File("Tabla de Símbolos.txt"));
        System.out.println("Tabla de Símbolos antes de ejecución:");
        imprimirTablaAPantalla(tablaSimbolos);
        int direccionVCI = tablaDirecciones.getFirst().getVCI();
        Stack<Token> pilaEjecucion = new Stack<>();
        boolean funcion = false;
        for(int i = direccionVCI; i < vci.size(); i++) {
            Token token = vci.get(i);
            if((esConstante(token) || esVariable(token)) && !funcion) {
                pilaEjecucion.push(token);
            } else if(esOperador(token)) {
                Token operandoDos = pilaEjecucion.pop();
                Token operando = pilaEjecucion.pop();
                Object op1 = obtenerValor(operando, tablaSimbolos);
                Object op2 = obtenerValor(operandoDos, tablaSimbolos);
                int tipo = operando.getToken();
                Token resultado = ejecutarOperacion(token, op1, op2, tipo);
                if(resultado!=null){
                    pilaEjecucion.push(resultado);
                }else if(token.getToken()==-26){
                    tablaSimbolos.get(operando.getPosTabla()).setValor(obtenerValor(operandoDos,tablaSimbolos));
                }
            } else if(esFuncion(token)){
                pilaEjecucion.push(token);
                funcion = true;
            }else if(funcion){
                Token io = pilaEjecucion.pop();
                if(io.getToken()==-4){
                    System.out.print("Inserte el dato: ");
                    Scanner sc = new Scanner(System.in);
                    String valor = sc.next();
                    try {
                        switch (token.getToken()) {
                            case -51:
                                tablaSimbolos.get(token.getPosTabla()).setValor(Integer.parseInt(valor));
                                break;
                            case -52:
                                tablaSimbolos.get(token.getPosTabla()).setValor(Float.parseFloat(valor));
                                break;
                            case -53:
                                tablaSimbolos.get(token.getPosTabla()).setValor(valor);
                                break;
                            case -54:
                                tablaSimbolos.get(token.getPosTabla()).setValor(Boolean.parseBoolean(valor));
                                break;
                        }
                    }catch(Exception e){
                        System.out.println("Dato ingresado de tipo incorrecto");
                        System.exit(0);
                    }
                }else if(io.getToken()==-5){
                    System.out.println(obtenerValor(token, tablaSimbolos));
                }
            }
        }
        System.out.println("Tabla de Símbolos después de la ejecución:");
        imprimirTablaAPantalla(tablaSimbolos);
        imprimirTabla(tablaSimbolos, "Tabla de Símbolos.txt");
    }

    public static Object obtenerValor(Token operando, List<TokenSimbolo> tablaSimbolos) {
        if(esVariable(operando)) {
            return tablaSimbolos.get(operando.getPosTabla()).getValor();
        } else if(operando.getToken()==-61){
            return Integer.parseInt(operando.getLexema());
        } else if (operando.getToken()==-62) {
            return Float.parseFloat(operando.getLexema());
        } else if (operando.getToken()==-64) {
            return Boolean.parseBoolean(operando.getLexema());
        }else{
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

    public static Token ejecutarOperacion(Token operador, Object op1, Object op2, int tipo) {
        boolean res;
        if(tipo==-51 || tipo==-61){
            switch(operador.getToken()){
                case -21: // *
                    return new Token((int) op1 * (int) op2 + "",-61,-1,0);
                case -22: // /
                    try{
                        return new Token((int) op1 / (int) op2 + "",-61,-1,0);
                    }catch(ArithmeticException e){
                        System.out.println("No se puede dividir por 0 en linea " + operador.getLinea());
                        System.exit(0);
                    }
                    break;
                case -23: // %
                    return new Token((int) op1 % (int) op2 + "",-61,-1,0);
                case -24: // +
                    return new Token((int) op1 + (int) op2 + "",-61,-1,0);
                case -25: // -
                    return new Token((int) op1 - (int) op2 + "",-61,-1,0);
                case -31: // <
                    res = (int) op1 < (int) op2;
                    return new Token(String.valueOf(res),-61,-1,0);
                case -32: // <=
                    res = (int) op1 <= (int) op2;
                    return new Token(String.valueOf(res),-61,-1,0);
                case -33: // >
                    res = (int) op1 > (int) op2;
                    return new Token(String.valueOf(res),-61,-1,0);
                case -34: // >=
                    res = (int) op1 >= (int) op2;
                    return new Token(String.valueOf(res),-61,-1,0);
                case -35: // ==
                    res =  (int)op1 ==  (int)op2;
                    return new Token(String.valueOf(res),-61,-1,0);
                case -36: // !=
                    res =  (int)op1 !=  (int)op2;
                    return new Token(String.valueOf(res),-61,-1,0);
            }
        }else if(tipo==-52 || tipo==-62){
            switch(operador.getToken()){
                case -21: // *
                    return new Token( (float) op1 *  (float) op2 + "",-62,-1,0);
                case -22: // /
                   try{
                        return new Token( (float) op1 /  (float) op2 + "",-62,-1,0);
                    }catch(ArithmeticException e){
                       System.out.println("No se puede dividir por 0 en línea " + operador.getLinea());
                       System.exit(0);
                    }
                   break;
                case -23: // %
                    return new Token( (float) op1 %  (float) op2 + "",-62,-1,0);
                case -24: // +
                    return new Token( (float) op1 +  (float) op2 + "",-62,-1,0);
                case -25: // -
                    return new Token( (float) op1 -  (float) op2 + "",-62,-1,0);
                case -31: // <
                    res =  (float) op1 <  (float) op2;
                    return new Token(String.valueOf(res),-62,-1,0);
                case -32: // <=
                    res =  (float) op1 <=  (float) op2;
                    return new Token(String.valueOf(res),-62,-1,0);
                case -33: // >
                    res =  (float) op1 >  (float) op2;
                    return new Token(String.valueOf(res),-62,-1,0);
                case -34: // >=
                    res =  (float) op1 >=  (float) op2;
                    return new Token(String.valueOf(res),-62,-1,0);
                case -35: // ==
                    res =  (float)op1 ==  (float)op2;
                    return new Token(String.valueOf(res),-62,-1,0);
                case -36: // !=
                    res =  (float)op1 !=  (float)op2;
                    return new Token(String.valueOf(res),-62,-1,0);
            }
        }else{
            switch (operador.getToken()) {
                case -35: // ==
                    res = (boolean)op1 == (boolean)op2;
                    return new Token(String.valueOf(res),-64,-1,0);
                case -36: // !=
                    res = (boolean)op1 != (boolean)op2;
                    return new Token(String.valueOf(res),-64,-1,0);
                case -41: // &&
                    res = (boolean)op1 && (boolean) op2;
                    return new Token(String.valueOf(res),-64,-1,0);
                case -42: // ||
                    res = (boolean)op1 || (boolean) op2;
                    return new Token(String.valueOf(res),-64,-1,0);
            }
        }
        return null;
    }

    public static boolean esFuncion(Token token){
        return token.getToken() == -4 || token.getToken() == -5;
    }

    public static void imprimirTabla(List<TokenSimbolo> lista, String nombre) throws IOException {
        File file = new File(nombre);
        FileWriter writer = new FileWriter(file);
        for (Token token : lista) {
            writer.write(token.toString());
            writer.write("\n");
        }
        writer.close();
    }

    public static void imprimirTablaAPantalla(List<TokenSimbolo> lista)  {
        for (Token token : lista) {
            System.out.println(token.toString());
        }
    }
}
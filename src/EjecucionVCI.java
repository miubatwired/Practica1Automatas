import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EjecucionVCI {
    public static void main(String[] args) throws IOException {
        List<Token> vci;
        List<TokenSimbolo> tablaSimbolos;
        List<TokenDireccion> tablaDirecciones;
        //Se leen los archivos
        vci = procesarArchivo(new File("VCI.txt"));
        tablaDirecciones = procesarTablaDirecciones(new File("Tabla de Direcciones.txt"));
        String simbolos = "Tabla de Símbolos";
        tablaSimbolos = procesarTablaSimbolos(new File(simbolos));
        System.out.println("Tabla de Símbolos antes de ejecución:");
        imprimirTablaAPantalla(tablaSimbolos);
        System.out.println("\nPantalla");
        //Se obtiene la dirección del vci en el que empieza el programa desde la tabla de direcciones
        int direccionVCI = tablaDirecciones.getFirst().getVCI();
        Stack<Token> pilaEjecucion = new Stack<>();
        boolean funcion = false;
        for(int i = direccionVCI; i < vci.size(); i++) {
            Token token = vci.get(i);
            //Se añade a la pila si es constante o es variable o  (exceptuando cuando se va a imprimir o leer)
            if((esConstante(token) || esVariable(token) || token.getToken()==0) && !funcion && !token.getLexema().equals("end-while")) {
                pilaEjecucion.push(token);
            } else if(esOperador(token)) {
                //Si es operador se hace pop dos veces
                Token operandoDos = pilaEjecucion.pop();
                Token operando = pilaEjecucion.pop();
                //Se obtiene su valor con su tipo
                Object op1 = obtenerValor(operando, tablaSimbolos);
                Object op2 = obtenerValor(operandoDos, tablaSimbolos);
                int tipo = operando.getToken();
                //Se realiza la operación
                Token resultado = ejecutarOperacion(token, op1, op2, tipo);
                //Si no es null el resultado se añade a la pila
                if(resultado!=null){
                    pilaEjecucion.push(resultado);
                }else if(token.getToken()==-26){ //si fue null significa que fue un operador de asignación
                    //Se le asigna el valor del operandoDos al operando uno en la TS
                    tablaSimbolos.get(operando.getPosTabla()).setValor(obtenerValor(operandoDos,tablaSimbolos));

                }
            } else if(esFuncion(token)){
                //Si es función se hace push y se utiliza la variable para saber cuándo hay que imprimir/leer
                pilaEjecucion.push(token);
                funcion = true;
            }else if(funcion){
                funcion = false;
                //Si se necesita hacer una función, se hace un pop para saber cuál
                Token io = pilaEjecucion.pop();
                if(io.getToken()==-4){
                    //Se lee usando scanner
                    System.out.print("Inserte el dato: ");
                    Scanner sc = new Scanner(System.in);
                    String valor = sc.next();
                    try {
                        //Se asigna a la variable con su tipo correcto
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
                        //Si el usuario ingresa un tipo que no corresponde marca error y termina el programa
                        System.out.println("Dato ingresado de tipo incorrecto");
                        System.exit(0);
                    }
                }else if(io.getToken()==-5){ //Si es un escribir simplemente lo lee de la TS y lo imprime
                    System.out.println(obtenerValor(token, tablaSimbolos));
                }
            }else if(token.getToken() == -16 || token.getToken()==-17 || token.getToken()==-10){
                // si es then/do/until
                int pc_aux=Integer.parseInt(pilaEjecucion.pop().getLexema());
                boolean vv = (boolean) obtenerValor(pilaEjecucion.pop(),tablaSimbolos);
                if(vv){
                    continue;
                }else{
                    i=pc_aux-1;
                }
            }else if(token.getToken() == -7 || token.getLexema().equals("end-while")){
                i = Integer.parseInt(pilaEjecucion.pop().getLexema())-1;
            }
        }
        System.out.println("\nTabla de Símbolos después de la ejecución:");
        imprimirTablaAPantalla(tablaSimbolos);
        imprimirTabla(tablaSimbolos, simbolos);
    }

    /***
     *
     * @param operando Un operando del cual se obtendrá el valor
     * @param tablaSimbolos La tabla de símbolos para buscar su valor en caso que sea necesario
     * @return regresa el valor real del token
     */
    public static Object obtenerValor(Token operando, List<TokenSimbolo> tablaSimbolos) {
        if(esVariable(operando)) {
            return tablaSimbolos.get(operando.getPosTabla()).getValor();
        } else if(operando.getToken()==-61){
            return Integer.parseInt(operando.getLexema());
        } else if (operando.getToken()==-62) {
            return Float.parseFloat(operando.getLexema());
        } else if (operando.getToken()==-64 || operando.getToken()==-65) {
            return Boolean.parseBoolean(operando.getLexema());
        }else{
            return operando.getLexema();
        }
    }

    /***
     *
     * @param archivo Un archivo que contiene la tabla de símbolos
     * @return Una lista que representa la tabla de símbolos de objetos TokenSimbolo
     * @throws IOException
     */
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

    /***
     *
     * @param archivo Un archivo que contiene la tabla de direcciones
     * @return  Una lista que representa la tabla de direcciones de objetos TokenDireccion
     * @throws IOException
     */
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

    //True si es constante
    public static boolean esConstante(Token token) {
        return token.getToken() <= -61 && token.getToken() >= -65;
    }

    //True si es variable
    public static boolean esVariable(Token token) {
        return token.getToken() <= -51 && token.getToken() >= -54;
    }

    //True si es operador
    public static boolean esOperador(Token token) {
        return token.getToken() <= -21 && token.getToken() >= -43 || token.getToken() == -73 || token.getToken() == -74;
    }

    /***
     *
     * @param operador El operador de la operación
     * @param op1 Operando uno de la operación
     * @param op2 Operando dos de la operación
     * @param tipo El tipo de la operación(Entera, Real o Lógica)
     * @return Un token con el resultado de la operación
     */
    public static Token ejecutarOperacion(Token operador, Object op1, Object op2, int tipo) {
        boolean res; //Resultado de la operación lógica
        if(tipo==-51 || tipo==-61){
            //Operaciones para todos las variables y constantes de tipo enteras
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
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -32: // <=
                    res = (int) op1 <= (int) op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -33: // >
                    res = (int) op1 > (int) op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -34: // >=
                    res = (int) op1 >= (int) op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -35: // ==
                    res =  (int)op1 ==  (int)op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -36: // !=
                    res =  (int)op1 !=  (int)op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
            }
        }else if(tipo==-52 || tipo==-62){
            //Operaciones para todos las variables y constantes de tipo reales
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
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -32: // <=
                    res =  (float) op1 <=  (float) op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -33: // >
                    res =  (float) op1 >  (float) op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -34: // >=
                    res =  (float) op1 >=  (float) op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -35: // ==
                    res =  (float)op1 ==  (float)op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -36: // !=
                    res =  (float)op1 !=  (float)op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
            }
        }else{
            //Operaciones para todos las variables y constantes de tipo lógicas
            switch (operador.getToken()) {
                case -35: // ==
                    res = (boolean)op1 == (boolean)op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -36: // !=
                    res = (boolean)op1 != (boolean)op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -41: // &&
                    res = (boolean)op1 && (boolean) op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
                case -42: // ||
                    res = (boolean)op1 || (boolean) op2;
                    if(res){
                        return new Token("true",-64,-1,0);
                    }else{
                        return new Token("false",-65,-1,0);
                    }
            }
        }
        return null;
    }

    //True si es función
    public static boolean esFuncion(Token token){
        return token.getToken() == -4 || token.getToken() == -5;
    }

    //Imprime la tabla a un archivo
    public static void imprimirTabla(List<TokenSimbolo> lista, String nombre) throws IOException {
        File file = new File(nombre);
        FileWriter writer = new FileWriter(file);
        for (Token token : lista) {
            writer.write(token.toString());
            writer.write("\n");
        }
        writer.close();
    }

    //Imprime la tabla a pantalla
    public static void imprimirTablaAPantalla(List<TokenSimbolo> lista)  {
        for (Token token : lista) {
            System.out.println(token.toString());
        }
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
}
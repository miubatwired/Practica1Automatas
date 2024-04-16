import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalisisSemantico {
    public static void main(String[] args) throws IOException {
        int posTabla = 0;
        int i = 0;
        //El archivo que tiene la tabla de Tokens se debe encontrar en la carpeta raíz del proyecto
        String nombreArchivo = "siEjemplo.txt";
        File archivoTokens = new File(nombreArchivo);
        File archivoSimbolos = new File("Tabla de Símbolos.txt");
        //ArrayList que almacenará los tokens
        List<Token> tablaTokens;
        //ArrayList que almacenará la tabla de direcciones
        List<Token> tablaDirecciones = new ArrayList<>();
        //Hashmap que almacenará la tabla de símbolos
        LinkedHashMap<String, TokenSimbolo> tablaSimbolos = new LinkedHashMap<>();
        //Procesa el archivo con la tabla de tokens
        tablaTokens = procesarArchivo(archivoTokens);
        Object[] tipo = new Object[2];
        // Busca el token 'programa' seguido por el nombre del programa que termina con '@'
        Token programaToken = encontrarProgramaToken(tablaTokens);
        try {
            if (programaToken != null) {
                tablaDirecciones.add(new TokenDireccion(programaToken.getLexema(), programaToken.getToken(), programaToken.getLinea(), 0));
                imprimirTabla(tablaDirecciones, "Tabla de Direcciones.txt");
            } else {
                throw new ErrorSemantico("No se encontró el token 'programa' seguido por el nombre del programa en la tabla de tokens.");
            }
        } catch (ErrorSemantico e) {
            System.out.println("Error: " + e.getMessage());
        }
        //Añade las variables declaradas a la tabla de símbolos
        while (!tablaTokens.get(i).getLexema().equals("inicio")) {
            Token token = tablaTokens.get(i);
            if (token.getToken() <= -11 && token.getToken() >= -14) {
                tipo[0] = token.getToken() - 40;
                tipo[1] = token.getLexema();
            }
            if (esVariable(token)) {
                String lexema = token.getLexema();
                int numToken = token.getToken();
                String nombreVariable = lexema.substring(0, lexema.length() - 1);
                TokenSimbolo tokenSimbolo;
                //Valida variables repetidas
                try {
                    if (tablaSimbolos.containsKey(nombreVariable)) {
                        throw new ErrorSemantico("Variable repetida con nombre " + token.getLexema() + " en la línea " +
                                token.getLinea());
                    }
                    if (token.getToken() != (int) tipo[0]) {
                        throw new ErrorSemantico(" variable " + token.getLexema() + " fue declarada como " + tipo[1] +
                                " pero por su carácter de control debería de ser " + declaracion(token));
                    }
                } catch (ErrorSemantico e) {
                    System.out.println("Error: " + e.getMessage());
                }
                //Asigna valores dependiendo el tipo de variable
                switch (numToken) {
                    case -51:
                        tokenSimbolo = new TokenSimbolo(lexema, numToken, 0, "Main", posTabla);
                        posTabla++;
                        tablaSimbolos.put(nombreVariable, tokenSimbolo);
                        break;
                    case -52:
                        tokenSimbolo = new TokenSimbolo(lexema, numToken, 0.0f, "Main", posTabla);
                        posTabla++;
                        tablaSimbolos.put(nombreVariable, tokenSimbolo);
                        break;
                    case -53:
                        tokenSimbolo = new TokenSimbolo(lexema, numToken, "Null", "Main", posTabla);
                        posTabla++;
                        tablaSimbolos.put(nombreVariable, tokenSimbolo);
                        break;
                    case -54:
                        tokenSimbolo = new TokenSimbolo(lexema, numToken, false, "Main", posTabla);
                        posTabla++;
                        tablaSimbolos.put(nombreVariable, tokenSimbolo);
                        break;
                }
            }
            i++;
        }
        //Modifica la tabla de tokens con su posición en la tabla de símbolos
        for (i = 0; i < tablaTokens.size(); i++) {
            Token token = tablaTokens.get(i);
            if (esVariable(token)) {
                String nombreVariable = token.getLexema();
                nombreVariable = nombreVariable.substring(0, nombreVariable.length() - 1);
                TokenSimbolo tokenTabla = tablaSimbolos.get(nombreVariable);
                try {
                    if (tokenTabla != null) {
                        tablaTokens.get(i).setPosTabla(tokenTabla.getPosTabla());
                    } else {
                        throw new ErrorSemantico("Variable " + tablaTokens.get(i).getLexema() + " no definida en línea" +
                                tablaTokens.get(i).getLinea());
                    }
                } catch (ErrorSemantico e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }else if(token.getToken()==-55){
                for(i=0; i<tablaDirecciones.size(); i++){
                    if(tablaDirecciones.get(i).getLexema().equals(token.getLexema())){
                        token.setPosTabla(i);
                    }
                }
            }
        }
        imprimirTabla(tablaTokens, nombreArchivo);
        //Imprime la tabla de símbolos a un archivo
        try (FileWriter writer = new FileWriter(archivoSimbolos)) {
            for (Token token : tablaSimbolos.values()) {
                writer.write(token.toString());
                writer.write("\n");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        //Verifica que los tipos de datos sean iguales
        boolean operacion = false;
        int tipoVariable = 0;
        for(i=0; i<tablaTokens.size();i++){
            Token token = tablaTokens.get(i);
            //Checa si el siguiente token es un operador
            if(esVariable(token)){
                if(tablaTokens.get(i+1).getToken()<=-26 && tablaTokens.get(i+1).getToken()>=-42){
                    //Si lo es, entonces estamos en una operación
                    operacion = true;
                }
                tipoVariable = token.getToken();
            }
            //Si es una operación entra al if
            if(operacion){
                try{
                    //Si es una variable
                    if(esVariable(token)){
                        //Si el token de la primer variable no es igual al token de la siguiente variable, entonces
                        //la operación no tiene los mismos tipos y tira una excepción
                        if(token.getToken() != tipoVariable){
                            throw new ErrorSemantico("La operación con la variable " + token.getLexema() +
                                    "tiene tipos incorrectos en línea " + token.getLinea());
                        }
                    //Valida con las constantes (si el token es una constante)
                    }else if(token.getToken() <=-61 && token.getToken()>=-62  || token.getToken()==-64){
                        //Si el token corresponde al token de la variable -10 (Se hace una resta -10 para crear una
                        //conexión entre el número de token de las variables y las constantes
                        if(token.getToken()!=tipoVariable-10) {
                            throw new ErrorSemantico("La operación en la línea " + token.getLinea() +
                                    " tiene tipos incorrectos ");
                        }
                    //Ya que la regla de restarle -10 para crear la conexión no se cumple con un false ya que las
                    //constantes lógicas son dos, se crea un caso especial para cuando es false (-65) para restarle -11
                    }else if(token.getToken()==-65){
                        if(token.getToken()!=tipoVariable-11){
                            throw new ErrorSemantico("La operación en la línea " + token.getLinea() +
                                    " tiene tipos incorrectos ");
                        }
                    }
                }catch (ErrorSemantico e){
                    System.out.println("Error: " + e.getMessage());
                }
            }
            //Si encuentra un punto y coma ahí termina la operación
            if(token.getToken()==-75){
                operacion = false;
            }
        }
    }

    // Encuentra el token 'programa' seguido por el nombre del programa que termina con '@'
    public static Token encontrarProgramaToken(List<Token> tokensList) {
        for (Token token : tokensList) {
            if (token.getLexema().equals("programa")) {
                int index = tokensList.indexOf(token) + 1;
                if (index < tokensList.size() && tokensList.get(index).getLexema().endsWith("@")) {
                    return tokensList.get(index);
                }
            }
        }
        return null;
    }

    public static String declaracion(Token token) {
        return switch (token.getToken()) {
            case -51 -> "entero";
            case -52 -> "real";
            case -53 -> "cadena";
            case -54 -> "lógico";
            default -> "";
        };
    }

    /***
     * Función para validar si un token es una variable
     * @param token un objeto Token
     * @return true si el token es una variable, false si no lo es
     */
    public static boolean esVariable(Token token) {
        return token.getToken() <= -51 && token.getToken() >= -54;
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

    public static class ErrorSemantico extends Exception {
        public ErrorSemantico(String mensaje) {
            super(mensaje);
        }
    }
}
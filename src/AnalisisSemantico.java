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
        File archivoTokens = new File("tablaTokens.txt");
        File archivoSimbolos = new File("Tabla de Símbolos.txt");
        //ArrayList que almacenará los tokens
        List<Token> tablaTokens;
        //Hashmap que almacenará la tabla de símbolos
        LinkedHashMap<String, TokenSimbolo> tablaSimbolos = new LinkedHashMap<>();
        //Procesa el archivo con la tabla de tokens
        tablaTokens = procesarArchivo(archivoTokens);
        Object[] tipo = new Object[2];
        //Añade las variables declaradas a la tabla de símbolos
        while (!tablaTokens.get(i).getLexema().equals("inicio")) {
            Token token = tablaTokens.get(i);
            if(token.getToken()<=-11 && token.getToken()>=-14){
                tipo[0] = token.getToken()-40;
                tipo[1] = token.getLexema();
            }
            if(esVariable(token)){
                String lexema = token.getLexema();
                int numToken = token.getToken();
                String nombreVariable = lexema.substring(0,lexema.length()-1);
                TokenSimbolo tokenSimbolo;
                //Valida variables repetidas
                if (tablaSimbolos.containsKey(nombreVariable)) {
                    System.out.println("ERROR: Variable repetida con nombre " + token.getLexema() + " en la línea " + token.getLinea());
                }
                if(token.getToken()!=(int)tipo[0] ){
                    System.out.println("ERROR: La variable " + token.getLexema() + " fue declarada como " + tipo[1] + " pero por su carácter de control debería de ser " + declaracion(token));
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
            if(esVariable(token)){
                String nombreVariable = token.getLexema();
                nombreVariable = nombreVariable.substring(0,nombreVariable.length()-1);
                TokenSimbolo tokenTabla = tablaSimbolos.get(nombreVariable);
                if (tokenTabla != null) {
                    tablaTokens.get(i).setPosTabla(tokenTabla.getPosTabla());
                } else{
                    System.out.println("ERROR: Variable " + tablaTokens.get(i).getLexema() + " no definida en línea" + tablaTokens.get(i).getLinea());
                }
            }
        }
        imprimirTabla(tablaTokens, "tablaTokens.txt");
        //Imprime la tabla de símbolos a un archivo
        try (FileWriter writer = new FileWriter(archivoSimbolos)) {
            for (Token token : tablaSimbolos.values()) {
                writer.write(token.toString());
                writer.write("\n");
            }
        }
    }

    public static String declaracion(Token token){
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
    public static boolean esVariable(Token token){
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
        Pattern pattern = Pattern.compile("\"[\\w+\\s*]*\"");
        String linea;
        while ((linea = br.readLine()) != null) {
            Matcher matcher = pattern.matcher(linea);
            boolean matchFound = matcher.find();
            Token token;
            if (matchFound) {
                linea = linea.replace(matcher.group(0), "");
                String[] lineaToken = linea.trim().split("\\s+");
                token = new Token(matcher.group(), Integer.parseInt(lineaToken[0]), Integer.parseInt(lineaToken[1]), Integer.parseInt(lineaToken[2]));
            } else {
                String[] lineaToken = linea.trim().split("\\s+");
                token = new Token(lineaToken[0], Integer.parseInt(lineaToken[1]), Integer.parseInt(lineaToken[2]), Integer.parseInt(lineaToken[3]));
            }
            tablaTokens.add(token);
        }
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
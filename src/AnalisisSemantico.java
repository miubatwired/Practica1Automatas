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
        File archivo = new File("tablaTokens.txt");
        File file = new File("Tabla de Símbolos.txt");
        //ArrayList que almacenará los tokens
        List<Token> tokensList = new ArrayList<>();
        //Hashtable que almacenará la tabla de símbolos
        LinkedHashMap<String, TokenSimbolo> tablaSimbolos = new LinkedHashMap<>();
        tokensList = procesarArchivo(archivo);
        //Añade las variables declaradas a la tabla de símbolos
        while (!tokensList.get(i).getLexema().equals("inicio")) {
            Token token = tokensList.get(i);
            TokenSimbolo tokenSimbolo = new TokenSimbolo();
            char tipo = token.getLexema().charAt(token.getLexema().length() - 1);
            if (tablaSimbolos.containsKey(token.getLexema())) {
                System.out.println("ERROR: Variable repetida con nombre " + token.getLexema() + " en la línea " + token.getLinea());
            }
            switch (tipo) {
                case '&':
                    modificarTokenTabla(token.getLexema(), -51, 0, tokenSimbolo);
                    tokenSimbolo.setPosTabla(posTabla);
                    posTabla++;
                    tablaSimbolos.put(token.getLexema(), tokenSimbolo);
                    break;
                case '%':
                    modificarTokenTabla(token.getLexema(), -52, 0.0f, tokenSimbolo);
                    tokenSimbolo.setPosTabla(posTabla);
                    posTabla++;
                    tablaSimbolos.put(token.getLexema(), tokenSimbolo);
                    break;
                case '$':
                    modificarTokenTabla(token.getLexema(), -53, "Null", tokenSimbolo);
                    tokenSimbolo.setPosTabla(posTabla);
                    posTabla++;
                    tablaSimbolos.put(token.getLexema(), tokenSimbolo);
                    break;
                case '#':
                    modificarTokenTabla(token.getLexema(), -54, false, tokenSimbolo);
                    tokenSimbolo.setPosTabla(posTabla);
                    posTabla++;
                    tablaSimbolos.put(token.getLexema(), tokenSimbolo);
                    break;
            }
            i++;
        }
        //Modifica la tabla de tokens con su posición en la tabla de símbolos
        for (i = 0; i < tokensList.size(); i++) {
            String id = tokensList.get(i).getLexema();
            TokenSimbolo tokenTabla = tablaSimbolos.get(id);
            if (tokenTabla != null) {
                tokensList.get(i).setPosTabla(tokenTabla.getPosTabla());
            } else if (tokensList.get(i).getToken() >= -54 && tokensList.get(i).getToken() <= -51) {
                System.out.println("ERROR: Variable " + tokensList.get(i).getLexema() + " no definida en línea" + tokensList.get(i).getLinea());
            }
        }
        imprimirTabla(tokensList, "tablaTokens.txt");
        //Imprime la tabla de símbolos a un archivo
        try (FileWriter writer = new FileWriter(file)) {
            for (Token token : tablaSimbolos.values()) {
                writer.write(token.toString());
                writer.write("\n");
            }
        }
    }

    public static void modificarTokenTabla(String lexema, int token, Object valor, TokenSimbolo tokenTabla) {
        tokenTabla.setLexema(lexema);
        tokenTabla.setToken(token);
        tokenTabla.setValor(valor);
        tokenTabla.setAmbito("Main");
    }

    /***
     *  Procesa el archivo en un ArrayList de Tokens
     * @param archivo
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
            Token token = new Token();
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
     * @param lista
     * @param nombre
     * @throws IOException
     */
    public static void imprimirTabla(List<Token> lista, String nombre) throws IOException {
        File file = new File(nombre);
        file.createNewFile();
        FileWriter writer = new FileWriter(file);
        for (Token token : lista) {
            writer.write(token.toString());
            writer.write("\n");
        }
        writer.close();
    }
}
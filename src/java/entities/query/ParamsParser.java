/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.query;

import java.util.HashMap;

/**
 *
 * @author Florian
 */
public class ParamsParser {
    private static final String COL = "col:";
    private static final String PARAM = "param:";
    
    private String params = "default";
    
    public ParamsParser() {
    }
    
    public ParamsParser(String params) {
        this.params = params;
    }
    
    public HashMap<String, String> parse() {
        HashMap<String, String> resmap = new HashMap<>();
        StringBuilder col;
        StringBuilder param;
        // valeur par défaut :
        if(params.equals("default") || params.equals("")) {
            return resmap; // map vide
        }
        // on crée un map (colonne, param) :
        for(int i = 0; i < params.length() && (i = extractNextValue(i, COL, col = new StringBuilder(25))) >= 0;) {
            // pour chaque colonne, on extrait la valeur de param :
            if((i = extractNextValue(i, PARAM, param = new StringBuilder(30))) < 0) {
                // valeur introuvable => erreur : on arrête ici et renvoie ce qu'on a déjà trouvé
                return resmap;
            }
            resmap.put(col.toString(), param.toString());
        }
        return resmap;
    }
    
    private int extractNextValue(int index, String key, StringBuilder buffer) {
        char c;
        char delimiter;
        
        index = params.indexOf(key, index);
        if(index < 0) {
            return index; // erreur : clé introuvable
        }
        index += key.length();
        c = 0;
        for(; index < params.length() && (c = params.charAt(index)) != '"' && c != '\''; ++index) { //skip to value delimiter " or '
            if(c == '}' || c == '{' || c == '[' || c == ']') {
                return -2; // erreur : valeur introuvable
            }
        }
        if(index >= params.length()) {
            return -2; // erreur : valeur introuvable
        }
        delimiter = c;
        c = 0;
        for(++index; index < params.length() && (c = params.charAt(index)) != delimiter; ++index) {
            buffer.append(c);
        }
        if(index >= params.length()) {
            return -2; // erreur : valeur introuvable
        }
        return index;
    }
}

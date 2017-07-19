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
    private static final String COL = "col:\"";
    private static final String PARAM = "param:\"";
    
    private String params = "default";
    
    public ParamsParser() {
    }
    
    public ParamsParser(String params) {
        this.params = params;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
    
    public HashMap<String, String> parse() {
        //params.trim(); // normalement pas utile, faire attention à ne pas insérer d'espace, mais en cas de besoin...
        HashMap<String, String> resmap = new HashMap<>();
        StringBuilder col;
        StringBuilder param;
        if(! params.equals("default")) {
            for(int i = 0; (i = params.indexOf(COL, i)) >= 0;) {
                col = new StringBuilder(25);
                param = new StringBuilder(25);
                i += COL.length();
                i = extractNextValue(i, col);
                if((i = params.indexOf(PARAM, i)) >= 0) {
                    i += PARAM.length();
                    i = extractNextValue(i, param);
                }
                if(col.length() > 0 && param.length() > 0) {
                    resmap.put(col.toString(), param.toString());
                }
            }
        }
        return resmap;
    }
    
    private int extractNextValue(int index, StringBuilder buffer) {
        for(; index < params.length() && params.charAt(index) != '"'; ++index) {
            buffer.append(params.charAt(index));
        }
        return index;
    }
}

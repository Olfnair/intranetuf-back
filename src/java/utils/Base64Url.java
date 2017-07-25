/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package utils;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 *
 * @author Florian
 */
public class Base64Url {
    @SuppressWarnings("empty-statement")
    public static String encode(String data, String charset) throws UnsupportedEncodingException {
        byte[] encoded = Base64.getEncoder().encode(data.getBytes(charset));
        int outputLen = encoded.length;
        for(int i = encoded.length - 1; i >= 0 && encoded[i] == '='; --i, outputLen--); // remove padding
        byte[] output = new byte[outputLen];
        for(int i = 0; i < outputLen; ++i) {
            switch(encoded[i]) {
                case '+':
                    output[i] = '-';
                    break;
                case '/':
                    output[i] = '_';
                    break;
                default:
                    output[i] = encoded[i];
            }
        }
        return new String(output, "ISO-8859-1");
    }
    
    public static String decode(String data) throws UnsupportedEncodingException {
        return decode(data, "ISO-8859-1");
    }
    
    @SuppressWarnings("empty-statement")
    public static String decode(String data, String charset) throws UnsupportedEncodingException {
       byte[] encoded = data.getBytes("ISO-8859-1");
       int inputLen = encoded.length;
       for(int i = encoded.length - 1; i >= 0 && (encoded[i] == '.' || encoded[i] == '='); --i, inputLen--); // remove padding
       byte[] input = new byte[inputLen];
       for(int i = 0; i < inputLen; ++i) {
           switch(encoded[i]) {
               case '-':
                   input[i] = '+';
                   break;
               case '_':
                   input[i] = '/';
                   break;
               default:
                   input[i] = encoded[i];
                   break;
           }
       }
       byte[] output = Base64.getDecoder().decode(input);
       return new String(output, charset);
    }
}

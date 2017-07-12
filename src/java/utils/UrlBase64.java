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
public class UrlBase64 {
    public static String encode(String data, String charset) throws UnsupportedEncodingException {
        byte base64Bytes[] = Base64.getEncoder().encode(data.getBytes(charset));
        for(int i = 0; i < base64Bytes.length; ++i) {
            switch(base64Bytes[i]) {
                case '+':
                    base64Bytes[i] = '-';
                    break;
                case '/':
                    base64Bytes[i] = '_';
                    break;
                case '=':
                    base64Bytes[i] = '.';
                    break;
                default:
                    break;
            }
        }
        return new String(base64Bytes, charset);
    }
    
    public static String decode(String data, String charset) throws UnsupportedEncodingException {
        byte base64Bytes[] = data.getBytes("ISO-8859-1");
        for(int i = 0; i < base64Bytes.length; ++i) {
            switch(base64Bytes[i]) {
                case '-':
                    base64Bytes[i] = '+';
                    break;
                case '_':
                    base64Bytes[i] = '/';
                    break;
                case '.':
                    base64Bytes[i] = '=';
                    break;
                default:
                    break;
            }
        }
        return new String(Base64.getDecoder().decode(base64Bytes), charset);
    }
}

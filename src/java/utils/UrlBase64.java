/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package utils;

import java.io.UnsupportedEncodingException;

/**
 *
 * @author Florian
 */
public class UrlBase64 {
    public final static char[] ALPHABET = {
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
        'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
        '0','1','2','3','4','5','6','7','8','9','-','_'
    };
    
    public final static char[] REVERSE_ALPHABET;
    
    static {
        REVERSE_ALPHABET = new char[128]; // 128 = taille de la table ascii, donc suffisant
        for(int i = 0; i < ALPHABET.length; ++i) {
            REVERSE_ALPHABET[ALPHABET[i]] = (char) i;
        }
        // juste pour être sur de ne pas avoir de problème avec les bytes de padding :
        REVERSE_ALPHABET['.'] = 0;
        REVERSE_ALPHABET['='] = 0;
    }
    
    public static String encode(String data, String charset) throws UnsupportedEncodingException {
        // fonction optimisée pour la vitesse d'execution du code (et pas forcément la lisibilité)
        int outputlen = (int)((double)(data.length()) * 1.4);
        StringBuilder output = new StringBuilder(outputlen);
        byte[] toEncode = data.getBytes(charset);
        int chunk = 0;
        int len = toEncode.length;
        
        int i = 0;
        for(; i + 2 < len; i += 3) {
            chunk = (toEncode[i] & 0xfc) << (24 - 2);
            chunk |= ((toEncode[i] & 0x03) << (16 + 4)) | ((toEncode[i + 1] & 0xf0) << (16 - 4));
            chunk |= ((toEncode[i + 1] & 0x0f) << (8 + 2)) | ((toEncode[i + 2] & 0xc0) << (8 - 6));
            chunk |= toEncode[i + 2] & 0x3f;
            for(int b = 0; b < 4; ++b) {
                output.append(ALPHABET[(chunk >> (24 - 8 * b)) & 0xff]);
            }
        }
        if(i >= len) {
            return output.toString();
        }
        int leftBytes = len - i; // bytes restants
        if(leftBytes == 2) { // reste 2 bytes à coder
            chunk = (toEncode[i] & 0xfc) << (24 - 2);
            chunk |= ((toEncode[i] & 0x03) << (16 + 4)) | ((toEncode[i + 1] & 0xf0) << (16 - 4));
            chunk |= (toEncode[i + 1] & 0x0f) << (8 + 2);
            // pas de padding, il faudra compter pour décoder
        }
        else if(leftBytes == 1) { // reste 1 byte à coder
            chunk = (toEncode[i] & 0xfc) << (24 - 2);
            chunk |= (toEncode[i] & 0x03) << (16 + 4);
            // pas de padding, il faudra compter pour décoder
        }
        for(int b = 0; b < leftBytes + 1; ++b) {
            output.append(ALPHABET[(chunk >> (24 - 8 * b)) & 0xff]);
        }
        return output.toString();
    }
    
    public static String decode(String data, String charset) throws UnsupportedEncodingException {
        byte[] toDecode = data.getBytes("ISO-8859-1");
        int len = toDecode.length;
        int mod = len % 4;
        int outputlen = (len / 4) * 3;
        if(mod == 0) {
            int padding = 0;
            for(int i = 0; i < 2 && i < len; ++i) {
                if(data.charAt(len - i - 1) == '.' || data.charAt(len - i - 1) == '=') {
                    ++padding;
                }
            }
            if(padding > 0) {
                outputlen -= 3; // on a compté un groupe de 4 en trop
                mod = 4 - padding;
            }
        }
        if(mod > 0) {
            outputlen += mod - 1;
        }
        
        int outputIndex = 0;
        byte[] output = new byte[outputlen];
        int chunk = 0;
        
        int i = 0;
        for(; i + 3 < len; i += 4) {
            chunk = REVERSE_ALPHABET[toDecode[i]] << (24 - 6);
            chunk |= REVERSE_ALPHABET[toDecode[i + 1]] << (24 - 12);
            chunk |= REVERSE_ALPHABET[toDecode[i + 2]] << (24 - 18);
            chunk |= REVERSE_ALPHABET[toDecode[i + 3]];
            for(int b = 0; b < 3; ++b) {
                byte c = (byte)((chunk >> (16 - b * 8)) & 0xff);
                if(c <= 0) {
                    break;
                }
                output[outputIndex++] = c;
            }
        }
        if(i >= len) {
            return new String(output, charset);
        }
        int leftBytes = len - i; // bytes restants
        switch (leftBytes) {
            case 3:
                // reste 3 bytes à décoder => 1 de padding
                chunk = REVERSE_ALPHABET[toDecode[i]] << (24 - 6);
                chunk |= REVERSE_ALPHABET[toDecode[i + 1]] << (24 - 12);
                chunk |= REVERSE_ALPHABET[toDecode[i + 2]] << (24 - 18);
                break;
            case 2:
                // reste 2 bytes à décoder => 2 de padding
                chunk = REVERSE_ALPHABET[toDecode[i]] << (24 - 6);
                chunk |= REVERSE_ALPHABET[toDecode[i + 1]] << (24 - 12);
                break;
            case 1:
                // 1 byte restant => erreur
                // on ignore ce byte restant..
                chunk = 0;
                break;
            default:
                break;
        }
        for(int b = 0; b < leftBytes - 1; ++b) {
            byte c = (byte)((chunk >> (16 - b * 8)) & 0xff);
            if(c <= 0) {
                break;
            }
            output[outputIndex++] = c;
        }
        return new String(output, charset);
    }
}

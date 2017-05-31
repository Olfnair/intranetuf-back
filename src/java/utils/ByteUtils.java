/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package utils;

/**
 *
 * @author Florian
 */
public class ByteUtils {
    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.SIZE / Byte.SIZE];
        for (int i = Long.SIZE / Byte.SIZE - 1; i >= 0; --i) {
            result[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return result;
    }
    
    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < Long.SIZE / Byte.SIZE; ++i) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}

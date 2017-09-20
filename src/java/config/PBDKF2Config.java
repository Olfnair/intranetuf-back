/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Florian
 */
public class PBDKF2Config extends Config {
    private final byte[] peper;
    private final int iterations;
    private final int outputLen;
    private final int saltLen;
    
    
    public PBDKF2Config(String filepath) {
        super(filepath);
        ConfigFile config = getConfigFile();
        
        // valeurs de secours
        int iterationsTmp = 102417;
        int outputLenTmp = 64;
        int saltLenTmp = 32;
        String peperTmp = "pLKvnBOx1V5qIxtblHUbdGQpTyifH0/Qbefppez0Kcg=";
        
        try {
            iterationsTmp = Integer.parseInt(config.read("iteration", Integer.toString(iterationsTmp)), 10);
            peperTmp = config.read("peper", peperTmp);
            outputLenTmp = Integer.parseInt(config.read("output_len", Integer.toString(outputLenTmp)), 10);
            saltLenTmp = Integer.parseInt(config.read("salt_len", Integer.toString(saltLenTmp)), 10);
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(PBDKF2Config.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            peper = Base64.getDecoder().decode(peperTmp);
            iterations = iterationsTmp;
            outputLen = outputLenTmp;
            saltLen = saltLenTmp;
        }
    }

    public byte[] getPeper() {
        return peper;
    }

    public int getIterations() {
        return iterations;
    }

    public int getOutputLen() {
        return outputLen;
    }

    public int getSaltLen() {
        return saltLen;
    }
}

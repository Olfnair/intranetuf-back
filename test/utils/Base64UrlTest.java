/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Florian
 */
public class Base64UrlTest {
    
    public Base64UrlTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testEncodeDecode() {
        try {
            String str;
            
            // decodage(encodage(str)) = str
            str = Base64Url.encode("abcdefghi", "UTF-8");
            str = Base64Url.decode(str, "UTF-8");
            assertEquals("abcdefghi", str);
            
            // test décodage padding 1 =
            str = Base64Url.decode("YWJjZGVmamhpamtsbW4=", "UTF-8");
            assertEquals("abcdefjhijklmn", str);
            
            // test décodage padding 1 .
            str = Base64Url.decode("YWJjZGVmamhpamtsbW4.", "UTF-8");
            assertEquals("abcdefjhijklmn", str);
            
            // test décodage padding 1 omis
            str = Base64Url.decode("YWJjZGVmamhpamtsbW4", "UTF-8");
            assertEquals("abcdefjhijklmn", str);
            
            // test encodage, + remplacé par -
            str = Base64Url.encode("^a~", "UTF-8");
            assertEquals("XmF-", str);
            
            // test décodage padding 2 =
            str = Base64Url.decode("c2tqZGhmanNkaGZqc2RoZnNkYQ==", "UTF-8");
            assertEquals("skjdhfjsdhfjsdhfsda", str);
            
            // test décodage padding 2 .
            str = Base64Url.decode("c2tqZGhmanNkaGZqc2RoZnNkYQ..", "UTF-8");
            assertEquals("skjdhfjsdhfjsdhfsda", str);
            
            // test décodage padding 2 omis
            str = Base64Url.decode("c2tqZGhmanNkaGZqc2RoZnNkYQ", "UTF-8");
            assertEquals("skjdhfjsdhfjsdhfsda", str);
            
            // encodage sans padding en sortie
            str = Base64Url.encode("skjdhfjsdhfjsdhfsda", "UTF-8");
            assertEquals("c2tqZGhmanNkaGZqc2RoZnNkYQ", str);
            
            // décodage : on récupère bien l'entrée précédente
            str = Base64Url.decode(str, "UTF-8");
            assertEquals("skjdhfjsdhfjsdhfsda", str);
            
            str = Base64Url.decode("aOly6Q", "ISO-8859-1");
            assertEquals("héré", str);
        } catch (UnsupportedEncodingException ex) {
            assertTrue(false);
            Logger.getLogger(Base64UrlTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}

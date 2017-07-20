/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.query;

import java.util.HashMap;
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
public class ParamsParserTest {
    
    public ParamsParserTest() {
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
    public void testParse() {
        HashMap<String, String> resmap;
        
        // test default
        resmap = new ParamsParser().parse();
        assertTrue(resmap.isEmpty());
        
        // test default
        resmap = new ParamsParser("default").parse();
        assertTrue(resmap.isEmpty());
        
        // test normal
        resmap = new ParamsParser("[{col: 'key', param: 'value'}]").parse();
        assertTrue(resmap.containsKey("key"));
        assertEquals("value", resmap.get("key"));
        
        resmap = new ParamsParser("[{col: 'key', param: 'value'}, {col:\"key2\",param:\"value2\"}]").parse();
        assertTrue(resmap.containsKey("key"));
        assertEquals("value", resmap.get("key"));
        assertTrue(resmap.containsKey("key2"));
        assertEquals("value2", resmap.get("key2"));
        
        resmap = new ParamsParser("[{col: 'key', param: 'value'}, {col:\"key2\",param:\"value2\"").parse();
        assertTrue(resmap.containsKey("key"));
        assertEquals("value", resmap.get("key"));
        assertTrue(resmap.containsKey("key2"));
        assertEquals("value2", resmap.get("key2"));
        
        // test de format : pas obligé d'envoyer du json, tant que chaque col est suivi de son param avec ' ou " pour délimiter les valeurs, c'est bon
        resmap = new ParamsParser("col:'key'param:'value'col:\"key2\"param:\"value2\"col:\"key3\"param:\"value3\"").parse();
        assertTrue(resmap.containsKey("key"));
        assertEquals("value", resmap.get("key"));
        assertTrue(resmap.containsKey("key2"));
        assertEquals("value2", resmap.get("key2"));
        assertTrue(resmap.containsKey("key3"));
        assertEquals("value3", resmap.get("key3"));
        
        // il manque un ' avant le premier param => empty
        resmap = new ParamsParser("[{col: 'key', param: value'}, {col:\"key2\",param:\"value2\"}]").parse();
        assertTrue(resmap.isEmpty());
        
        // typo dans le 2ème param, on a donc pas la 2ème paire
        resmap = new ParamsParser("[{col: 'key', param: 'value'}, {col:\"key2\",paam:\"value2\"}]").parse();
        assertTrue(resmap.containsKey("key"));
        assertEquals("value", resmap.get("key"));
        assertFalse(resmap.containsKey("key2"));
        assertFalse(resmap.containsValue("value2"));
        
        resmap = new ParamsParser("[{col: 'key', param: 'value'}, {col:\"key2\",param:\"value2\"}]col:'key3'param:\"value3\"").parse();
        assertTrue(resmap.containsKey("key"));
        assertEquals("value", resmap.get("key"));
        assertTrue(resmap.containsKey("key2"));
        assertEquals("value2", resmap.get("key2"));
        assertTrue(resmap.containsKey("key3"));
        assertEquals("value3", resmap.get("key3"));
    }
}

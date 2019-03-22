package functionalTesting;

import static org.junit.Assert.*;

import java.util.HashMap;
import org.junit.*;

import jodd.util.StringTemplateParser;
import jodd.util.StringTemplateParser.MacroResolver;

public class StringTemplateParserTest {
	
    HashMap<String, String> map;
    StringTemplateParser parser;
    MacroResolver macroResolver;
    
    @Before
    public void set() {
    	parser = new StringTemplateParser();
    	map = new HashMap<String, String>();
        map.put("hello", "yello");    
    	macroResolver = StringTemplateParser.createMapMacroResolver(map);
        
    }
    
    @Test
    public void UpperCaseTest() {
             String result = parser.parse("xxx${small}xxx", String::toUpperCase);
             assertEquals(result, "xxxSMALLxxx");
    }
    
    @Test
    public void ReplacementTest() {
            String result = parser.parse("${hello} everyone", macroResolver);
            assertEquals(result, "yello everyone");
    }
    
    @Test
    public void escapedTest() {
            String result = parser.parse("\\${hello} everyone", macroResolver);
            assertEquals(result, "${hello} everyone");
    }
    
    
}

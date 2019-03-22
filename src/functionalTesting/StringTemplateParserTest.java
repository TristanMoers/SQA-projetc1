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
    public void multipleTest() {
    	map = new HashMap<String, String>();
    	map.put("hello", "yello");
    	map.put("world", "hello");
    	MacroResolver resolver = StringTemplateParser.createMapMacroResolver(map);
    	String result = parser.parse("${hello} ${world}", resolver);
    	assertEquals(result, "yello hello");
    }
    
    @Test
    public void escapedTest() {
    	String result = parser.parse("\\${hello} everyone", macroResolver);
    	assertEquals(result, "${hello} everyone");
    }
    
    
   @Test(expected = IllegalArgumentException.class)
   public void malformedTest1() {
	   parser.parse("${hello", macroResolver);
   }
    
   @Test(expected = NullPointerException.class)
   public void nullTemplateTest() {
           parser.parse(null, macroResolver);
   }
   @Test(expected = NullPointerException.class)
   public void nullMapResolverTest() {
           macroResolver =  StringTemplateParser.createMapMacroResolver(null);
           parser.parse(ONE, macroResolver);
   }
   @Test
   public void emptyTest() {
           String output = parser.parse(EMPTY, macroResolver);
           assertEquals("", output);
   }
   
   @Test
   public void malformedTest2() {
           String output = parser.parse("${hello} %{people}?", macroResolver);
           assertEquals(output, "yello %{people}?");
   }
   
   @Test
   public void noMacroTest() {
           String output = parser.parse("No macro", macroResolver);
           assertEquals(output, "No macro");
   }
	   
   @Test
   public void emptyMapTest() {
           map = new HashMap<String, String>();
           macroResolver =  StringTemplateParser.createMapMacroResolver(map);
           String output = parser.parse("Yo ${hello}", macroResolver);
           assertEquals(output, "Yo ");
   }
   @Test
   public void replaceMacroWithNullValue() {
           map = new HashMap<String, String>();
           map.put("hello", null);
           macroResolver = StringTemplateParser.createMapMacroResolver(map);
           String output = parser.parse("Yo ${hello}", macroResolver);
           assertEquals(output, "Yo ");
   }

   
    // ================== FUNCTIONNAL TESTING ====================
    
    public static final String EMPTY = "";
    public static final String ONE = "${aaa}";
    public static final String MULTIPLE = "${aaa} ${bbb}";
    public static final String NESTED = "${aaa${bbb}ccc}";
    public static final String NESTED_OK = "ddddddddd";
    public static final String ESCAPED = "Escaping\\${aaa}";
    public static final String ESCAPED_OK = "Escaping${aaa}";
    public static final String MODIFIED_ESCAPED = "Escaping*${aaa}";
    public static final String MODIFIED_ESCAPED_ONE = "Escaping*@[aaa]macro"; 

    public static final char DEFAULT_ESCAPE = '\\';
    public static final String DEFAULT_PREFIX = "$";
    public static final String DEFAULT_START = "${";
    public static final String DEFAULT_END = "}";
    public static final char MODIFIED_ESCAPE = '*';
    public static final String MODIFIED_PREFIX = "@";
    public static final String MODIFIED_START = "@[";
    public static final String MODIFIED_END = "]";
    public static final String MODIFIED_MISSING_KEY = "<miss>";
    
    public static final MacroResolver RESOLVER_EMPTY = StringTemplateParser.createMapMacroResolver(new HashMap<>());
    
    public static MacroResolver MATCH; 
    {
    	HashMap<String, String> mapmatch = new HashMap<>();
    	mapmatch.put("aaa", "zzz");
    	mapmatch.put("bbb", "yyy");
    	mapmatch.put("aaayyyccc", "ddddddddd");
    	MATCH = StringTemplateParser.createMapMacroResolver(mapmatch);
    }

    public static MacroResolver SOMEMATCH;  
    {
    	HashMap<String, String> mapsomematch = new HashMap<>();
    	mapsomematch.put("aaa", "zzz");
    	mapsomematch.put("bbb", "yyy");
    	SOMEMATCH = StringTemplateParser.createMapMacroResolver(mapsomematch);
    }
    
    public static MacroResolver UNMATCH;
    {
    	HashMap<String, String> mapunmatch = new HashMap<>();
    	mapunmatch.put("not", "value");
    	UNMATCH = StringTemplateParser.createMapMacroResolver(mapunmatch);
    }
    
    
    @Test
    public void test1() {
            parser.setMacroPrefix(MODIFIED_PREFIX);
            parser.setMacroStart(MODIFIED_START);
            String result = parser.parse("@[aaa@[bbb}ccc}", MATCH);
            assertEquals(NESTED_OK, result);
    }
    
    @Test
    public void test2() {
    	parser.setMacroEnd("#");
    	parser.setEscapeChar('*');
    	parser.setMissingKeyReplacement("NOT");
    	map = new HashMap<String, String>();
    	macroResolver = StringTemplateParser.createMapMacroResolver(map);
    	String result = parser.parse("Hello *${foo# bar ${baz#!", macroResolver);
    	assertEquals("Hello ${foo# bar NOT!", result);
    }
    
    @Test
    public void test3() {
    	parser.setReplaceMissingKey(false);
    	parser.setEscapeChar(MODIFIED_ESCAPE);
    	String result = parser.parse(MODIFIED_ESCAPED, MATCH); 
    	assertEquals(ESCAPED_OK, result);
    }
    
    @Test
    public void test4() {
    	parser.setMissingKeyReplacement(MODIFIED_MISSING_KEY);
    	String template = "Escaping\\@[xyz]";
    	String result = parser.parse(template, RESOLVER_EMPTY);
    	assertEquals(template, result);
    }
    
    @Test
    public void test5() {
    	parser.setMacroStart("$[");
    	parser.setMacroEnd("]");
    	parser.setReplaceMissingKey(false);
    	map = new HashMap<String, String>();
    	map.put("foo", "BAZ");
    	macroResolver = StringTemplateParser.createMapMacroResolver(map);
    	String result = parser.parse("Hello $[foo] bar \\$[baz]!", macroResolver);
    	assertEquals("Hello BAZ bar $[baz]!", result);
    }
    
    @Test
    public void test6() {
    	parser.setMissingKeyReplacement(MODIFIED_MISSING_KEY);
    	parser.setMacroStart(MODIFIED_START);
    	String result = parser.parse(MULTIPLE, UNMATCH);
    	assertEquals(MULTIPLE, result);
    }
    
    @Test
    public void test7() {
    	parser.setEscapeChar('*');
    	parser.setMacroStart("#[");
    	parser.setMacroPrefix("#");
    	map = new HashMap<String, String>();
    	macroResolver = StringTemplateParser.createMapMacroResolver(map);
    	parser.setReplaceMissingKey(false);
    	String result = parser.parse("Hello #[world}", macroResolver);
    	assertEquals("Hello #[world}", result);
    }
 
    	
    	
    @Test
    public void test8() {
    	parser.setEscapeChar('*');
    	parser.setMacroEnd("]");
    	map = new HashMap<String, String>();
    	macroResolver = StringTemplateParser.createMapMacroResolver(map);
    	parser.setMissingKeyReplacement("NOT");
    	String result = parser.parse("Hello ${worl ${d]]", macroResolver);
    	assertEquals("Hello NOT", result);
    }
    
    @Test
    public void test10() {
    	map = new HashMap<String, String>();
    	map.put("fizz", "bizz");
    	map.put("fuzz", "buzz");
    	macroResolver = StringTemplateParser.createMapMacroResolver(map);
    	parser.setMissingKeyReplacement("NOT");
    	parser.setMacroEnd("]");
    	String result = parser.parse("Hello ${fizz] ${bozz]", macroResolver);
    	assertEquals("Hello bizz NOT", result);
     }
    
    @Test
    public void test11() {
    	parser.setMacroPrefix("#");
    	parser.setMacroStart("#[");
    	 parser.setEscapeChar('*');
    	parser.setReplaceMissingKey(false);
    	map = new HashMap<String, String>();
    	map.put("ld", "m");
    	macroResolver = StringTemplateParser.createMapMacroResolver(map);
    	String result = parser.parse("Hello *#[wor#[ld}}", macroResolver);
    	assertEquals("Hello #[worm}", result);
    }
   
    @Test
    public void test12() {
    	parser.setMissingKeyReplacement(MODIFIED_MISSING_KEY);
    	String result = parser.parse(ONE, UNMATCH);
    	assertEquals("<miss>", result);
    }
    
    @Test
    public void test13() {
    	parser.setMissingKeyReplacement(MODIFIED_MISSING_KEY);
    	String result = parser.parse(ESCAPED, SOMEMATCH);
    	assertEquals("Escaping${aaa}", result);
    }
    

    
    @Test
    public void test15() {
    parser.setMacroStart("#["); // cannot work if prefix unchanged
     parser.setReplaceMissingKey(false);
     map = new HashMap<String, String>();
     map.put("sap", "?");
    macroResolver = StringTemplateParser.createMapMacroResolver(map);
    
    String result = parser.parse("Hello \\#[world} #[sup} #[sap}", macroResolver);
    assertEquals("Hello \\#[world} #[sup} #[sap}", result);
    }
    
    @Test
    public void test16() {
    parser.setReplaceMissingKey(false);
     parser.setMacroPrefix(MODIFIED_PREFIX);
     parser.setMacroEnd(MODIFIED_END);
     parser.setEscapeChar(MODIFIED_ESCAPE);
    String result = parser.parse(MULTIPLE, MATCH);
     assertEquals(MULTIPLE, result);
     }
    
    @Test
    public void test17() {
    parser.setMissingKeyReplacement(MODIFIED_MISSING_KEY);
    parser.setMacroEnd(MODIFIED_END);
    parser.setMacroStart(MODIFIED_START);
    parser.setMacroPrefix(MODIFIED_PREFIX);
    String result = parser.parse(ONE, MATCH);
    assertEquals(ONE, result);
    }

    @Test
    public void test18() {
    	parser.setMissingKeyReplacement(MODIFIED_MISSING_KEY);
    	parser.setMacroEnd(MODIFIED_END);
    	parser.setMacroStart(MODIFIED_START);
    	parser.setMacroPrefix(MODIFIED_PREFIX);
    	String output = parser.parse(ONE, MATCH);
    	assertEquals(ONE, output);
    }
    
    @Test
    public void test19() {
    	parser.setReplaceMissingKey(false);
    	parser.setResolveEscapes(false);
    	parser.setMacroStart(MODIFIED_START);
    	String template = "Escaping\\@[aaa}";
    	String result = parser.parse("Escaping\\@[aaa}", MATCH);
    	assertEquals(template, result);
    }
    
    @Test
    public void test20() {
    	parser.setReplaceMissingKey(false);
    	parser.setResolveEscapes(false);
    	parser.setMacroStart(MODIFIED_START);
    	String template = "Escaping\\@[aaa}";
    	String result = parser.parse("Escaping\\@[aaa}", MATCH);
    	assertEquals(template, result);
    }
   
    @Test
    public void test21() {
    	parser.setReplaceMissingKey(false);
    	parser.setResolveEscapes(false);
    	parser.setMacroStart(MODIFIED_START);
    	String template = "Escaping\\@[aaa}";
    	String result = parser.parse("Escaping\\@[aaa}", MATCH);
    	assertEquals(template, result);
    }
    
    
    @Test
    public void test22() {
    	parser.setReplaceMissingKey(false);
    	parser.setEscapeChar(MODIFIED_ESCAPE);
    	parser.setMacroEnd(MODIFIED_END);
    	parser.setMacroStart(MODIFIED_START);
    	parser.setMacroPrefix(MODIFIED_PREFIX);
    	String result = parser.parse(MULTIPLE, SOMEMATCH);
    	assertEquals(MULTIPLE, result);
    }
    
    // Added after code coverage analysis ==========
    
    
    // parse double escaping
	@Test 
	public void test23() {
		String output = parser.parse("Hello \\\\${hello}!", macroResolver);
		assertEquals("Hello \\yello!", output);
	}

	@Test // parse empty inner 
	public void test24() {
		String output = parser.parse("Hello ${${${}}}!", macroResolver);
		assertEquals("Hello !", output);
	}

	@Test // null key replacement
	public void test25() {
		parser.setMissingKeyReplacement(null);
		parser.setReplaceMissingKey(true);
		String output = parser.parse("${nope}", macroResolver);
		assertEquals("", output);
	}

	@Test // unresolved escape sequence
	public void test26() {
		parser.setResolveEscapes(false);
		String output = parser.parse("Hello \\${world}", macroResolver);
		assertEquals(output, "Hello \\${world}");
	}

	@Test // macro in map with ParseValue = true
	public void test27() {
		parser.setParseValues(true);
		map = new HashMap<String, String>();
		map.put("foo", "${bar}");
		map.put("bar", "baz");
		macroResolver = StringTemplateParser.createMapMacroResolver(map);
		String template = "Hello $foo!";
		String output = parser.parse(template, macroResolver);
		assertEquals("Hello baz!", output);
	}

	@Test // catch exception
	public void test28() {
		MacroResolver macroResolver = new MacroResolver() {
	        public String resolve(String macroName) {
	            throw new IllegalArgumentException();
	        }
	    };
		parser.setReplaceMissingKey(false);
	  String output = parser.parse("Hello ${world}!", macroResolver);
	  assertEquals("Hello ${world}!", output);
	}
	
	 @Test
	 public void nullMacroPrefixTest() {
		 parser.setMacroPrefix(null);
		 String template = "${hello}";
		 String output = parser.parse(template, macroResolver);
		 System.out.println(output);
		 assertEquals("yello", output);
	 }
	 
	 @Test
	 public void gettersSettersTest() {
		 StringTemplateParser stp = StringTemplateParser.create();
		 assertEquals(stp.isReplaceMissingKey(), true);
		 assertEquals(stp.getMissingKeyReplacement(), null);
		 assertEquals(stp.isResolveEscapes(), true);
		 assertEquals(stp.getMacroPrefix(), "$");
		 assertEquals(stp.getMacroStart(), "${");
		 assertEquals(stp.getMacroEnd(), "}");
		 assertEquals(stp.getEscapeChar(), '\\');
		 stp.setParseValues(true);
		 assertEquals(stp.isParseValues(), true);
		 stp.setStrictFormat();
		 assertEquals(stp.getMacroPrefix(), null);
	 }
	 
	 @Test
		public void macrosInnerChainTest() {
			parser.setParseValues(true);
			map = new HashMap<String, String>();
			map.put("foobuzz", "world");
			map.put("barfoo", "buzz");
			map.put("baz", "foo");
			macroResolver = StringTemplateParser.createMapMacroResolver(map);
			String template = "Hello ${foo${bar${baz}}}!";
			String output = parser.parse(template, macroResolver);
			assertEquals("Hello world!", output);
		}

		@Test
		public void unstrictMacrosInnerChainTest() {
			parser.setParseValues(true);
			parser.setMacroPrefix("$");
			map = new HashMap<String, String>();
			map.put("foobuzz", "world");
			map.put("barfoo", "buzz");
			map.put("baz", "foo");
			macroResolver = StringTemplateParser.createMapMacroResolver(map);
			String template = "$foo$bar$baz";
			String output = parser.parse(template, macroResolver);
			assertEquals("foo", output);
		}

	 
	 
}

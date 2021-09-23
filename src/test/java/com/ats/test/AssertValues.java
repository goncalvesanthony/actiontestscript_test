package com.ats.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.generator.variables.transform.NumericTransformer;
import com.ats.script.actions.ActionAssertValue;
import com.ats.tools.Operators;

public class AssertValues {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	/*
	 * @Before public void setUp() { }
	 * 
	 * @After public void tearDown() { }
	 */

	@Test
	public void operatorsTrue() throws IOException {

		final ArrayList<String[]> items = new ArrayList<String[]>();

		items.add(new String[] {"20.000000001", "20", Operators.GREATER});
		items.add(new String[] {"20", "20.0000000001", Operators.LOWER});
		
		items.add(new String[] {"20.10", "20.10", Operators.LOWER_EQUAL});
		items.add(new String[] {"20.10", "20.100001", Operators.LOWER_EQUAL});
		
		items.add(new String[] {"20.10", "20.1", Operators.GREATER_EQUAL});
		items.add(new String[] {"20.00000001", "20", Operators.GREATER_EQUAL});
		
		items.add(new String[] {"20.10", "20.10001", Operators.DIFFERENT});
		items.add(new String[] {"20.10", "20.10", Operators.EQUAL});
		items.add(new String[] {"20.100001", "20.100001", Operators.EQUAL});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(String[] item : items) {
			final CalculatedValue calc1 = new CalculatedValue(script, item[0]);
			final CalculatedValue calc2 = new CalculatedValue(script, item[1]);
			
			final ActionAssertValue assertValue = new ActionAssertValue(script, 0, item[2], calc1, calc2);
			assertValue.execute(script, "unitTest", 0);
			
			assertEquals(assertValue.getStatus().isPassed(), true);
		}
	}
	
	@Test
	public void operatorsFalse() throws IOException {

		final ArrayList<String[]> items = new ArrayList<String[]>();

		items.add(new String[] {"20.000000001", "20", Operators.LOWER});
		items.add(new String[] {"20", "20.0000000001", Operators.GREATER});
		
		items.add(new String[] {"20.10001", "20.10", Operators.LOWER_EQUAL});
		items.add(new String[] {"20.101", "20.100001", Operators.LOWER_EQUAL});
		
		items.add(new String[] {"20.10", "20.101", Operators.GREATER_EQUAL});
		items.add(new String[] {"20.00000001", "20.1", Operators.GREATER_EQUAL});
		
		items.add(new String[] {"20.10", "20.10", Operators.DIFFERENT});
		items.add(new String[] {"20.10", "20.1", Operators.EQUAL});
		items.add(new String[] {"20.10001", "20.100001", Operators.EQUAL});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(String[] item : items) {
			final CalculatedValue calc1 = new CalculatedValue(script, item[0]);
			final CalculatedValue calc2 = new CalculatedValue(script, item[1]);
			
			final ActionAssertValue assertValue = new ActionAssertValue(script, 0, item[2], calc1, calc2);
			assertValue.execute(script, "unitTest", 0);
			
			assertEquals(assertValue.getStatus().isPassed(), false);
		}
	}
	
	@Test
	public void numericWithSpaces() throws IOException {

		final ArrayList<String[]> items = new ArrayList<String[]>();
		items.add(new String[] {" 20 .10  ", "20  . 11", Operators.LOWER, Boolean.toString(true)});
		items.add(new String[] {"20.10 ", "20.11", Operators.GREATER, Boolean.toString(false)});
		items.add(new String[] {"20.10", " 20.11", Operators.LOWER_EQUAL, Boolean.toString(true)});
		items.add(new String[] {"20.10", " 20.11", Operators.GREATER_EQUAL, Boolean.toString(false)});
		items.add(new String[] {"20. 10", "20.11", Operators.DIFFERENT, Boolean.toString(true)});
		items.add(new String[] {"20.10", "20.  11", Operators.EQUAL, Boolean.toString(false)});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(String[] item : items) {
			final CalculatedValue calc1 = new CalculatedValue(script, item[0]);
			final CalculatedValue calc2 = new CalculatedValue(script, item[1]);
			
			final ActionAssertValue assertValue = new ActionAssertValue(script, 0, item[2], calc1, calc2);
			assertValue.execute(script, "unitTest", 0);
			
			assertEquals(Boolean.parseBoolean(item[3]), assertValue.getStatus().isPassed());
		}
	}
	
	@Test
	public void numericWithSpacesResult() throws IOException {

		final ArrayList<String[]> items = new ArrayList<String[]>();
		items.add(new String[] {" 20.10", "20.11", Operators.LOWER, Boolean.toString(true)});
		items.add(new String[] {"20.10 ", "20.11", Operators.GREATER, Boolean.toString(false)});
		items.add(new String[] {"  20.10", " 20.11   ", Operators.LOWER_EQUAL, Boolean.toString(true)});
		items.add(new String[] {"20.10", " 20.11", Operators.GREATER_EQUAL, Boolean.toString(false)});
		items.add(new String[] {"20. 10", "20.11", Operators.DIFFERENT, Boolean.toString(true)});
		items.add(new String[] {"20.10", "20.  11", Operators.EQUAL, Boolean.toString(false)});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(String[] item : items) {
			final CalculatedValue calc1 = new CalculatedValue(script, item[0]);
			final CalculatedValue calc2 = new CalculatedValue(script, item[1]);
			
			final ActionAssertValue assertValue = new ActionAssertValue(script, 0, item[2], calc1, calc2);
			assertValue.execute(script, "unitTest", 0);
			assertEquals(Boolean.parseBoolean(item[3]), assertValue.getStatus().isPassed());
		}
	}
	
	@Test
	public void numericWithComma() throws IOException {

		final ArrayList<String[]> items = new ArrayList<String[]>();
		items.add(new String[] {"20,10", "20,11", Operators.LOWER, Boolean.toString(true)});
		items.add(new String[] {"20,10", "20,11", Operators.GREATER, Boolean.toString(false)});
		items.add(new String[] {"20,10", "20,11", Operators.LOWER_EQUAL, Boolean.toString(true)});
		items.add(new String[] {"20,10", "20,11", Operators.GREATER_EQUAL, Boolean.toString(false)});
		items.add(new String[] {"20,10", "20,11", Operators.DIFFERENT, Boolean.toString(true)});
		items.add(new String[] {"20,10", "20,11", Operators.EQUAL, Boolean.toString(false)});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(String[] item : items) {
			final CalculatedValue calc1 = new CalculatedValue(script, item[0]);
			final CalculatedValue calc2 = new CalculatedValue(script, item[1]);
			
			final ActionAssertValue assertValue = new ActionAssertValue(script, 0, item[2], calc1, calc2);
			assertValue.execute(script, "unitTest", 0);
			
			assertEquals(Boolean.parseBoolean(item[3]), assertValue.getStatus().isPassed());
		}
	}
	
	@Test
	public void numericWithDot() throws IOException {

		final ArrayList<String[]> items = new ArrayList<String[]>();
		items.add(new String[] {"20.10", "20.11", Operators.LOWER, Boolean.toString(true)});
		items.add(new String[] {"20.10", "20.11", Operators.GREATER, Boolean.toString(false)});
		items.add(new String[] {"20.10", "20.11", Operators.LOWER_EQUAL, Boolean.toString(true)});
		items.add(new String[] {"20.10", "20.11", Operators.GREATER_EQUAL, Boolean.toString(false)});
		items.add(new String[] {"20.10", "20.11", Operators.DIFFERENT, Boolean.toString(true)});
		items.add(new String[] {"20.10", "20.11", Operators.EQUAL, Boolean.toString(false)});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(String[] item : items) {
			final CalculatedValue calc1 = new CalculatedValue(script, item[0]);
			final CalculatedValue calc2 = new CalculatedValue(script, item[1]);
			
			final ActionAssertValue assertValue = new ActionAssertValue(script, 0, item[2], calc1, calc2);
			assertValue.execute(script, "unitTest", 0);
			
			assertEquals(Boolean.parseBoolean(item[3]), assertValue.getStatus().isPassed());
		}
	}
	
	@Test
	public void numericWithMixedSeparator() throws IOException {

		final ArrayList<String[]> items = new ArrayList<String[]>();
		items.add(new String[] {"20,10", "20.11", Operators.LOWER, Boolean.toString(true)});
		items.add(new String[] {"20.10", "20,11", Operators.GREATER, Boolean.toString(false)});
		items.add(new String[] {"20,10", "20.11", Operators.LOWER_EQUAL, Boolean.toString(true)});
		items.add(new String[] {"20.10", "20,11", Operators.GREATER_EQUAL, Boolean.toString(false)});
		items.add(new String[] {"20,10", "20.11", Operators.DIFFERENT, Boolean.toString(true)});
		items.add(new String[] {"20.10", "20,11", Operators.EQUAL, Boolean.toString(false)});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(String[] item : items) {
			final CalculatedValue calc1 = new CalculatedValue(script, item[0]);
			final CalculatedValue calc2 = new CalculatedValue(script, item[1]);
			
			final ActionAssertValue assertValue = new ActionAssertValue(script, 0, item[2], calc1, calc2);
			assertValue.execute(script, "unitTest", 0);
			
			assertEquals(Boolean.parseBoolean(item[3]), assertValue.getStatus().isPassed());
		}
	}
	
	@Test
	public void numericError() throws IOException {

		final ArrayList<String[]> items = new ArrayList<String[]>();
		items.add(new String[] {"20.10x", "20.11", Operators.GREATER});
		items.add(new String[] {"20.10x", "20.11", Operators.GREATER_EQUAL});
		items.add(new String[] {"20.10x", "20.11", Operators.LOWER});
		items.add(new String[] {"20.10x", "20.11", Operators.LOWER_EQUAL});
		items.add(new String[] {"20.10", "20.11x", Operators.GREATER});
		items.add(new String[] {"20.10", "20.11x", Operators.GREATER_EQUAL});
		items.add(new String[] {"20.10", "20.11x", Operators.LOWER});
		items.add(new String[] {"20.10", "20.11x", Operators.LOWER_EQUAL});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(String[] item : items) {
			final CalculatedValue calc1 = new CalculatedValue(script, item[0]);
			final CalculatedValue calc2 = new CalculatedValue(script, item[1]);
			
			final ActionAssertValue assertValue = new ActionAssertValue(script, 0, item[2], calc1, calc2);
			assertValue.execute(script, "unitTest", 0);
			
			assertEquals(assertValue.getStatus().isPassed(), false);
		}
	}
	
	@Test
	public void stringCompare() throws IOException {

		final ArrayList<String[]> items = new ArrayList<String[]>();
		items.add(new String[] {"value1", "value1", Operators.EQUAL, Boolean.toString(true)});
		items.add(new String[] {"value1", "value2", Operators.DIFFERENT, Boolean.toString(true)});
		items.add(new String[] {"value1", "value1", Operators.DIFFERENT, Boolean.toString(false)});
		items.add(new String[] {"value1", "value2", Operators.EQUAL, Boolean.toString(false)});
		items.add(new String[] {"value14", "value\\d\\d", Operators.REGEXP, Boolean.toString(true)});
		items.add(new String[] {"value1", "value\\d\\d", Operators.REGEXP, Boolean.toString(false)});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(String[] item : items) {
			final CalculatedValue calc1 = new CalculatedValue(script, item[0]);
			final CalculatedValue calc2 = new CalculatedValue(script, item[1]);
			
			final ActionAssertValue assertValue = new ActionAssertValue(script, 0, item[2], calc1, calc2);
			assertValue.execute(script, "unitTest", 0);
			
			assertEquals(Boolean.parseBoolean(item[3]), assertValue.getStatus().isPassed());
		}
	}
	
	@Test
	public void roundValues() throws IOException {
				
		final ArrayList<Object[]> items = new ArrayList<Object[]>();
		items.add(new Object[] {"333.8777", new NumericTransformer(0), "334"});
		items.add(new Object[] {"333.54", new NumericTransformer(0), "334"});
		items.add(new Object[] {"333.56", new NumericTransformer(0), "334"});
		items.add(new Object[] {"333.46", new NumericTransformer(0), "333"});
		items.add(new Object[] {"333.8777", new NumericTransformer(2), "333.88"});
		items.add(new Object[] {"333.8747", new NumericTransformer(2), "333.87"});
		items.add(new Object[] {"333.8777", new NumericTransformer(4), "333.8777"});
		items.add(new Object[] {"333.87755", new NumericTransformer(4), "333.8775"});
		items.add(new Object[] {"333.87756", new NumericTransformer(4), "333.8776"});
		items.add(new Object[] {"333.8777", null, "333.8777"});
		items.add(new Object[] {"333.877778789879797", null, "333.877778789879797"});
		items.add(new Object[] {"333.87756454545", new NumericTransformer(-1), "333.87756454545"});
		items.add(new Object[] {"333.87756", new NumericTransformer(-1), "333.87756"});
		items.add(new Object[] {"333.8777787898797978", null, "333.8777787898797978"});
		items.add(new Object[] {"333.8777787898797978", new NumericTransformer(-1), "333.8777787898798"});
				
		final ActionTestScript script = new ActionTestScript(tempFolder.newFolder());
		
		for(Object[] item : items) {
			final Variable variable = script.createVariable("varNum", new CalculatedValue(item[0].toString()), (NumericTransformer)item[1]);
			assertEquals(variable.getCalculatedValue(), item[2].toString());
		}
	}
}
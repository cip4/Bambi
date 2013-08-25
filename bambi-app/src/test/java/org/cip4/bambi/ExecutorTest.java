/**
 * 
 */
package org.cip4.bambi;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author smeissner
 *
 */
public class ExecutorTest {
	
	private final static String UPDATE_URL = "http://apps.jdf4you.org/update.php?appId=test-bambi-app";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.cip4.bambi.Executor#main(java.lang.String[])}.
	 * @throws Exception 
	 */
	@Test
	public void testCheckForUpdate() throws Exception {
		
		Method method = Executor.class.getDeclaredMethod("checkForUpdates", String.class);
		method.setAccessible(true);
		method.invoke(null, UPDATE_URL);
		
	}
}

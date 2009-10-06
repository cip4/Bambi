/**
 * 
 */
package org.cip4.bambi.richclient.service;

import java.io.IOException;

import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import junit.framework.TestCase;

/**
 * JUnit test case for DeviceServiceImpl
 * @author smeissner
 *
 */
public class DeviceServiceImplTest extends TestCase {

	DeviceServiceImpl deviceServiceImpl;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// setup super
		super.setUp();
		
		// get instance
		deviceServiceImpl = DeviceServiceImpl.getInstance();
	}

	/* 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.service.DeviceServiceImpl#getInstance()}.
	 * Check for singleton class.
	 */
	public void testGetInstance() {
		// get second instance
		DeviceServiceImpl getInstance2 = DeviceServiceImpl.getInstance();
		
		// must be the same instance
		assertSame("Two instances of DeviceServiceImpl are illegal!", deviceServiceImpl, getInstance2);
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.service.DeviceServiceImpl#run()}.
	 */
	public void testRun() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.service.DeviceServiceImpl#getDeviceList()}.
	 */
	public void testGetDeviceList() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.service.DeviceServiceImpl#getDeviceList(long)}.
	 */
	public void testGetDeviceListLong() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.cip4.bambi.richclient.service.DeviceServiceImpl#unmarshallDeviceList()}.
	 * @throws IOException 
	 * @throws MappingException 
	 */
	public void testUnmarshallDeviceList() throws MappingException, IOException, MarshalException, ValidationException {
		deviceServiceImpl.unmarshallDeviceList();
	}

}

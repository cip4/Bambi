/**
 * 
 */
package org.cip4.bambi.core;

import org.cip4.bambi.BambiTestCase;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JMFBuilder;

/**
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class XMLRequestTest extends BambiTestCase
{
	/**
	 * 
	 */
	public void testConstruct()
	{
		JDFJMF jmf = new JMFBuilder().buildHoldQueueEntry("42");
		XMLRequest req = new XMLRequest(jmf);
		assertEquals(jmf, req.getXML());
	}
}

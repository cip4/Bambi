/**
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2024 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This product includes software developed by the The International Cooperation for
 * the Integration of Processes in Prepress, Press and Postpress (www.cip4.org)" Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party acknowledgments
 * normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of Processes in Prepress, Press and Postpress" must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4", nor may "CIP4" appear in their name, without prior written permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE. ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the The International Cooperation for the Integration of Processes in Prepress, Press and Postpress and was
 * originally based on software copyright (c) 1999-2006, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestProp;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.jdflib.auto.JDFAutoDeviceFilter.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.junit.Test;

public class ProxyDeviceTest extends BambiTestCaseBase
{

	@Test
	public void testToString()
	{
		final ProxyDevice dev = new ProxyDevice(new BambiTestProp());
		assertNotNull(dev.toString());
	}

	@Test
	public void testGetHandlers()
	{
		final ProxyDevice dev = new ProxyDevice(new BambiTestProp());
		final Vector<JMFHandler> handlers = dev.getJMFHandlers();
		assertNotNull(handlers);
	}

	/**
	 *
	 */
	@Test
	public void testResourceQuery()
	{
		final JDFJMF jmf = new JMFBuilder().buildResourceQuery(true);
		final ProxyDevice device = getDevice();
		final JMFHandler jmfHandler = device.getJMFHandler(null);
		final JDFDoc respDoc = jmfHandler.processJMF(jmf.getOwnerDocument_JDFElement());
		final JDFResponse resp = respDoc.getJMFRoot().getResponse(0);
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
	}

	public static ProxyDevice getDevice()
	{
		final ProxyDevice device = new ProxyDevice(new BambiTestProp());
		device.getSignalDispatcher().reset();
		device.getSignalDispatcher().shutdown();

		return device;
	}

	/**
	 *
	 */
	@Test
	public void testResourceQuerysubscribed()
	{
		final JDFJMF jmf = new JMFBuilder().buildResourceQuery(true);
		jmf.getQuery(0).setAttribute(JMFHandler.subscribed, true, null);
		final ProxyDevice device = new ProxyDevice(new BambiTestProp());
		final JMFHandler jmfHandler = device.getJMFHandler(null);
		final JDFDoc respDoc = jmfHandler.processJMF(jmf.getOwnerDocument_JDFElement());
		assertNull(respDoc.getJMFRoot().getResponse());
		assertNotNull(respDoc.getJMFRoot().getSignal());
	}

	/**
	 *
	 */
	@Test
	public void testStatus()
	{
		final JDFJMF jmf = new JMFBuilder().buildStatus(EnumDeviceDetails.Brief, EnumJobDetails.Brief);
		final ProxyDevice device = getDevice();
		final JMFHandler jmfHandler = device.getJMFHandler(null);
		final JDFDoc respDoc = jmfHandler.processJMF(jmf.getOwnerDocument_JDFElement());
		final JDFResponse resp = respDoc.getJMFRoot().getResponse(0);
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		final JDFDeviceInfo di = resp.getDeviceInfo(0);
		assertNotNull(di);
	}

	/**
	 *
	 */
	@Test
	public void testStatusSubScription()
	{
		final JDFJMF jmf = new JMFBuilder().buildStatusSubscription("http://url", 20, 30, null);
		final ProxyDevice device = getDevice();
		final JMFHandler jmfHandler = device.getJMFHandler(null);
		device.getSignalDispatcher().removeSubScriptions(null, null);
		final JDFDoc respDoc = jmfHandler.processJMF(jmf.getOwnerDocument_JDFElement());
		final JDFResponse resp = respDoc.getJMFRoot().getResponse(0);
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		assertTrue(resp.getSubscribed());
	}

	/**
	*
	*/
	@Test
	public void testStatusSubscribed()
	{
		final JDFJMF jmf = new JMFBuilder().buildStatus(EnumDeviceDetails.Brief, EnumJobDetails.Brief);
		jmf.getQuery(0).setAttribute(JMFHandler.subscribed, true, null);
		final ProxyDevice device = new ProxyDevice(new BambiTestProp());
		final JMFHandler jmfHandler = device.getJMFHandler(null);
		final JDFDoc respDoc = jmfHandler.processJMF(jmf.getOwnerDocument_JDFElement());
		assertNull(respDoc.getJMFRoot().getResponse());
		assertNotNull(respDoc.getJMFRoot().getSignal());
	}

	/**
	*
	*/
	@Test
	public void testStatusSignal()
	{
		final ProxyDevice device = new ProxyDevice(new BambiTestProp());
		device.getSignalDispatcher().removeSubScriptions(null, null);
		final JMFHandler jmfHandler = device.getJMFHandler(null);

		final JDFJMF status = new JMFBuilder().buildStatusSignal(EnumDeviceDetails.Brief, EnumJobDetails.Brief);
		final JDFDoc respStatus = jmfHandler.processJMF(status.getOwnerDocument_JDFElement());

		for (int i = 0; i < 3; i++)
		{
			final JDFJMF jmf = new JMFBuilder().buildStatus(EnumDeviceDetails.Brief, EnumJobDetails.Brief);

			final JDFDoc respDoc = jmfHandler.processJMF(jmf.getOwnerDocument_JDFElement());
			final JDFResponse resp = respDoc.getJMFRoot().getResponse(0);
			assertNotNull(resp);
			assertEquals(0, resp.getReturnCode());
		}
	}

}

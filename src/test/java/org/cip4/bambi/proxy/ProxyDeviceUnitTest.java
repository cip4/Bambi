/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of
 * Processes in  Prepress, Press and Postpress (CIP4).  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        The International Cooperation for the Integration of
 *        Processes in  Prepress, Press and Postpress (www.cip4.org)"
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "CIP4" and "The International Cooperation for the Integration of
 *    Processes in  Prepress, Press and Postpress" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact info@cip4.org.
 *
 * 5. Products derived from this software may not be called "CIP4",
 *    nor may "CIP4" appear in their name, without prior written
 *    permission of the CIP4 organization
 *
 * Usage of this software in commercial products is subject to restrictions. For
 * details please consult info@cip4.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE INTERNATIONAL COOPERATION FOR
 * THE INTEGRATION OF PROCESSES IN PREPRESS, PRESS AND POSTPRESS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the The International Cooperation for the Integration
 * of Processes in Prepress, Press and Postpress and was
 * originally based on software
 * copyright (c) 1999-2001, Heidelberger Druckmaschinen AG
 * copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the
 * Integration of Processes in  Prepress, Press and Postpress , please see
 * <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestProp;
import org.cip4.bambi.core.ConverterCallback;
import org.cip4.bambi.core.queues.IQueueEntry;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.node.NodeIdentifier;
import org.junit.Test;

public class ProxyDeviceUnitTest extends BambiTestCaseBase
{
	private static class ProxyDeviceForUnit extends ProxyDevice
	{
		public ProxyDeviceForUnit(final BambiTestProp properties)
		{
			super(properties);
		}

		AbstractProxyProcessor exposeCreateExistingProcessor(final IQueueEntry iqe)
		{
			return createExistingProcessor(iqe);
		}

		ProxyDeviceProcessor exposeGetProcessorForSignal(final String slaveQEID, final NodeIdentifier nid)
		{
			return getProcessorForSignal(slaveQEID, nid);
		}
	}

	@Test
	public void testProxyPropertiesSlaveDeviceAndURLParsing()
	{
		final XMLDoc doc = new XMLDoc("application", null);
		final KElement root = doc.getRoot();
		final KElement deviceElement = root.appendElement(ElementName.DEVICE);
		deviceElement.setAttribute("DeviceID", "ProxyA");
		deviceElement.setAttribute("SlaveURL", "http://slave.example/jmf/slaveA");

		final ProxyProperties properties = new ProxyProperties(doc);
		final ProxyProperties.ProxyDeviceProperties proxyDeviceProperties = properties.createDeviceProps(deviceElement);

		assertEquals("ProxyA", proxyDeviceProperties.getSlaveDeviceID());
		assertEquals("http://slave.example/jmf/slaveA", proxyDeviceProperties.getSlaveURL());
		proxyDeviceProperties.setSlaveDeviceID("SlaveA");
		assertEquals("SlaveA", proxyDeviceProperties.getSlaveDeviceID());
	}

	@Test
	public void testProxyPropertiesDeviceURLForSlaveConversion()
	{
		final XMLDoc doc = new XMLDoc("application", null);
		final KElement deviceElement = doc.getRoot().appendElement(ElementName.DEVICE);
		deviceElement.setAttribute("DeviceID", "ProxyB");
		final ProxyProperties properties = new ProxyProperties(doc);
		final ProxyProperties.ProxyDeviceProperties proxyDeviceProperties = properties.createDeviceProps(deviceElement);

		final String deviceURL = proxyDeviceProperties.getDeviceURL();
		assertTrue(deviceURL.contains("/jmf/"));
		assertEquals(deviceURL.replace("/jmf/", "/slavejmf/"), proxyDeviceProperties.getDeviceURLForSlave());
	}

	@Test
	public void testProxyPropertiesSlaveMimeAndChunkConfig()
	{
		final XMLDoc doc = new XMLDoc("application", null);
		final KElement deviceElement = doc.getRoot().appendElement(ElementName.DEVICE);
		deviceElement.setAttribute("DeviceID", "ProxyC");
		deviceElement.setAttribute("HTTPChunk", "256");
		final ProxyProperties properties = new ProxyProperties(doc);
		final ProxyProperties.ProxyDeviceProperties proxyDeviceProperties = properties.createDeviceProps(deviceElement);

		assertEquals(256, proxyDeviceProperties.getSlaveHTTPChunk());
		proxyDeviceProperties.setDeviceAttribute("SlaveHTTPChunk", "1024");
		assertEquals(1024, proxyDeviceProperties.getSlaveHTTPChunk());

		proxyDeviceProperties.setDeviceAttribute("SlaveMimePackaging", "false");
		proxyDeviceProperties.setDeviceAttribute("SlaveMIMETransferExpansion", "true");
		assertFalse(proxyDeviceProperties.getSlaveMIMEExpansion());

		proxyDeviceProperties.setDeviceAttribute("SlaveMimePackaging", "true");
		assertTrue(proxyDeviceProperties.getSlaveMIMEExpansion());
	}

	@Test
	public void testProxyPropertiesSlaveCallbackConversion()
	{
		final XMLDoc doc = new XMLDoc("application", null);
		final KElement deviceElement = doc.getRoot().appendElement(ElementName.DEVICE);
		deviceElement.setAttribute("DeviceID", "ProxyD");
		deviceElement.setAttribute("SlaveCallBackName", ConverterCallback.class.getName());
		final ProxyProperties properties = new ProxyProperties(doc);
		final ProxyProperties.ProxyDeviceProperties proxyDeviceProperties = properties.createDeviceProps(deviceElement);

		assertNotNull(proxyDeviceProperties.getSlaveCallBackClass());
		final JDFDoc jdf = JDFNode.createRoot().getOwnerDocument_JDFElement();
		assertNotNull(proxyDeviceProperties.getSlaveCallBackClass().updateJDFForExtern(jdf));
		assertNotNull(proxyDeviceProperties.getSlaveCallBackClass().prepareJDFForBambi(jdf));
	}

	@Test
	public void testProxyDeviceSlaveURICallbackRouting()
	{
		final BambiTestProp properties = new BambiTestProp();
		properties.setSlaveURL("http://slave.example/jmf/slaveX");
		properties.setDeviceAttribute("SlaveCallBackName", ConverterCallback.class.getName());
		final ProxyDeviceForUnit device = new ProxyDeviceForUnit(properties);

		final String deviceURLForSlave = device.getDeviceURLForSlave();
		assertTrue(device.isSlaveURI(deviceURLForSlave));
		assertTrue(device.isSlaveURI(properties.getSlaveURL()));
		assertFalse(device.isSlaveURI("http://example.org/jmf/other"));
		assertSame(device.getSlaveCallback(), device.getCallback(deviceURLForSlave));
	}

	@Test
	public void testGetNameFromQueueEntry()
	{
		final ProxyDeviceForUnit device = new ProxyDeviceForUnit(new BambiTestProp());
		final JDFQueueEntry qe = device.getQueueProcessor().getQueue().appendQueueEntry();
		qe.setQueueEntryID("qe-99");

		assertEquals("qqe-99.jdf", device.getNameFromQE(qe));
	}

	// Integration-only: NodeIdentifier/slave-QEID mapping requires live proxy submission state.
}

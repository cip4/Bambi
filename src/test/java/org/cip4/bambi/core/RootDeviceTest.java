/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2026 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
 * originally based on software copyright (c) 1999-2001, Heidelberger Druckmaschinen AG copyright (c) 1999-2001, Agfa-Gevaert N.V.
 *
 * For more information on The International Cooperation for the Integration of Processes in Prepress, Press and Postpress , please see <http://www.cip4.org/>.
 *
 *
 */
package org.cip4.bambi.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.core.messaging.IMessageHandler;
import org.cip4.bambi.core.messaging.JMFHandler;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFDeviceList;
import org.junit.Test;

public class RootDeviceTest extends BambiTestCaseBase
{

	@Test
	public void testToString()
	{
		final InputStream resourceAsStream = getClass().getResourceAsStream("/config/devices.xml");
		if (resourceAsStream == null)
		{
			log.fatal("invalid resource stream ");
		}
		final MultiDeviceProperties mp = MultiDeviceProperties.getProperties(resourceAsStream);

		final RootDevice rd = new RootDevice(mp.createDeviceProps(0));
		assertNotNull(rd.toString());
	}

	@Test
	public void testXSLT()
	{
		final RootDevice rd = getRootDevice();
		final ContainerRequest request = new ContainerRequest();
		assertNotNull(rd.getXSLT(request));
		request.setParameter(RootDevice.XSL, "0");
		assertNull(rd.getXSLT(request));
	}

	@Test
	public void testGetDevice()
	{
		final RootDevice rd = getRootDevice();
		assertEquals(rd, rd.getDevice(null));
	}

	@Test
	public void testReset()
	{
		final RootDevice rd = getRootDevice();
		rd.reset();
		assertEquals(rd, rd.getDevice(null));
	}

	@Test
	public void testShutdown()
	{
		final RootDevice rd = getRootDevice();
		rd.shutdown();
		assertEquals(rd, rd.getDevice(null));
	}

	@Test
	public void testNodeFromDoc()
	{
		final RootDevice rd = getRootDevice();
		assertNull(rd.getNodeFromDoc(null));
		assertNull(rd.getNodeFromDoc(JDFNode.createRoot().getOwnerDocument_JDFElement()));
	}

	@Test
	public void testKnownDevicesHandlerIncludesChildDevices()
	{
		final RootDevice root = getRootWithChildDevices(2);
		try
		{
			final JDFJMF knownQuery = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownDevices);
			final JDFJMF knownResponse = JDFJMF.createJMF(EnumFamily.Response, EnumType.KnownDevices);
			final JDFResponse response = knownResponse.getResponse(0);

			assertTrue(root.new KnownDevicesHandler().handleMessage(knownQuery.getQuery(0), response));

			final JDFDeviceList deviceList = response.getDeviceList(0);
			assertNotNull(deviceList);
			assertTrue(hasDeviceInfo(deviceList, root.getDeviceID()));
			for (final AbstractDevice child : root.getDeviceArray())
			{
				assertTrue(hasDeviceInfo(deviceList, child.getDeviceID()));
			}
		}
		finally
		{
			root.shutdown();
		}
	}

	@Test
	public void testStatusHandlerAggregatesChildStatuses()
	{
		final RootDevice root = getRootDevice();
		try
		{
			addStatusDispatchChild(root, "child-A");
			addStatusDispatchChild(root, "child-B");

			final JDFJMF statusQuery = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status);
			final JDFJMF statusResponse = JDFJMF.createJMF(EnumFamily.Response, EnumType.Status);
			final JDFResponse response = statusResponse.getResponse(0);

			assertTrue(root.new StatusHandler().handleMessage(statusQuery.getQuery(0), response));
			assertEquals(0, response.getReturnCode());
			assertTrue(hasDeviceInfo(response, "child-A"));
			assertTrue(hasDeviceInfo(response, "child-B"));
		}
		finally
		{
			root.shutdown();
		}
	}

	@Test
	public void testQueueDispatchHandlerRoutesByDeviceID()
	{
		final RootDevice root = getRootDevice();
		try
		{
			addQueueDispatchChild(root, "queue-device-1");

			final JDFJMF commandJMF = JDFJMF.createJMF(EnumFamily.Command, EnumType.AbortQueueEntry);
			commandJMF.setDeviceID("queue-device-1");
			commandJMF.getCommand(0).appendQueueEntryDef().setQueueEntryID("qe-missing");

			final JDFJMF responseJMF = JDFJMF.createJMF(EnumFamily.Response, EnumType.AbortQueueEntry);
			final JDFResponse response = responseJMF.getResponse(0);

			assertTrue(root.new QueueDispatchHandler(EnumType.AbortQueueEntry, new EnumFamily[] { EnumFamily.Command })
					.handleMessage(commandJMF.getCommand(0), response));
			assertEquals(0, response.getReturnCode());
		}
		finally
		{
			root.shutdown();
		}
	}

	@Test
	public void testQueueDispatchHandlerUnknownDeviceReturnsError()
	{
		final RootDevice root = getRootDevice();
		try
		{
			addQueueDispatchChild(root, "queue-device-1");

			final JDFJMF commandJMF = JDFJMF.createJMF(EnumFamily.Command, EnumType.AbortQueueEntry);
			commandJMF.setDeviceID("unknown-device");
			commandJMF.getCommand(0).appendQueueEntryDef().setQueueEntryID("qe-missing");

			final JDFJMF responseJMF = JDFJMF.createJMF(EnumFamily.Response, EnumType.AbortQueueEntry);
			final JDFResponse response = responseJMF.getResponse(0);

			root.new QueueDispatchHandler(EnumType.AbortQueueEntry, new EnumFamily[] { EnumFamily.Command }).handleMessage(commandJMF.getCommand(0), response);
			assertTrue(response.getReturnCode() != 0);
		}
		finally
		{
			root.shutdown();
		}
	}

	@Test
	public void testRootGetDispatchHandlerWithDeviceParameter()
	{
		final RootDevice root = getRootDevice();
		try
		{
			final AbstractDevice child = mock(AbstractDevice.class);
			when(child.handleGet(any(ContainerRequest.class))).thenAnswer(invocation -> {
				final ContainerRequest request = invocation.getArgument(0);
				if ("device-1".equals(request.getParameter("device")))
				{
					return new XMLResponse(new XMLDoc("ChildDevice", null).getRoot());
				}
				return null;
			});
			root._devices.put("device-1", child);

			final ContainerRequest request = new ContainerRequest();
			request.setParameter("device", "device-1");
			final XMLResponse response = root.new RootGetDispatchHandler().handleGet(request);

			assertNotNull(response);
			assertNotNull(response.getXML());
			assertEquals("ChildDevice", response.getXML().getLocalName());
		}
		finally
		{
			root.shutdown();
		}
	}

	@Test
	public void testShowDeviceContainsChildDevices()
	{
		final RootDevice root = getRootWithChildDevices(2);
		try
		{
			final ContainerRequest request = new ContainerRequest();
			request.setRequestURI("http://localhost:44493/simulator/showDevice/" + root.getDeviceID());
			final XMLResponse response = root.handleGet(request);

			assertNotNull(response);
			assertNotNull(response.getXML());
			assertEquals("DeviceList", response.getXML().getLocalName());
			final String xml = response.getXML().toXML();

			final Set<String> childIDs = new HashSet<>();
			for (final AbstractDevice child : root.getDeviceArray())
			{
				childIDs.add(child.getDeviceID());
			}
			for (final String childID : childIDs)
			{
				assertTrue(xml.contains("DeviceID=\"" + childID + "\""));
			}
		}
		finally
		{
			root.shutdown();
		}
	}

	public RootDevice getRootDevice()
	{
		final InputStream resourceAsStream = getClass().getResourceAsStream("/config/devices.xml");
		final MultiDeviceProperties mp = MultiDeviceProperties.getProperties(resourceAsStream);
		return new RootDevice(mp.createDeviceProps(0));
	}

	private RootDevice getRootWithChildDevices(final int nChildren)
	{
		final InputStream resourceAsStream = getClass().getResourceAsStream("/config/devices.xml");
		final MultiDeviceProperties mp = MultiDeviceProperties.getProperties(resourceAsStream);
		final RootDevice root = new RootDevice(mp.createDeviceProps(0));
		for (int i = 1; i <= nChildren; i++)
		{
			root.createDevice(mp.createDeviceProps(i));
		}
		return root;
	}

	private void addStatusDispatchChild(final RootDevice root, final String childID)
	{
		final AbstractDevice child = mock(AbstractDevice.class);
		final JMFHandler childHandler = mock(JMFHandler.class);
		final IMessageHandler statusHandler = new JMFHandler.AbstractHandler(EnumType.Status, new EnumFamily[] { EnumFamily.Query })
		{
			@Override
			public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
			{
				response.appendDeviceInfo().setDeviceID(childID);
				return true;
			}
		};

		when(child.getDeviceID()).thenReturn(childID);
		when(child.getJMFHandler(null)).thenReturn(childHandler);
		when(childHandler.getMessageHandler(EnumType.Status.getName(), EnumFamily.Query)).thenReturn(statusHandler);
		root._devices.put(childID, child);
	}

	private void addQueueDispatchChild(final RootDevice root, final String childID)
	{
		final AbstractDevice child = mock(AbstractDevice.class);
		final JMFHandler childHandler = mock(JMFHandler.class);
		final IMessageHandler queueHandler = new JMFHandler.AbstractHandler(EnumType.AbortQueueEntry, new EnumFamily[] { EnumFamily.Command })
		{
			@Override
			public boolean handleMessage(final JDFMessage inputMessage, final JDFResponse response)
			{
				final String requestedDeviceID = inputMessage.getJMFRoot() == null ? null : inputMessage.getJMFRoot().getDeviceID();
				if (!childID.equals(requestedDeviceID))
				{
					response.setErrorText("Unknown deviceID for dispatch: " + requestedDeviceID, null);
					response.setReturnCode(5);
					return false;
				}
				response.setReturnCode(0);
				return true;
			}
		};

		when(child.getDeviceID()).thenReturn(childID);
		when(child.getJMFHandler(null)).thenReturn(childHandler);
		when(childHandler.getMessageHandler(EnumType.AbortQueueEntry.getName(), EnumFamily.Command)).thenReturn(queueHandler);
		root._devices.put(childID, child);
	}

	private boolean hasDeviceInfo(final KElement parent, final String deviceID)
	{
		final VElement infos = parent == null ? null : parent.getChildElementVector(ElementName.DEVICEINFO, null);
		if (infos == null)
		{
			return false;
		}
		for (final KElement info : infos)
		{
			if (deviceID.equals(info.getAttribute(AttributeName.DEVICEID)))
			{
				return true;
			}
		}
		return false;
	}

}

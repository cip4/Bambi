/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2025 The International Cooperation for the Integration of Processes in Prepress, Press and Postpress (CIP4). All rights reserved.
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
package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.cip4.bambi.core.AbstractDevice.StatusHandler;
import org.cip4.bambi.core.messaging.JMFBufferHandler.NotificationBufferHandler;
import org.cip4.bambi.core.messaging.JMFBufferHandler.StatusBufferHandler;
import org.cip4.bambi.proxy.ProxyDevice;
import org.cip4.bambi.proxy.ProxyDeviceTest;
import org.cip4.jdflib.auto.JDFAutoDeviceFilter.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoDeviceInfo.EnumDeviceStatus;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.core.JDFElement.EnumNodeStatus;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFJobPhase;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.junit.Test;

public class JMFBufferHandlerTest
{

	@Test
	public void testMessageIdentifier()
	{
		final JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		final JDFJMF jmf = jmfBuilder.buildQueueStatusSubscription("url");
		jmf.getMessageElement(null, null, 0).setSenderID("s3");
		final MessageIdentifier mi = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		assertNull(mi.deviceID);
	}

	@Test
	public void testMatches()
	{
		final JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		final JDFJMF jmf = jmfBuilder.buildQueueStatusSubscription("url");
		jmf.getMessageElement(null, null, 0).setSenderID("s3");
		final MessageIdentifier mi = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		final MessageIdentifier mi2 = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		assertTrue(mi.matches(mi2));
		final MessageIdentifier mi3 = mi.clone();
		mi3.msgType = "foo";
		assertFalse(mi.matches(mi3));
		assertFalse(mi.equals(mi3));

	}

	@Test
	public void testClone()
	{
		final JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		final JDFJMF jmf = jmfBuilder.buildQueueStatusSubscription("url");
		jmf.getMessageElement(null, null, 0).setSenderID("s3");
		final MessageIdentifier mi = new MessageIdentifier(jmf.getMessageElement(null, null, 0), jmf.getDeviceID());
		final MessageIdentifier mi2 = mi.clone();
		assertTrue(mi.matches(mi2));
		assertEquals(mi, mi2);
		assertEquals(mi.hashCode(), mi2.hashCode());

	}

	@Test
	public void testFallBack()
	{
		final ProxyDevice dev = ProxyDeviceTest.getDevice();
		final StatusBufferHandler bh = new StatusBufferHandler(dev);
		final StatusHandler previousQueryHandler = dev.new StatusHandler();
		bh.setFallbackHandler(previousQueryHandler);
		assertEquals(previousQueryHandler, bh.fallBack);
		bh.setFallbackHandler(bh);
		assertEquals(previousQueryHandler, bh.fallBack);
		bh.setFallbackHandler(null);
		assertNull(bh.fallBack);
	}

	@Test
	public void testSignalsFromMap()
	{
		final ProxyDevice dev = ProxyDeviceTest.getDevice();
		final StatusBufferHandler bh = new StatusBufferHandler(dev);
		assertNull(bh.getSignalsFromMap(new MessageIdentifier(null, null)));
	}

	@Test
	public void testNoConsumer()
	{
		final ProxyDevice dev = ProxyDeviceTest.getDevice();
		final StatusBufferHandler bh = new StatusBufferHandler(dev);
		final JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		final JDFJMF jmf = jmfBuilder.buildStatusSignal(null, null);

		for (int i = 0; i < 200; i++)
		{
			final JDFJMF jmf2 = jmfBuilder.buildStatus(EnumDeviceDetails.Full, EnumJobDetails.Full);
			bh.handleSignal(jmf.getSignal(0), null);
			assertTrue(bh.handleQuery(jmf2.getQuery(0), jmf2.createResponse().getResponse()));
		}
	}

	@Test
	public void testNotification()
	{
		final ProxyDevice dev = ProxyDeviceTest.getDevice();
		final NotificationBufferHandler bh = new NotificationBufferHandler(dev);
		final JMFBuilder jmfBuilder = new JMFBuilder();
		jmfBuilder.setSenderID("sender");
		for (int i = 0; i < 50; i++)
		{
			final JDFJMF jmf = jmfBuilder.createJMF(EnumFamily.Signal, EnumType.Notification);
			jmf.getSignal().appendNotification().appendMilestone().setMilestoneType("MS" + i);
			bh.handleSignal(jmf.getSignal(0), null);
		}

		for (int i = 0; i < 200; i++)
		{
			final JDFJMF jmf2 = jmfBuilder.createJMF(EnumFamily.Query, EnumType.Notification);
			final JDFJMF jmf3 = jmfBuilder.createJMF(EnumFamily.Response, EnumType.Notification);
			assertTrue(bh.handleQuery(jmf2.getQuery(0), jmf3.getResponse()));
		}
	}

	@Test
	public void testIdleJobPhase()
	{
		final ProxyDevice dev = ProxyDeviceTest.getDevice();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFJMF jmfs = jmfBuilder.buildStatusSubscription("http://url.com", 0, 0, null);
		final JDFQuery subscription = jmfs.getQuery();
		subscription.setID("channel");
		dev.getSignalDispatcher().addSubscription(subscription);

		final StatusBufferHandler bh = new StatusBufferHandler(dev);

		jmfBuilder.setSenderID("sender");
		final JDFJMF jmf = jmfBuilder.buildStatusSignal(null, null);
		final JDFDeviceInfo deviceInfo = jmf.getSignal().getDeviceInfo(0);
		deviceInfo.setDeviceStatus(EnumDeviceStatus.Idle);
		final JDFJobPhase jp = deviceInfo.getJobPhase();
		jp.setStatus(EnumNodeStatus.InProgress);
		bh.handleSignal(jmf.getSignal(0), null);

		for (int i = 0; i < 3; i++)
		{
			final JDFJMF jmf2 = jmfBuilder.buildStatus(EnumDeviceDetails.Full, EnumJobDetails.Full);
			final JDFQuery q0 = jmf2.getQuery(0);
			q0.setID("channel");
			q0.setAttribute(JMFHandler.subscribed, true, null);
			final JDFJMF jmf3 = JDFJMF.createJMF(EnumFamily.Response, EnumType.Status);
			final JDFResponse r3 = jmf3.getResponse();

			assertTrue(bh.handleQuery(q0, r3));
			final JDFDeviceInfo di = jmf3.getSignal().getDeviceInfo(0);
			assertNotNull(di);
			if (i == 0)
			{
				assertNotNull(di.getJobPhase());
			}
			else
			{
				assertNull(di.getJobPhase());
			}
		}
	}

	@Test
	public void testgetSignals()
	{
		final ProxyDevice dev = ProxyDeviceTest.getDevice();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFJMF jmfs = jmfBuilder.buildStatusSubscription("http://url.com", 0, 0, null);
		final JDFQuery subscription = jmfs.getQuery();
		subscription.setID("channel");
		dev.getSignalDispatcher().addSubscription(subscription);

		final StatusBufferHandler bh = new StatusBufferHandler(dev);
		jmfBuilder.setSenderID("sender");
		final JDFJMF jmf = jmfBuilder.buildStatusSignal(null, null);
		final JDFDeviceInfo deviceInfo = jmf.getSignal().getDeviceInfo(0);
		deviceInfo.setDeviceStatus(EnumDeviceStatus.Idle);
		final JDFJobPhase jp = deviceInfo.getJobPhase();
		jp.setStatus(EnumNodeStatus.InProgress);
		bh.handleSignal(jmf.getSignal(0), null);
		for (int i = 0; i < 4; i++)
		{
			final JDFJMF jmf2 = jmfBuilder.buildStatus(EnumDeviceDetails.Full, EnumJobDetails.Full);
			final JDFResponse r2 = JDFJMF.createJMF(EnumFamily.Response, EnumType.Status).getResponse();
			final JDFQuery q2 = jmf2.getQuery();
			q2.setID("channel");
			final JDFJMF jmf3 = bh.getSignals(q2, r2, false);
			final JDFDeviceInfo di = jmf3.getSignal().getDeviceInfo(0);
			assertNull(jmf3.getResponse());
			if (i == 0)
			{
				assertNotNull(di.getJobPhase());
			}
			else
			{
				assertNull(di.getJobPhase());
			}
		}
	}

	@Test
	public void testgetResponses()
	{
		final ProxyDevice dev = ProxyDeviceTest.getDevice();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFJMF jmfs = jmfBuilder.buildStatusSubscription("http://url.com", 0, 0, null);
		final JDFQuery subscription = jmfs.getQuery();
		subscription.setID("channel");
		dev.getSignalDispatcher().addSubscription(subscription);

		final StatusBufferHandler bh = new StatusBufferHandler(dev);
		jmfBuilder.setSenderID("sender");
		final JDFJMF jmf = jmfBuilder.buildStatusSignal(null, null);
		final JDFDeviceInfo deviceInfo = jmf.getSignal().getDeviceInfo(0);
		deviceInfo.setDeviceStatus(EnumDeviceStatus.Idle);
		final JDFJobPhase jp = deviceInfo.getJobPhase();
		jp.setStatus(EnumNodeStatus.InProgress);
		bh.handleSignal(jmf.getSignal(0), null);
		for (int i = 0; i < 4; i++)
		{
			final JDFJMF jmf2 = jmfBuilder.buildStatus(EnumDeviceDetails.Full, EnumJobDetails.Full);
			final JDFResponse r2 = JDFJMF.createJMF(EnumFamily.Response, EnumType.Status).getResponse();
			final JDFQuery q2 = jmf2.getQuery();
			q2.setID("channel");
			final JDFJMF jmf3 = bh.getSignals(q2, r2, true);
			assertNull(jmf3.getSignal());
			final JDFDeviceInfo di = jmf3.getResponse().getDeviceInfo(0);
			if (i == 0)
			{
				assertNotNull(di.getJobPhase());
			}
			else
			{
				assertNull(di.getJobPhase());
			}
		}
	}

	@Test
	public void testgetResponsesLater()
	{
		final ProxyDevice dev = ProxyDeviceTest.getDevice();
		final JMFBuilder jmfBuilder = new JMFBuilder();

		final StatusBufferHandler bh = new StatusBufferHandler(dev);
		for (int ii = 0; ii < 3; ii++)
		{
			jmfBuilder.setSenderID("sender");
			final JDFJMF jmf = jmfBuilder.buildStatusSignal(null, null);
			jmf.getSignal().setrefID("s");
			final JDFDeviceInfo deviceInfo = jmf.getSignal().getDeviceInfo(0);
			deviceInfo.setDeviceStatus((EnumDeviceStatus) EnumDeviceStatus.getEnumList().get(ii));
			final JDFJobPhase jp = deviceInfo.getJobPhase();
			jp.setStatus((EnumNodeStatus) EnumNodeStatus.getEnumList().get(ii));
			bh.handleSignal(jmf.getSignal(0), null);
		}
		for (int i = 0; i < 4; i++)
		{
			final JDFJMF jmf2 = jmfBuilder.buildStatus(EnumDeviceDetails.Full, EnumJobDetails.Full);
			final JDFResponse r2 = JDFJMF.createJMF(EnumFamily.Response, EnumType.Status).getResponse();
			final JDFQuery q2 = jmf2.getQuery();
			q2.setID("dummy");
			final JDFJMF jmf3 = bh.getSignals(q2, r2, true);
			if (i < 3)
			{
				assertNull(jmf3.getSignal());
				final JDFDeviceInfo di = jmf3.getResponse().getDeviceInfo(0);
				assertNotNull(di.getJobPhase());
			}
			else
			{
				assertNull(jmf3);
			}
		}
	}

}

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

import java.io.File;
import java.util.Set;
import java.util.Vector;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.BambiTestDevice;
import org.cip4.bambi.core.ContainerRequest;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.workers.WorkerDevice;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.cip4.jdflib.jmf.JDFStopPersChParams;
import org.cip4.jdflib.util.FileUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG 10.12.2008
 */
public class SignalDispatcherTest extends BambiTestCase
{

	SignalDispatcher dispatcher;
	WorkerDevice rootDev;
	File dispatcherStoreDir;

	/**
	 * @throws Exception
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 */
	@Override
	@Before
	public void setUp() throws Exception
	{
		rootDev = new BambiTestDevice(false);
		dispatcherStoreDir = rootDev.getDeviceDir();
		FileUtil.deleteAll(dispatcherStoreDir);
		final JMFHandler h = new JMFHandler(rootDev);
		dispatcher = new SignalDispatcher(rootDev);
		dispatcher.addHandlers(h);
		dispatcher.reset();
		super.setUp();
	}

	/**
	 *
	 */
	@Test
	public void testAddSubscription()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");
		assertNotNull(dispatcher.addSubscription(q, null));
		assertNull(dispatcher.addSubscription(q, null));
		s.setRepeatTime(5.0);
		q.setID("1234");
		assertNotNull(dispatcher.addSubscription(q, null));
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testTrigger()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Resource);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(0);
		s.setURL("http://localhost:8080/httpdump/");
		final String channelID = dispatcher.addSubscription(q, null);
		assertNotNull(channelID);
		assertNotNull(dispatcher.triggerChannel(channelID, -1, true, true));
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testGetTrigger()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Resource);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(0);
		s.setURL("http://localhost:8080/httpdump/");
		final String channelID = dispatcher.addSubscription(q, null);
		assertNotNull(channelID);
		final Trigger t = dispatcher.triggerChannel(channelID, -1, true, true);
		assertNotNull(t);
		assertEquals(null, dispatcher.getTrigger(channelID));
		assertEquals(null, dispatcher.getTrigger(t));
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testGetTriggerSubs()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Resource);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(0);
		s.setURL("http://localhost:8080/httpdump/");
		final String channelID = dispatcher.addSubscription(q, null);
		assertNotNull(channelID);
		final Trigger t = dispatcher.triggerChannel(channelID, -1, true, true);
		assertEquals(null, dispatcher.getTrigger(t));
		assertNotNull(t);
		assertEquals(null, dispatcher.getTrigger(t));
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testAddSubscriptionNonJDF()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		jmf.getMessageElement(EnumFamily.Query, null, 0).setType("foobar");
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");
		assertNotNull(dispatcher.addSubscription(q, null));
		assertNull(dispatcher.addSubscription(q, null));
		s.setRepeatTime(5.0);
		q.setID("1234");
		assertNotNull(dispatcher.addSubscription(q, null));
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testAddSubscriptionDevID()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");
		final JDFJMF jmf2 = (JDFJMF) jmf.cloneNewDoc();
		jmf.setDeviceID("d1");
		jmf2.setDeviceID("d2");
		final JDFQuery q2 = jmf2.getQuery(0);
		q2.setID("newID");

		assertNotNull(dispatcher.addSubscription(q, null));
		assertNotNull(dispatcher.addSubscription(q2, null));
		q2.setID("newID2");
		assertNull(dispatcher.addSubscription(q2, null));
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testAddSubscriptionNonSpecific()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status);
		final JDFQuery q = jmf.getQuery(0);
		final JDFStatusQuParams sqp = q.getCreateStatusQuParams(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");

		assertNotNull(dispatcher.addSubscription(q, null));
		assertNull(dispatcher.addSubscription(q, null));
		for (int i = 0; i < 42; i++)
		{
			q.setID("q" + i);
			sqp.setQueueEntryID("q" + i);
			assertNull(dispatcher.addSubscription(q, null));
		}
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testGetChannels()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");
		assertEquals(0, dispatcher.getChannels(EnumType.KnownMessages, null, null).size());
		dispatcher.addSubscription(q, null);
		assertEquals(1, dispatcher.getChannels(EnumType.KnownMessages, null, null).size());
		assertEquals(0, dispatcher.getChannels(EnumType.Resource, null, null).size());
		assertEquals(1, dispatcher.getAllChannels(EnumType.KnownMessages.getName(), null, null).size());
		assertEquals(0, dispatcher.getAllChannels(EnumType.Resource.getName(), null, null).size());
	}

	/**
	 *
	 */
	@Test
	public void testGetSubscription()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");
		final Set<String> channels = dispatcher.getChannels(EnumType.KnownMessages, null, null);
		assertEquals(0, channels.size());
		final String id = dispatcher.addSubscription(q, null);
		assertNotNull(dispatcher.getSubscription(id));
	}

	/**
	 *
	 */
	@Test
	public void testSize()
	{
		assertEquals(0, dispatcher.size());
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");
		dispatcher.addSubscription(q, null);
		assertEquals(1, dispatcher.size());
	}

	/**
	 *
	 */
	@Test
	public void testToString()
	{
		assertNotNull(dispatcher.toString());
		assertNotNull(new SignalDispatcher(null).toString());
	}

	/**
	*
	*/
	@Test
	public void testHasSubsrciption()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");
		assertFalse(dispatcher.hasSubscription(EnumType.KnownMessages));
		assertFalse(dispatcher.hasSubscription(null));
		dispatcher.addSubscription(q, null);
		assertTrue(dispatcher.hasSubscription(null));
		assertTrue(dispatcher.hasSubscription(EnumType.KnownMessages));
	}

	/**
	*
	*/
	@Test
	public void testHandleGetList()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");
		dispatcher.addSubscription(q, null);
		final ContainerRequest req = new ContainerRequest();
		final XMLResponse resp = dispatcher.handleGet(req);
		assertNotNull(resp);
	}

	/**
	*
	*/
	@Test
	public void testHandleGetDetails()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		final JDFQuery q = jmf.getQuery(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");
		dispatcher.addSubscription(q, null);
		final ContainerRequest req = new ContainerRequest();
		req.setParameter("ListSenders", "true");
		req.setParameter("pos", "1");
		final XMLResponse resp = dispatcher.handleGet(req);
		assertNotNull(resp);
		req.setParameter("Raw", "true");
		final XMLResponse raw = dispatcher.handleGet(req);
		assertNotNull(raw);
	}

	/**
	 *
	 */
	@Test
	public void testRemoveSubscription()
	{
		for (int i = 0; i < 3; i++)
		{
			EnumType status = EnumType.Notification;
			if (i == 0)
			{
				status = EnumType.Status;
			}
			else if (i == 1)
			{
				status = EnumType.Resource;
			}

			final JDFJMF jmf1 = JDFJMF.createJMF(EnumFamily.Query, status);
			final JDFQuery q1 = jmf1.getQuery(0);
			final JDFSubscription s1 = q1.appendSubscription();
			s1.setRepeatTime(1.0);
			s1.setURL("http://localhost:8080/httpdump/");
			assertNotNull(dispatcher.addSubscription(q1, null));
		}

		assertEquals(3, dispatcher.getChannels(null, null, null).size());

		final Vector<MsgSubscription> vr = dispatcher.removeSubScriptions("http://localhost:8080/httpdump/", "Resource");
		assertEquals(vr.size(), 1);
		assertEquals(2, dispatcher.getChannels(null, null, null).size());
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testRemoveSubscriptionByChannelID()
	{
		final JDFQuery q = createSubscriptionQuery(EnumType.KnownMessages, "c-remove", "http://localhost:8080/httpdump/remove/");
		final String channelID = dispatcher.addSubscription(q, null);
		assertNotNull(channelID);
		assertNotNull(dispatcher.getSubscription(channelID));
		final MsgSubscription removed = dispatcher.removeSubScription(channelID);
		assertNotNull(removed);
		assertNull(dispatcher.getSubscription(channelID));
		assertEquals(0, dispatcher.size());
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testFindSubscriptionHitMiss()
	{
		final JDFQuery qHit = createSubscriptionQuery(EnumType.KnownMessages, "findHit", "http://localhost:8080/httpdump/find/");
		final JDFResponse rHit = new JDFDoc("JMF").getJMFRoot().appendResponse(EnumType.KnownMessages);
		assertTrue(dispatcher.findSubscription(qHit, rHit));
		assertTrue(rHit.getSubscribed());

		final JDFJMF jmfMiss = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownMessages);
		final JDFQuery qMiss = jmfMiss.getQuery(0);
		qMiss.setID("findMiss");
		final JDFResponse rMiss = new JDFDoc("JMF").getJMFRoot().appendResponse(EnumType.KnownMessages);
		assertFalse(dispatcher.findSubscription(qMiss, rMiss));
		assertFalse(rMiss.getSubscribed());
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testGetSubscriptionsXMLRenderNonEmpty()
	{
		dispatcher.addSubscription(createSubscriptionQuery(EnumType.KnownMessages, "xml1", "http://localhost:8080/httpdump/xml/"), null);
		final XMLResponse resp = dispatcher.handleGet(new ContainerRequest());
		assertNotNull(resp);
		assertNotNull(resp.getXML());
		assertTrue(resp.getXML().numChildElements(MsgSubscription.SUBSCRIPTION_ELEMENT, null) > 0);
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testStopPersistentChannelHandlerByChannelID()
	{
		final String channelID = dispatcher.addSubscription(createSubscriptionQuery(EnumType.Resource, "stopById", "http://localhost:8080/httpdump/stop/id/"), null);
		assertNotNull(channelID);
		final JDFMessage stopCommand = createStopCommand(channelID, null, null);
		final JDFResponse response = new JDFDoc("JMF").getJMFRoot().appendResponse(EnumType.StopPersistentChannel);
		assertTrue(dispatcher.new StopPersistentChannelHandler().handleMessage(stopCommand, response));
		assertNull(dispatcher.getSubscription(channelID));
		assertEquals("Removed Channels: 1", response.getDescriptiveName());
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testStopPersistentChannelHandlerByURL()
	{
		dispatcher.addSubscription(createSubscriptionQuery(EnumType.Resource, "stopUrl1", "http://localhost:8080/httpdump/stop/url/"), null);
		dispatcher.addSubscription(createSubscriptionQuery(EnumType.Status, "stopUrl2", "http://localhost:8080/httpdump/stop/url/"), null);
		dispatcher.addSubscription(createSubscriptionQuery(EnumType.Notification, "stopUrl3", "http://localhost:8080/httpdump/stop/other/"), null);

		final JDFMessage stopCommand = createStopCommand("*", "http://localhost:8080/httpdump/stop/url/", null);
		final JDFResponse response = new JDFDoc("JMF").getJMFRoot().appendResponse(EnumType.StopPersistentChannel);
		assertTrue(dispatcher.new StopPersistentChannelHandler().handleMessage(stopCommand, response));
		assertEquals(1, dispatcher.size());
		assertEquals("Removed Channels: 2", response.getDescriptiveName());
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testStopAllSubscriptionsBookkeeping()
	{
		dispatcher.addSubscription(createSubscriptionQuery(EnumType.Resource, "stopAll1", "http://localhost:8080/httpdump/stop/all/1"), null);
		dispatcher.addSubscription(createSubscriptionQuery(EnumType.Status, "stopAll2", "http://localhost:8080/httpdump/stop/all/2"), null);
		assertEquals(2, dispatcher.size());
		final Vector<MsgSubscription> removed = dispatcher.removeSubScriptions(null, null);
		assertNotNull(removed);
		assertEquals(2, removed.size());
		assertEquals(0, dispatcher.size());
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testKnownSubscriptionsHandlerQueryResponse()
	{
		final JDFQuery subQuery = createSubscriptionQuery(EnumType.KnownMessages, "knownSub1", "http://localhost:8080/httpdump/known/1");
		dispatcher.addSubscription(subQuery, null);

		final JDFJMF knownJMF = JDFJMF.createJMF(EnumFamily.Query, EnumType.KnownSubscriptions);
		final JDFQuery knownQuery = knownJMF.getQuery(0);
		final JDFResponse response = new JDFDoc("JMF").getJMFRoot().appendResponse(EnumType.KnownSubscriptions);
		assertTrue(dispatcher.new KnownSubscriptionsHandler().handleMessage(knownQuery, response));
		assertNotNull(response.getSubscriptionInfo(0));
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testDuplicateSubscriptionSameDeviceIDDedup()
	{
		final JDFQuery q1 = createSubscriptionQuery(EnumType.KnownMessages, "dupDev1", "http://localhost:8080/httpdump/dup/dev/", "devSame");
		final JDFQuery q2 = createSubscriptionQuery(EnumType.KnownMessages, "dupDev2", "http://localhost:8080/httpdump/dup/dev/", "devSame");
		assertNotNull(dispatcher.addSubscription(q1, null));
		assertNull(dispatcher.addSubscription(q2, null));
		dispatcher.shutdown();
	}

	private JDFQuery createSubscriptionQuery(final EnumType type, final String queryID, final String url)
	{
		return createSubscriptionQuery(type, queryID, url, null);
	}

	private JDFQuery createSubscriptionQuery(final EnumType type, final String queryID, final String url, final String deviceID)
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, type);
		if (deviceID != null)
		{
			jmf.setDeviceID(deviceID);
		}
		final JDFQuery q = jmf.getQuery(0);
		q.setID(queryID);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL(url);
		return q;
	}

	private JDFMessage createStopCommand(final String channelID, final String url, final String messageType)
	{
		final JDFJMF stopJMF = JDFJMF.createJMF(EnumFamily.Command, EnumType.StopPersistentChannel);
		final JDFMessage stopCommand = stopJMF.getCommand(0);
		final JDFStopPersChParams spcp = stopCommand.appendStopPersChParams();
		if (channelID != null)
		{
			spcp.setChannelID(channelID);
		}
		if (url != null)
		{
			spcp.setURL(url);
		}
		if (messageType != null)
		{
			spcp.setMessageType(messageType);
		}
		return stopCommand;
	}

	@Override
	@After
	public void tearDown() throws Exception
	{
		if (dispatcher != null)
		{
			dispatcher.reset();
			dispatcher.shutdown();
		}
		JMFFactory.shutdown();
		if (dispatcherStoreDir != null)
		{
			FileUtil.deleteAll(dispatcherStoreDir);
		}
		super.tearDown();
	}

}

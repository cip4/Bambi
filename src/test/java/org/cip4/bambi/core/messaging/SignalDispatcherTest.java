/*
 *
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

import java.util.Set;
import java.util.Vector;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.BambiTestDevice;
import org.cip4.bambi.workers.WorkerDevice;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JDFStatusQuParams;
import org.cip4.jdflib.jmf.JDFSubscription;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG 10.12.2008
 */
public class SignalDispatcherTest extends BambiTestCase
{

	SignalDispatcher dispatcher;

	/**
	 * @throws Exception
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 */
	@Override
	@Before
	public void setUp() throws Exception
	{
		MsgSubscription.setSpecific(true);
		final WorkerDevice rootDev = new BambiTestDevice();
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
		assertNotNull(dispatcher.addSubscription(q, null, null));
		assertNull(dispatcher.addSubscription(q, null, null));
		s.setRepeatTime(5.0);
		q.setID("1234");
		assertNotNull(dispatcher.addSubscription(q, null, null));
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
		assertNotNull(dispatcher.addSubscription(q, null, null));
		assertNull(dispatcher.addSubscription(q, null, null));
		s.setRepeatTime(5.0);
		q.setID("1234");
		assertNotNull(dispatcher.addSubscription(q, null, null));
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

		assertNotNull(dispatcher.addSubscription(q, null, null));
		assertNotNull(dispatcher.addSubscription(q2, null, null));
		q2.setID("newID2");
		assertNull(dispatcher.addSubscription(q2, null, null));
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

		assertNotNull(dispatcher.addSubscription(q, null, null));
		assertNull(dispatcher.addSubscription(q, null, null));
		MsgSubscription.setSpecific(false);
		s.setRepeatTime(5.0);
		for (int i = 0; i < 42; i++)
		{
			q.setID("q" + i);
			sqp.setQueueEntryID("q" + i);
			assertNull(dispatcher.addSubscription(q, "q" + i, null));
		}
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Test
	public void testAddSubscriptionQE()
	{
		final JDFJMF jmf = JDFJMF.createJMF(EnumFamily.Query, EnumType.Status);
		final JDFQuery q = jmf.getQuery(0);
		final JDFStatusQuParams sqp = q.getCreateStatusQuParams(0);
		final JDFSubscription s = q.appendSubscription();
		s.setRepeatTime(1.0);
		s.setURL("http://localhost:8080/httpdump/");

		assertNotNull(dispatcher.addSubscription(q));
		assertNull(dispatcher.addSubscription(q));
		MsgSubscription.setSpecific(true);
		for (int i = 0; i < 42; i++)
		{
			sqp.setQueueEntryID("q" + i);
			q.setID("q" + i);
			assertNotNull(dispatcher.addSubscription(q, "q" + i, null));
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
		dispatcher.addSubscription(q, null, null);
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
		final String id = dispatcher.addSubscription(q, null, null);
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
		dispatcher.addSubscription(q, null, null);
		assertTrue(dispatcher.hasSubscription(null));
		assertTrue(dispatcher.hasSubscription(EnumType.KnownMessages));
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
				status = EnumType.Status;
			else if (i == 1)
				status = EnumType.Resource;

			final JDFJMF jmf1 = JDFJMF.createJMF(EnumFamily.Query, status);
			final JDFQuery q1 = jmf1.getQuery(0);
			final JDFSubscription s1 = q1.appendSubscription();
			s1.setRepeatTime(1.0);
			s1.setURL("http://localhost:8080/httpdump/");
			assertNotNull(dispatcher.addSubscription(q1, null));
		}

		assertEquals(3, dispatcher.getChannels(null, null, null).size());

		final Vector<MsgSubscription> vr = dispatcher.removeSubScriptions(null, "http://localhost:8080/httpdump/", "Resource");
		assertEquals(vr.size(), 1);
		assertEquals(2, dispatcher.getChannels(null, null, null).size());
		dispatcher.shutdown();
	}

	/**
	 *
	 */
	@Ignore
	@Test
	public void testWaitQueued()
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
		final Trigger[] ts = dispatcher.triggerQueueEntry(null, null, -1, null);
		assertNotNull(ts);
		final long t0 = System.currentTimeMillis();
		Trigger.waitQueued(ts, 4000);
		final long t1 = System.currentTimeMillis();
		assertTrue(t1 - t0 < 3000);
		dispatcher.shutdown();
	}

}

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

import java.io.File;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.BambiTestDevice;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.ConverterCallback;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.extensions.MessageHelper;
import org.cip4.jdflib.extensions.XJDFHelper;
import org.cip4.jdflib.extensions.XJMFHelper;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQuery;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.util.FileUtil;
import org.cip4.jdflib.util.MimeUtil;
import org.junit.Test;

/**
 *
 * @author rainer prosi
 *
 */
public class SubscriptionStoreTest extends BambiTestCaseBase
{

	/**
	 *
	 */
	@Test
	public void testCreateDispatcher()
	{
		final SignalDispatcher d = new SignalDispatcher(new BambiTestDevice());
		assertNotNull(d);
	}

	/**
	 *
	 */
	@Test
	public void testCreateStore()
	{
		final SignalDispatcher d = new SignalDispatcher(new BambiTestDevice());
		final SubscriptionStore ss = new SubscriptionStore(d, new File(sm_dirTestDataTemp + "subs"));
		assertNotNull(ss);
	}

	/**
	 *
	 */
	@Test
	public void testExtend()
	{
		final SignalDispatcher d = new SignalDispatcher(new BambiTestDevice());
		d.reset();
		final File dir = new File(sm_dirTestDataTemp + "subs1");
		FileUtil.deleteAll(dir);
		final SubscriptionStore ss = new SubscriptionStore(d, dir);
		final JDFJMF jmf = new JMFBuilder().buildStatusSubscription("http://abc.com", 0, 0, null);
		final JDFQuery q = jmf.getQuery(0);
		q.setID("q");
		q.getSubscription().setAttribute("Foo", "Bar");

		d.addSubscription(q, null);
		ss.persist();

		final SignalDispatcher d2 = new SignalDispatcher(new BambiTestDevice());
		final SubscriptionStore ss2 = new SubscriptionStore(d2, dir);
		ss2.load();
		assertEquals("Bar", ((JDFQuery) d2.getSubscriptionMessage("q")).getSubscription().getAttribute("Foo"));

	}

	/**
	 *
	 */
	@Test
	public void testLoad()
	{
		final SignalDispatcher d = new SignalDispatcher(new BambiTestDevice());
		final File dir = new File(sm_dirTestDataTemp + "subs2");
		FileUtil.deleteAll(dir);
		final SubscriptionStore ss = new SubscriptionStore(d, dir);
		final JDFJMF jmf = new JMFBuilder().buildStatusSubscription("http://abc.com", 0, 0, null);
		final JDFQuery q = jmf.getQuery(0);
		q.setID("qqq");

		d.removeSubScriptions(null, null, null);
		d.addSubscription(q, null);
		ss.persist();

		final SignalDispatcher d2 = new SignalDispatcher(new BambiTestDevice());
		final SubscriptionStore ss2 = new SubscriptionStore(d2, dir);
		ss2.load();
		assertEquals("qqq", d2.getAllChannels(null, null, null).iterator().next());

	}

	/**
	*
	*/
	@Test
	public void testLoad3()
	{
		final SignalDispatcher d = new SignalDispatcher(new BambiTestDevice());
		final File dir = new File(sm_dirTestData + "sub3");
		final SubscriptionStore ss = new SubscriptionStore(d, dir);
		ss.load();
		assertEquals(1, d.subscriptionMap.size());
	}

	/**
	 *
	 */
	@Test
	public void testLoadIdentical()
	{
		final SignalDispatcher d = new SignalDispatcher(new BambiTestDevice());
		final File dir = new File(sm_dirTestData + "subs2");
		FileUtil.deleteAll(dir);
		final SubscriptionStore ss = new SubscriptionStore(d, dir);
		final JDFJMF jmf = new JMFBuilder().buildStatusSubscription("http://abc.com", 0, 0, null);
		final JDFQuery q = jmf.getQuery(0);
		q.setID("qqq");

		d.removeSubScriptions(null, null, null);
		d.addSubscription(q, null);
		ss.persist();

		final SignalDispatcher d2 = new SignalDispatcher(new BambiTestDevice());
		final SubscriptionStore ss2 = new SubscriptionStore(d2, dir);
		ss2.load();
		assertEquals("qqq", d2.getAllChannels(null, null, null).iterator().next());

	}

	/**
	*
	*/
	@Test
	public void testLoadVersion()
	{
		final SignalDispatcher d = new SignalDispatcher(new BambiTestDevice());
		d.reset();
		final File dir = new File(sm_dirTestDataTemp + "subs3");
		FileUtil.deleteAll(dir);
		final SubscriptionStore ss = new SubscriptionStore(d, dir);
		final JDFJMF jmf = new JMFBuilder().buildStatusSubscription("http://abc.com", 0, 0, null);
		final JDFQuery q = jmf.getQuery(0);
		q.setID("q");
		jmf.setMaxVersion(EnumVersion.Version_2_2);

		d.removeSubScriptions(null, null, null);
		d.addSubscription(q, null);
		ss.persist();

		final SignalDispatcher d2 = new SignalDispatcher(new BambiTestDevice());
		final SubscriptionStore ss2 = new SubscriptionStore(d2, dir);
		ss2.load();
		assertEquals(EnumVersion.Version_2_2, d2.getSubscription("q").getJdfVersion());

	}

	/**
	 *
	 */
	@Test
	public void testSenderID()
	{
		final SignalDispatcher dis = new SignalDispatcher(new BambiTestDevice());
		dis.reset();
		final File dir = new File(sm_dirTestDataTemp + "subs4");
		FileUtil.deleteAll(dir);
		final SubscriptionStore ss = new SubscriptionStore(dis, dir);

		final XJMFHelper h = new XJMFHelper();
		final MessageHelper mh = h.appendMessage(EnumFamily.Query, EnumType.Status);
		mh.appendElement(ElementName.SUBSCRIPTION).setAttribute("URL", "http://u2/abc");
		final JDFDoc d = new ConverterCallback().prepareJMFForBambi(new JDFDoc(h.getRoot().getOwnerDocument()));
		final JDFJMF jmf = d.getJMFRoot();
		final JDFQuery query = jmf.getQuery(0);
		query.setID("q");
		final MsgSubscription s = new MsgSubscription(null, query);
		assertNull(s.jmfDeviceID);

		dis.removeSubScriptions(null, null, null);

		dis.addSubscription(query, null);
		ss.persist();

		final SignalDispatcher d2 = new SignalDispatcher(new BambiTestDevice());
		final SubscriptionStore ss2 = new SubscriptionStore(d2, dir);
		ss2.load();
		final MsgSubscription sloaded = d2.getSubscription("q");
		assertEquals(XJDFHelper.defaultVersion(), sloaded.getJdfVersion());
		assertNull(sloaded.jmfDeviceID);
	}

	/**
	 *
	 */
	@Test
	public void testXJMFCallback()
	{
		final SignalDispatcher dis = new SignalDispatcher(new BambiTestDevice());
		dis.reset();
		final File dir = new File(sm_dirTestDataTemp + "subs5");
		FileUtil.deleteAll(dir);
		final SubscriptionStore ss = new SubscriptionStore(dis, dir);

		final XJMFHelper h = new XJMFHelper();
		final MessageHelper mh = h.appendMessage(EnumFamily.Query, EnumType.Status);
		mh.appendElement(ElementName.SUBSCRIPTION).setAttribute("URL", "http://u2/abc");
		final JDFDoc d = new ConverterCallback().prepareJMFForBambi(new JDFDoc(h.getRoot().getOwnerDocument()));
		final JDFJMF jmf = d.getJMFRoot();
		final JDFQuery query = jmf.getQuery(0);
		query.setID("q");
		final MsgSubscription s = new MsgSubscription(null, query);
		assertEquals(XJDFHelper.defaultVersion(), s.getQuery().getMaxVersion(true));
		dis.removeSubScriptions(null, null, null);
		dis.addSubscription(query, null);
		ss.persist();

		final SignalDispatcher d2 = new SignalDispatcher(new BambiTestDevice());
		final SubscriptionStore ss2 = new SubscriptionStore(d2, dir);
		ss2.load();
		final MsgSubscription sloaded = d2.getSubscription("q");
		assertEquals(XJDFHelper.defaultVersion(), sloaded.getQuery().getMaxVersion(true));
	}

	/**
	 *
	 */
	@Test
	public void testJSONCallback()
	{
		final SignalDispatcher dis = new SignalDispatcher(new BambiTestDevice());
		dis.reset();
		final File dir = new File(sm_dirTestDataTemp + "subs6");
		FileUtil.deleteAll(dir);
		final SubscriptionStore ss = new SubscriptionStore(dis, dir);

		final XJMFHelper h = new XJMFHelper();
		BambiNSExtension.setContentType(h.getRoot(), MimeUtil.APPLICATION_JSON);
		final MessageHelper mh = h.appendMessage(EnumFamily.Query, EnumType.Status);
		mh.appendElement(ElementName.SUBSCRIPTION).setAttribute("URL", "http://u2/abc");
		final JDFDoc d = new ConverterCallback().prepareJMFForBambi(new JDFDoc(h.getRoot().getOwnerDocument()));
		final JDFJMF jmf = d.getJMFRoot();
		final JDFQuery query = jmf.getQuery(0);
		query.setID("q");
		final MsgSubscription s = new MsgSubscription(null, query);
		assertFalse(s.isJSON);
		assertEquals(XJDFHelper.defaultVersion(), s.getQuery().getMaxVersion(true));
		dis.removeSubScriptions(null, null, null);
		dis.addSubscription(query, null);
		ss.persist();

		final SignalDispatcher d2 = new SignalDispatcher(new BambiTestDevice());
		final SubscriptionStore ss2 = new SubscriptionStore(d2, dir);
		ss2.load();
		final MsgSubscription sloaded = d2.getSubscription("q");
		assertEquals(XJDFHelper.defaultVersion(), sloaded.getQuery().getMaxVersion(true));
		assertTrue(sloaded.isJSON);
		assertTrue(sloaded.getConverterCallback().isJSON());
	}

}

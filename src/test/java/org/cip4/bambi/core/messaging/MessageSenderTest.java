/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2023 The International Cooperation for the Integration of
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

package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.BambiTestHelper;
import org.cip4.bambi.core.messaging.MessageSender.SendReturn;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.util.ThreadUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 */
public class MessageSenderTest extends BambiTestCase
{

	String snafu = "http://www.foobar.snafu/next";
	MessageSender s;

	/**
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 */
	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
	}

	/**
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void testBadURL() throws Exception
	{
		final JDFJMF jmf = new JMFBuilder().buildStatusSubscription(snafu, 1, 0, null);
		final XMLDoc resp = new BambiTestHelper().submitXMLtoURL(jmf, getWorkerURL());
		assertNotNull(resp);
	}

	/**
	 *
	 *
	 */
	@Test
	@Ignore
	public void testSendToDump()
	{
		s.queuePost(null, "http://localhost:8080/httpdump/messagesendertest", null);
		new Thread(s).start();
		ThreadUtil.sleep(12345);
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testSendDetails() throws IllegalArgumentException, IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		assertNull(s.sendDetails(md));
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testSendHTTP() throws IllegalArgumentException, IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		assertEquals(SendReturn.error, s.sendHTTP(md));
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testProblemError() throws IllegalArgumentException, IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		md.setFireForget(true);
		assertEquals(SendReturn.removed, s.processProblem(md, SendReturn.error));
		md.setFireForget(false);
		assertEquals(SendReturn.error, s.processProblem(md, SendReturn.error));
	}

	/**
	 *
	 *
	 */
	@Test
	public void isRC()
	{
		JMFFactory.getInstance().setZapp500(false);
		assertFalse(s.isRemoveResponseCode(200));
		assertTrue(s.isRemoveResponseCode(400));
		assertFalse(s.isRemoveResponseCode(500));
		assertFalse(s.isRemoveResponseCode(404));

	}

	/**
	 *
	 *
	 */
	@Test
	public void isRC500()
	{
		JMFFactory.getInstance().setZapp500(true);
		assertFalse(s.isRemoveResponseCode(200));
		assertTrue(s.isRemoveResponseCode(400));
		assertTrue(s.isRemoveResponseCode(500));
		assertFalse(s.isRemoveResponseCode(507));

	}

	/**
	 *
	 *
	 */
	@Test
	public void testQueue()
	{
		assertFalse(s.queueMessage(null, null, null, null, null));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testQueueMany()
	{
		JDFJMF jmf = new JMFBuilder().buildStatusSignal(EnumDeviceDetails.Details, EnumJobDetails.Full);
		for (int i = 0; i < 420; i++)
			s.queueMessage(jmf, null, null, null, null);

	}

}

/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2025 The International Cooperation for the Integration of
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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.IConverterCallback;
import org.cip4.bambi.core.messaging.MessageSender.SendReturn;
import org.cip4.bambi.core.messaging.MessageSender.SenderQueueOptimizer;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.util.DumpDir;
import org.cip4.jdflib.util.net.HTTPDetails;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 */
public class MessageSenderTest extends BambiTestCase
{

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		JMFFactory.getInstance().setLogLots(false);
	}

	String snafu = "http://www.foobar.snafu/next";

	/**
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 */
	@BeforeClass
	public static void setUpClass() throws Exception
	{
		final DumpDir outputDumpDir = new DumpDir(new File(sm_dirTestDataTemp + "bambiOut"));
		final DumpDir inputDumpDir = new DumpDir(new File(sm_dirTestDataTemp + "bambiIn"));
		MessageSender.addDumps("TestSender", inputDumpDir, outputDumpDir);
	}

	/**
	 * @throws Exception
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testSendDetailsDump() throws IllegalArgumentException, Exception
	{
		final DumpDir outputDumpDir = new DumpDir(new File(sm_dirTestDataTemp + "bambiOut"));
		final DumpDir inputDumpDir = new DumpDir(new File(sm_dirTestDataTemp + "bambiIn"));
		MessageSender.addDumps("TestSender", inputDumpDir, outputDumpDir);
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");

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
	public void testSendDetails() throws IllegalArgumentException, IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		assertNull(s.sendDetails(md));
		s.getJMFFactory().setLogLots(true);
		assertNull(s.sendDetails(md));

	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testSendDetailsNull() throws IllegalArgumentException, IOException
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		assertThrows(IllegalArgumentException.class, () -> s.sendDetails(null));
		s.getJMFFactory().setLogLots(true);
		assertThrows(IllegalArgumentException.class, () -> s.sendDetails(null));
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testSendDetailsEmpty() throws IllegalArgumentException, IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "");
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		assertThrows(IllegalArgumentException.class, () -> s.sendDetails(md));
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testReactivate() throws IllegalArgumentException, IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		s.reactivate(md);
		s.getJMFFactory().setLogLots(true);
		s.reactivate(md);
		// no boom
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testProcessResponse() throws IllegalArgumentException, IOException
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		final MessageDetails md2 = new MessageDetails(jmf, mock(IResponseHandler.class), mock(IConverterCallback.class), new HTTPDetails(), "http://nosuchurl");
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		for (int i = 0; i < 42; i++)
		{
			assertEquals(SendReturn.error, s.processResponse(md, null));
			assertEquals(SendReturn.error, s.processResponse(md, mock(HttpURLConnection.class)));
			assertEquals(SendReturn.error, s.processResponse(md2, null));
			assertEquals(SendReturn.error, s.processResponse(md2, mock(HttpURLConnection.class)));
		}

	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testToString()
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		assertNotNull(s.toString());
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
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		assertEquals(SendReturn.error, s.sendHTTP(md));
		s.getJMFFactory().setLogLots(true);
		assertEquals(SendReturn.error, s.sendHTTP(md));
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testDumpDir() throws IllegalArgumentException, IOException
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		assertNull(s.getOuputDumpDir("foo"));
		assertNull(s.getInputDumpDir("foo"));
		final DumpDir outputDumpDir = new DumpDir(new File(sm_dirTestDataTemp + "bambiOut"));
		final DumpDir inputDumpDir = new DumpDir(new File(sm_dirTestDataTemp + "bambiIn"));
		MessageSender.addDumps("foo", inputDumpDir, outputDumpDir);
		assertEquals(outputDumpDir, s.getOuputDumpDir("foo"));
		assertEquals(inputDumpDir, s.getInputDumpDir("foo"));
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
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		md.setFireForget(true);
		assertEquals(SendReturn.removed, s.processProblem(md, SendReturn.error));
		md.setFireForget(false);
		assertEquals(SendReturn.error, s.processProblem(md, SendReturn.error));
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testProcessSuccess() throws IllegalArgumentException, IOException
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		md.setFireForget(true);
		s.processSuccess(md);
		md.setFireForget(false);
		s.processSuccess(md);
		s.getJMFFactory().setLogLots(true);
		s.processSuccess(md);

		assertNotNull(s);
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testProcessMsgResponse() throws IllegalArgumentException, IOException
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		md.setFireForget(true);
		s.processMessageResponse(md, SendReturn.sent);
		s.processMessageResponse(md, SendReturn.removed);
		s.processMessageResponse(md, SendReturn.error);
		md.setFireForget(false);
		s.processMessageResponse(md, SendReturn.sent);
		s.processMessageResponse(md, SendReturn.removed);
		s.processMessageResponse(md, SendReturn.error);
		s.getJMFFactory().setLogLots(true);
		s.processMessageResponse(md, SendReturn.sent);
		s.processMessageResponse(md, SendReturn.removed);
		s.processMessageResponse(md, SendReturn.error);

		assertNotNull(s);
	}

	/**
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *
	 *
	 */
	@Test
	public void testCheckDetails() throws IllegalArgumentException, IOException
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		final MessageDetails md = new MessageDetails(jmf, null, null, null, "http://nosuchurl");
		md.setFireForget(true);
		s.checkDetails(md);
		s.checkDetails(null);
		s.zappFirstMessage();
		s.checkDetails(md);
	}

	/**
	 *
	 *
	 */
	@Test
	public void isRC()
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
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
	public void testGetTimeLast()
	{
		long t0 = System.currentTimeMillis();
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		for (long l = 1; l < 1090; l++)
		{
			t0 -= l * 100000;
			s.timeLastSent = t0;
			assertNotNull(s.getReadableTime());
		}
	}

	/**
	 *
	 *
	 */
	@Test
	public void isRC500()
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
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
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		assertTrue(s.queueMessage(null, null, null, null, null, null));
		assertFalse(s.queueMessage(null, null, null, null, null));
		s.shutDown();
		assertFalse(s.queueMessage(null, null, null, null, null, null));
		assertFalse(s.queueMessage(null, null, null, null, null));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testPersistLocation()
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://");
		assertNull(s.getPersistLocation(false));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testOptimize1()
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://");
		final SenderQueueOptimizer senderQueueOptimizer = s.getSenderQueueOptimizer();
		assertNotNull(senderQueueOptimizer);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testOptimize2()
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://");
		final SenderQueueOptimizer senderQueueOptimizer = s.getSenderQueueOptimizer();
		assertNotNull(senderQueueOptimizer);
		senderQueueOptimizer.optimizeMessage(null);
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.MIS);
		senderQueueOptimizer.optimizeMessage(jmf.getSignal());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testOptimize3()
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://");
		final SenderQueueOptimizer senderQueueOptimizer = s.getSenderQueueOptimizer();
		assertNotNull(senderQueueOptimizer);
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.MIS);
		senderQueueOptimizer.optimizeSingle(jmf.getSignal(), mock(IMessageOptimizer.class));
		s.pause();
		s.queueMessage(jmf, null, "dummy", null, null);
		senderQueueOptimizer.optimizeSingle(jmf.getSignal(), mock(IMessageOptimizer.class));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testPause()
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		s.pause();
		assertNotNull(s.toString());
		s.resume();
		assertNotNull(s.toString());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testQueueMany()
	{
		final MessageSender s = JMFFactory.getInstance().getCreateMessageSender("http://localhost:8080/httpdump/messagesendertest");
		final JDFJMF jmf = new JMFBuilder().buildStatusSignal(EnumDeviceDetails.Details, EnumJobDetails.Full);
		for (int i = 0; i < 420; i++)
			s.queueMessage(jmf, null, null, null, null);

	}

}

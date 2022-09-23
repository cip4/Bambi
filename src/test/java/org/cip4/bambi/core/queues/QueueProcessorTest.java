/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2022 The International Cooperation for the Integration of
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

package org.cip4.bambi.core.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.MessagingException;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.StreamRequest;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.core.queues.QueueProcessor.SubmitQueueEntryHandler;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.jmf.JDFAbortQueueEntryParams;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFMessage.EnumFamily;
import org.cip4.jdflib.jmf.JDFMessage.EnumType;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JDFResumeQueueEntryParams;
import org.cip4.jdflib.jmf.JDFReturnQueueEntryParams;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.mime.MimeWriter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * test for the various queue processor functions
 *
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG 03.12.2008
 */
public class QueueProcessorTest extends BambiTestCase
{
	String queueEntryId = "qe_130102_112609938_007349";
	Lock sequential = new ReentrantLock();

	/**
	 * @throws IOException
	 * @throws MessagingException
	 */
	@Test
	@Ignore
	public void testReturnQE() throws IOException, MessagingException
	{
		final JDFDoc docJMF = new JDFDoc("JMF");
		final JDFJMF jmf = docJMF.getJMFRoot();
		jmf.setSenderID("DeviceID");
		final JDFCommand com = (JDFCommand) jmf.appendMessageElement(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.ReturnQueueEntry);
		final JDFReturnQueueEntryParams returnQEParams = com.appendReturnQueueEntryParams();

		final String queueEntryID = "qe1";
		returnQEParams.setQueueEntryID(queueEntryID);
		final JDFDoc docJDF = JDFDoc.parseFile("C:\\data\\jdf\\foo.jdf");
		returnQEParams.setURL("cid:dummy"); // will be overwritten by buildMimePackage
		final MimeWriter mw = new MimeWriter();
		mw.buildMimePackage(docJMF, docJDF, false);
		final MIMEDetails mimeDetails = new MIMEDetails();
		mimeDetails.transferEncoding = UrlUtil.BINARY;
		mimeDetails.modifyBoundarySemicolon = false;
		mw.setMIMEDetails(mimeDetails);
		final StreamRequest req = new StreamRequest(mw.getInputStream());
		req.setContentType(MimeUtil.MULTIPART_RELATED);
		final XMLResponse resp = bambiContainer.processStream(req);

		assertNotNull(resp.getXML());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testRemoveQE()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildRemoveQueueEntry(queueEntryId);
		final QueueProcessor qp = getDevice().getQueueProcessor();
		qp.new RemoveQueueEntryHandler().handleMessage(jmf.getMessageElement(null, null, 0), null);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testConstruct()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		assertNotNull(qp);
	}

	/**
	*
	*
	*/
	@Test
	public void testGetQueue()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFQueue queue = qp.getQueue();
		assertNotNull(queue);
		qp.setQueue(null);
		assertNull(qp.getQueue());
		qp.setQueue(queue);
	}

	/**
	*
	*
	*/
	@Test
	public void testWaitForEntryNull()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		assertNull(qp.waitForEntry(null, null, null));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testGetDocFromMessageBad()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		SubmitQueueEntryHandler submitQueueEntryHandler = qp.new SubmitQueueEntryHandler();
		submitQueueEntryHandler.getDocFromMessage(null);
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry(null, null);
		submitQueueEntryHandler.getDocFromMessage(jmf.getMessageElement(null, null, 0));

	}

	/**
	 *
	 *
	 */
	@Test
	public void testAddEntryMany()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		for (int i = 0; i < 10; i++)
		{
			final JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
			final JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
			final JDFNode jdf = JDFNode.createRoot();
			jdf.setJobID("J" + i);
			final JDFDoc doc = jdf.getOwnerDocument_JDFElement();
			final JDFQueueEntry qe = qp.addEntry(c, r, doc);
			assertNotNull(qe);
		}
		for (final Object o : EnumQueueEntryStatus.getEnumList())
			log.info(o.toString() + " " + qp.getQueue().numEntries((EnumQueueEntryStatus) o));
		assertEquals(10, qp.getQueue().getQueueSize(), 1);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testAddEntryManyQueue()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		for (int i = 0; i < 10; i++)
		{
			final JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
			final JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
			final JDFNode jdf = JDFNode.createRoot();
			jdf.setJobID("J" + i);
			final JDFDoc doc = jdf.getOwnerDocument_JDFElement();
			qp.addEntry(c, r, doc);
			final JDFQueue queue = r.getQueue(0);
			assertNull(queue.getQueueEntry(0));
			assertEquals(i + 1, queue.getQueueSize());
			assertTrue(BambiNSExtension.getTotal(qp.getQueue()) > i);

		}
	}

	/**
	 *
	 *
	 */
	@Test
	public void testAddEntry()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
		final JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueueEntry qe = qp.addEntry(c, r, doc);
		assertNotNull(qe);
		assertTrue(BambiNSExtension.getTotal(qp.getQueue()) > 0);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testMessageQEAbort()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1");
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.AbortQueueEntry).getCommand(0);
		final JDFAbortQueueEntryParams aqp = (JDFAbortQueueEntryParams) c.appendElement(ElementName.ABORTQUEUEENTRYPARAMS);
		aqp.getCreateQueueFilter().appendQueueEntryDef("q1");
		assertEquals(qe, qp.getMessageQueueEntry(c, null));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testMessageQEAbortJobID()
	{
		final QueueProcessor qp = new QueueProcessor(getDevice());
		JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1a");
		qe.setJobID("j1");
		qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q2a");
		qe.setJobID("j2");
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.AbortQueueEntry).getCommand(0);
		final JDFAbortQueueEntryParams aqp = (JDFAbortQueueEntryParams) c.appendElement(ElementName.ABORTQUEUEENTRYPARAMS);
		aqp.getCreateQueueFilter().setJobID("j2");
		assertEquals(qe, qp.getMessageQueueEntry(c, null));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testMessageQEResume()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1res");
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.ResumeQueueEntry).getCommand(0);
		final JDFResumeQueueEntryParams aqp = (JDFResumeQueueEntryParams) c.appendElement(ElementName.RESUMEQUEUEENTRYPARAMS);
		aqp.getCreateQueueFilter(0).appendQueueEntryDef("q1res");
		assertEquals(qe.getQueueEntryID(), qp.getMessageQueueEntry(c, null).getQueueEntryID());
		assertEquals(qe.getQueueEntryID(), qp.getMessageQueueEntry(c, null).getQueueEntryID());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testMessageQEEmpty()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1empty");
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.ResumeQueueEntry).getCommand(0);
		final JDFResumeQueueEntryParams aqp = (JDFResumeQueueEntryParams) c.appendElement(ElementName.RESUMEQUEUEENTRYPARAMS);
		aqp.getCreateQueueFilter(0);
		assertNull(qp.getMessageQueueEntry(c, null));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testSuspendQE()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSuspendQueueEntry(queueEntryId);
		final QueueProcessor qp = getDevice().getQueueProcessor();
		qp.new SuspendQueueEntryHandler().handleMessage(jmf.getMessageElement(null, null, 0), null);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testHoldQueueEntry()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildHoldQueueEntry(queueEntryId);
		final QueueProcessor qp = getDevice().getQueueProcessor();
		qp.new HoldQueueEntryHandler().handleMessage(jmf.getMessageElement(null, null, 0), null);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testResumeQE()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildResumeQueueEntry(queueEntryId);
		final QueueProcessor qp = getDevice().getQueueProcessor();
		qp.new ResumeQueueEntryHandler().handleMessage(jmf.getMessageElement(null, null, 0), null);
	}

	/**
	 * @see org.cip4.bambi.core.BambiContainerTest#setUp()
	 */
	@Override
	@Before
	public void setUp() throws Exception
	{
		wantContainer = false;
		super.setUp();
		workerURLBase = "http://localhost:44482/SimWorker/jmf/simIDP";
		deviceID = "simIDP";
		sequential.lock();

	}

	@Override
	public void tearDown() throws Exception
	{
		sequential.unlock();
		super.tearDown();
	}

}

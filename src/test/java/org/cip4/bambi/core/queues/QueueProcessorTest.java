/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2024 The International Cooperation for the Integration of
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.mail.MessagingException;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.AbstractDevice;
import org.cip4.bambi.core.BambiNSExtension;
import org.cip4.bambi.core.IDeviceProperties.QERetrieval;
import org.cip4.bambi.core.StreamRequest;
import org.cip4.bambi.core.XMLResponse;
import org.cip4.bambi.core.queues.QueueProcessor.CanExecuteCallBack;
import org.cip4.bambi.core.queues.QueueProcessor.QueueEntryReturn;
import org.cip4.bambi.core.queues.QueueProcessor.SubmitQueueEntryHandler;
import org.cip4.bambi.proxy.ProxyDevice;
import org.cip4.jdflib.auto.JDFAutoQueue.EnumQueueStatus;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.VString;
import org.cip4.jdflib.extensions.XJDFConstants;
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
import org.cip4.jdflib.util.JDFDate;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.ThreadUtil;
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
	public void testQEReturn1()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe = q.appendQueueEntry();
		qe.setQueueEntryID("q12345");
		qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
		qe.setSubmissionTime(new JDFDate());
		final QueueEntryReturn r = qp.new QueueEntryReturn(qe, EnumQueueEntryStatus.Completed);
		assertFalse(r.returnJMF(null, null));
		assertFalse(r.returnJMF(null, null, 4));
		assertFalse(r.returnJMF(doc, null));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testReallyReturnXJDF()
	{
		final AbstractDevice device = getDevice();
		final QueueProcessor qp = device.getQueueProcessor();
		JDFDoc doc = JDFElement.createRoot(XJDFConstants.XJDF).getOwnerDocument_JDFElement();
		doc = device.getCallback(null).prepareJDFForBambi(doc);
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe = q.appendQueueEntry();
		qe.setQueueEntryID("q12345");
		qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
		qe.setSubmissionTime(new JDFDate());
		final QueueEntryReturn r = qp.new QueueEntryReturn(qe, EnumQueueEntryStatus.Completed);
		r.returnQueueEntry(new VString(), doc);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testQueueDelta()
	{
		final AbstractDevice device = getDevice();
		final QueueProcessor qp = device.getQueueProcessor();
		assertNotNull(qp.getQueueDelta().toString());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testQEReturnBadNS()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe = q.appendQueueEntry();
		qe.setQueueEntryID("q12346");
		qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
		qe.setAttributeRaw(AttributeName.XMLNS, "foo");
		qe.setSubmissionTime(new JDFDate());
		final QueueEntryReturn r = qp.new QueueEntryReturn(qe, EnumQueueEntryStatus.Completed);
		assertFalse(r.returnJMF(null, null));
		assertFalse(r.returnJMF(doc, null));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testReturnQE2()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe = q.appendQueueEntry();
		qe.setQueueEntryID("q12345");
		qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
		qe.setSubmissionTime(new JDFDate());
		BambiNSExtension.setReturnJMF(qe, "http://foo");
		final QueueEntryReturn r = qp.new QueueEntryReturn(qe, EnumQueueEntryStatus.Completed);
		assertFalse(r.returnQueueEntry(null, null));
		assertFalse(r.returnQueueEntry(null, doc));
		assertFalse(r.returnQueueEntry(new VString(), doc));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testStoreDoc()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe = q.appendQueueEntry();
		qe.setQueueEntryID("q12345");
		qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
		qe.setSubmissionTime(new JDFDate());
		assertTrue(qp.storeDoc(qe, doc, null, null));
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
		qp.new RemoveQueueEntryHandler().handleMessage(jmf.getMessageElement(null, null, 0), JDFJMF.createJMF(EnumFamily.Response, EnumType.RemoveQueueEntry).getResponse());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testTotalCount()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		assertTrue(qp.getTotalEntryCount() >= 0);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testResubmit()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildResubmitQueueEntry(queueEntryId, "http;foo");
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFMessage me = jmf.getMessageElement(null, null, 0);
		qp.new ResubmitQueueEntryHandler().handleMessage(me, JDFJMF.createJMF(EnumFamily.Response, EnumType.ResubmitQueueEntry).getResponse());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testNewJDF()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildNewJDFQuery("q1", "p1");
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFMessage me = jmf.getMessageElement(null, null, 0);
		qp.new NewJDFQueryHandler().handleMessage(me, JDFJMF.createJMF(EnumFamily.Response, EnumType.NewJDF).getResponse());
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
	public void testReadQueueFile()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		qp.readQueueFile(); // no boom
	}

	/**
	 *
	 *
	 */
	@Test
	public void testUpdateNextEntry()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		assertNull(qp.updateNextEntry(null));
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe = q.appendQueueEntry();
		qe.setQueueEntryID("q1234u");
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		assertTrue(qp.storeDoc(qe, doc, null, null));

		assertNotNull(qp.updateNextEntry(qe));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testUpdateWaiting()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		assertNull(qp.updateNextEntry(null));
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe = q.appendQueueEntry();
		qe.setQueueEntryID("q1234u");

		qp.updateWaiting(qe, EnumQueueEntryStatus.Waiting, "foo", q);
		assertEquals("foo", qe.getStatusDetails());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testUpdateSynch()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		assertNull(qp.updateNextEntry(null));
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe = q.appendQueueEntry();
		qe.setQueueEntryID("q1234u");
		for (final Object o : EnumQueueEntryStatus.getEnumList())
		{
			final EnumQueueEntryStatus s = (EnumQueueEntryStatus) o;
			qp.updateWaiting(qe, s, "foo", q);
			assertEquals("foo", qe.getStatusDetails());
			assertEquals(s, qe.getQueueEntryStatus());
		}
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
	@SuppressWarnings("deprecation")
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
		final SubmitQueueEntryHandler submitQueueEntryHandler = qp.new SubmitQueueEntryHandler();
		submitQueueEntryHandler.getDocFromMessage(null);
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry(null, null);
		submitQueueEntryHandler.getDocFromMessage(jmf.getMessageElement(null, null, 0));

	}

	/**
	 *
	 *
	 */
	@Test
	public void testSynchBad()
	{
		final AbstractDevice device = getDevice();
		device.setSynchronous(true);
		final QueueProcessor qp = device.getQueueProcessor();
		final SubmitQueueEntryHandler submitQueueEntryHandler = qp.new SubmitQueueEntryHandler();
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry(null, null);

		assertFalse(submitQueueEntryHandler.doSynchronous(jmf.getMessage(0), null, null));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testSynch()
	{
		final AbstractDevice device = getDevice();
		device.setSynchronous(true);
		final QueueProcessor qp = device.getQueueProcessor();
		final SubmitQueueEntryHandler submitQueueEntryHandler = qp.new SubmitQueueEntryHandler();
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry(null, null);

		final JDFNode n = JDFNode.createRoot();
		n.setJobID("j1");
		assertTrue(submitQueueEntryHandler.doSynchronous(jmf.getMessage(0), null, n.getOwnerDocument_JDFElement()));

	}

	/**
	 *
	 *
	 */
	@Test
	public void testSynchMany()
	{
		final AbstractDevice device = getDevice();
		device.setSynchronous(true);
		final QueueProcessor qp = device.getQueueProcessor();
		final SubmitQueueEntryHandler submitQueueEntryHandler = qp.new SubmitQueueEntryHandler();
		for (int i = 0; i < 100; i++)
		{
			final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry(null, null);

			final JDFNode n = JDFNode.createRoot();
			n.setJobID("j" + i);
			assertTrue(submitQueueEntryHandler.doSynchronous(jmf.getMessage(0), null, n.getOwnerDocument_JDFElement()));
		}
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
			final JDFQueue queue = qp.getQueue();
			assertNull(r.getQueue(0));
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
		final JDFQueueEntry qe2 = qp.addEntry(c, null, doc);
		assertNotNull(qe2);
		assertTrue(BambiNSExtension.getTotal(qp.getQueue()) > 0);
	}

	/**
	*
	*
	*/
	@Test
	public void testAddEntryBad()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		qp.getQueue().setQueueStatus(EnumQueueStatus.Closed);
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
		final JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		JDFQueueEntry qe = qp.addEntry(c, r, doc);
		assertNull(qe);
		assertEquals(112, r.getReturnCode());
		qe = qp.addEntry(c, null, doc);
		assertNull(qe);
		r.setReturnCode(0);
		qe = qp.addEntry(null, r, doc);
		assertNull(qe);
		assertEquals(7, r.getReturnCode());
		r.setReturnCode(0);
		qe = qp.addEntry(c, r, null);
		assertNull(qe);
		assertEquals(7, r.getReturnCode());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testMessageQEAbortOld()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1old");
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.AbortQueueEntry).getCommand(0);
		c.appendQueueEntryDef().setQueueEntryID("q1old");
		assertEquals(qe, qp.getMessageQueueEntry(c, null));
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
		final AbstractDevice dev = getDevice();
		final QueueProcessor qp = dev.getQueueProcessor();
		final JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1res");
		ThreadUtil.sleep(2);
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.ResumeQueueEntry).getCommand(0);
		final JDFResumeQueueEntryParams aqp = (JDFResumeQueueEntryParams) c.appendElement(ElementName.RESUMEQUEUEENTRYPARAMS);
		aqp.getCreateQueueFilter(0).appendQueueEntryDef("q1res");
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

		sequential.lock();
		wantContainer = false;
		super.setUp();
		workerURLBase = "http://localhost:44482/SimWorker/jmf/simIDP";
		deviceID = "simIDP";
	}

	@Override
	public void tearDown() throws Exception
	{
		super.tearDown();
		sequential.unlock();
	}

	/**
	 *
	 *
	 */
	@Test
	public void testGetNextEntry()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
		final JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueueEntry qe = qp.addEntry(c, r, doc);
		assertNotNull(qe);
		assertNotNull(qp.getNextEntry(qp.getParent().getDeviceID(), QERetrieval.BOTH));
		final QueueProcessor qp2 = spy(qp);
		when(qp2.getCanExecuteCallback(any())).thenReturn(null);
		qp2.addEntry(c, r, doc);
		assertNotNull(qp2.getNextEntry(qp.getParent().getDeviceID(), QERetrieval.BOTH));
		assertNull(qp2.getNextEntry(qp.getParent().getDeviceID(), QERetrieval.BOTH));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testCanExecute()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		assertNotNull(qp.getCanExecuteCallback("33").toString());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testCanExecuteClone()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		assertNotNull(qp.getCanExecuteCallback("33").clone().toString());
	}

	/**
	*
	*
	*/
	@Test
	public void testCanExecute2()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final CanExecuteCallBack canExecuteCallback = qp.new CanExecuteCallBack("a", "b");
		assertFalse(canExecuteCallback.canExecute(null));
		final CanExecuteCallBack c2 = qp.new CanExecuteCallBack(null, "b");
		final JDFQueueEntry qe = (JDFQueueEntry) JDFElement.createRoot(ElementName.QUEUEENTRY);

		assertTrue(c2.canExecute(qe));
		c2.setCheckSubmitted(true);
		qe.setStatusDetails(ProxyDevice.SUBMITTED);
		qe.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
		assertFalse(c2.canExecute(qe));
		c2.setCheckSubmitted(false);
		assertTrue(c2.canExecute(qe));
		qe.setAttribute("b", "c");
		assertFalse(c2.canExecute(qe));
	}

	/**
	 *
	 *
	 */
	@Test
	public void testGetIQEntry()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
		final JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueueEntry qe = qp.addEntry(c, r, doc);
		assertNotNull(qe);
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe2 = q.getQueueEntry(0);
		qe2.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
		assertNotNull(qp.getIQueueEntry(qe2));
	}

	@Test
	public void testGetIQEntryNull()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
		final JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueueEntry qe = qp.addEntry(c, r, doc);
		assertNotNull(qe);
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe2 = q.getQueueEntry(0);
		qe2.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
		BambiNSExtension.setDocURL(qe2, null);
		assertNull(qp.getIQueueEntry(qe2));
	}

	@Test
	public void testHasWaiting()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		qp.getQueue().flush();
		assertFalse(qp.hasWaiting());
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
		final JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueueEntry qe = qp.addEntry(c, r, doc);
		assertNotNull(qe);
		assertTrue(qp.hasWaiting());
	}

	@Test
	public void testGetIQEntryBadFile()
	{
		final QueueProcessor qp = getDevice().getQueueProcessor();
		final JMFBuilder jmfBuilder = new JMFBuilder();
		final JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
		final JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
		final JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		final JDFQueueEntry qe = qp.addEntry(c, r, doc);
		assertNotNull(qe);
		final JDFQueue q = qp.getQueue();
		final JDFQueueEntry qe2 = q.getQueueEntry(0);
		qe2.setQueueEntryStatus(EnumQueueEntryStatus.Waiting);
		BambiNSExtension.setDocURL(qe2, "notthere");
		assertNull(qp.getIQueueEntry(qe2));
	}
}

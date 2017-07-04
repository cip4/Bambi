/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2017 The International Cooperation for the Integration of
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

import java.io.IOException;

import javax.mail.MessagingException;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.StreamRequest;
import org.cip4.bambi.core.XMLResponse;
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
import org.cip4.jdflib.util.UrlPart;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.mime.MimeWriter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * test for the various queue processor functions
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 *
 * 03.12.2008
 */
public class QueueProcessorTest extends BambiTestCase
{
	String queueEntryId = "qe_130102_112609938_007349";

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
	@Ignore
	public void testRemoveQE()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildRemoveQueueEntry(queueEntryId);
		final UrlPart p = jmf.getOwnerDocument_JDFElement().write2HttpURL(UrlUtil.stringToURL(getWorkerURL()), null);
		p.buffer();
	}

	/**
	 *
	 *
	 */
	@Test
	public void testConstruct()
	{
		QueueProcessor qp = new QueueProcessor(getDevice());
		assertNotNull(qp);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testAddEntryMany()
	{
		QueueProcessor qp = new QueueProcessor(getDevice());
		JMFBuilder jmfBuilder = new JMFBuilder();
		for (int i = 0; i < 100; i++)
		{
			JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
			JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
			JDFNode jdf = JDFNode.createRoot();
			jdf.setJobID("J" + i);
			JDFDoc doc = jdf.getOwnerDocument_JDFElement();
			JDFQueueEntry qe = qp.addEntry(c, r, doc);
			assertNotNull(qe);
		}
		assertEquals(100, qp.getQueue().getQueueSize());
	}

	/**
	 *
	 *
	 */
	@Test
	public void testAddEntryManyQueue()
	{
		QueueProcessor qp = new QueueProcessor(getDevice());
		JMFBuilder jmfBuilder = new JMFBuilder();
		for (int i = 0; i < 100; i++)
		{
			JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
			JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
			JDFNode jdf = JDFNode.createRoot();
			jdf.setJobID("J" + i);
			JDFDoc doc = jdf.getOwnerDocument_JDFElement();
			qp.addEntry(c, r, doc);
			JDFQueue queue = r.getQueue(0);
			assertNull(queue.getQueueEntry(0));
			assertEquals(i + 1, queue.getQueueSize());
		}
	}

	/**
	 *
	 *
	 */
	@Test
	public void testAddEntry()
	{
		QueueProcessor qp = new QueueProcessor(getDevice());
		JMFBuilder jmfBuilder = new JMFBuilder();
		JDFCommand c = jmfBuilder.buildSubmitQueueEntry("url").getCommand(0);
		JDFResponse r = jmfBuilder.createJMF(EnumFamily.Response, EnumType.SubmitQueueEntry).getResponse(0);
		JDFDoc doc = JDFNode.createRoot().getOwnerDocument_JDFElement();
		JDFQueueEntry qe = qp.addEntry(c, r, doc);
		assertNotNull(qe);
	}

	/**
	 *
	 *
	 */
	@Test
	public void testMessageQEAbort()
	{
		QueueProcessor qp = new QueueProcessor(getDevice());
		JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1");
		JMFBuilder jmfBuilder = new JMFBuilder();
		JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.AbortQueueEntry).getCommand(0);
		JDFAbortQueueEntryParams aqp = (JDFAbortQueueEntryParams) c.appendElement(ElementName.ABORTQUEUEENTRYPARAMS);
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
		QueueProcessor qp = new QueueProcessor(getDevice());
		JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1");
		qe.setJobID("j1");
		qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q2");
		qe.setJobID("j2");
		JMFBuilder jmfBuilder = new JMFBuilder();
		JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.AbortQueueEntry).getCommand(0);
		JDFAbortQueueEntryParams aqp = (JDFAbortQueueEntryParams) c.appendElement(ElementName.ABORTQUEUEENTRYPARAMS);
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
		QueueProcessor qp = new QueueProcessor(getDevice());
		JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1");
		JMFBuilder jmfBuilder = new JMFBuilder();
		JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.ResumeQueueEntry).getCommand(0);
		JDFResumeQueueEntryParams aqp = (JDFResumeQueueEntryParams) c.appendElement(ElementName.RESUMEQUEUEENTRYPARAMS);
		aqp.getCreateQueueFilter(0).appendQueueEntryDef("q1");
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
		QueueProcessor qp = new QueueProcessor(getDevice());
		JDFQueueEntry qe = qp.getQueue().appendQueueEntry();
		qe.setQueueEntryID("q1");
		JMFBuilder jmfBuilder = new JMFBuilder();
		JDFCommand c = jmfBuilder.createJMF(EnumFamily.Command, EnumType.ResumeQueueEntry).getCommand(0);
		JDFResumeQueueEntryParams aqp = (JDFResumeQueueEntryParams) c.appendElement(ElementName.RESUMEQUEUEENTRYPARAMS);
		aqp.getCreateQueueFilter(0);
		assertNull(qp.getMessageQueueEntry(c, null));
	}

	/**
	 *
	 *
	 */
	@Test
	@Ignore
	public void testSuspendQE()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSuspendQueueEntry(queueEntryId);
		final UrlPart p = jmf.getOwnerDocument_JDFElement().write2HttpURL(UrlUtil.stringToURL(getWorkerURL()), null);
		p.buffer();
	}

	/**
	 *
	 *
	 */
	@Test
	@Ignore
	public void testResumeQE()
	{
		final JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildResumeQueueEntry(queueEntryId);
		final UrlPart p = jmf.getOwnerDocument_JDFElement().write2HttpURL(UrlUtil.stringToURL(getWorkerURL()), null);
		p.buffer();
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
	}

}

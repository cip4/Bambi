/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2009 The International Cooperation for the Integration of 
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

package org.cip4.bambi.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.HttpURLConnection;

import javax.mail.Multipart;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.bambi.core.messaging.JMFFactory.CallURL;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFDeviceInfo;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.util.MimeUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 * 
 */
public class JMFFactoryTest extends BambiTestCase
{

	/**
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 * @throws Exception
	 */
	@Override
    @Before
	public void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * 
	 */
    @Test
	public void testCallURLGetBaseURL()
	{
		CallURL cu = new CallURL("http://foo/bar?abc");
		assertEquals(cu.getBaseURL(), "http://foo/bar");
		cu = new CallURL("http://foo/bar/abc");
		assertEquals(cu.getBaseURL(), "http://foo/bar");
		cu = new CallURL("http://foo");
		assertEquals(cu.getBaseURL(), "http://foo");
	}

	/**
	 * 
	 */
    @Test
	public void testStatus()
	{
		JDFJMF jmf = new JMFBuilder().buildStatus(EnumDeviceDetails.Brief, EnumJobDetails.Brief);
		JDFResponse resp = sendToURL(jmf, getWorkerURL());
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		final JDFDeviceInfo di = resp.getDeviceInfo(0);
		assertTrue(di != null);

		jmf = new JMFBuilder().buildStatus(EnumDeviceDetails.Brief, EnumJobDetails.Brief);
		resp = sendToURL(jmf, getWorkerURL());
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		resp.getDeviceInfo(0);
		assertTrue(di != null);
	}

	/**
	 * 
	 */
    @Test
	public void testResumeQueueEntry()
	{
		final JDFJMF jmf = new JMFBuilder().buildResumeQueueEntry("12345");
		final JDFResponse resp = sendToURL(jmf, getWorkerURL());
		assertNotNull(resp);
		assertEquals(105, resp.getReturnCode());
	}

	/**
	 * 
	 */
    @Test
	public void testRemoveQueueEntry()
	{
		final JDFJMF jmf = new JMFBuilder().buildRemoveQueueEntry("12345");
		final JDFResponse resp = sendToURL(jmf, getWorkerURL());
		assertNotNull(resp);
		assertEquals(105, resp.getReturnCode());
	}

	/**
	 * 
	 */
    @Test
	public void testQueueStatus()
	{
		final JDFJMF jmf = new JMFBuilder().buildQueueStatus();
		final JDFResponse resp = sendToURL(jmf, getWorkerURL());
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		final JDFQueue q = resp.getQueue(0);
		assertNotNull(q);
		final int qSize = q.getQueueSize();
		if (qSize > 0)
		{
			final VElement v = q.getQueueEntryVector();
			assertEquals(v.size(), qSize);
			for (int i = 0; i < qSize; i++)
			{
				final JDFQueueEntry qe = (JDFQueueEntry) v.elementAt(i);
				assertTrue(qe != null);
			}
		}
		System.out.println("Q Size: " + qSize);
	}

	/**
	 * @throws Exception
	 */
    @Test
	public void testMultiStatus() throws Exception
	{
		final long t = System.currentTimeMillis();
		for (int i = 0; i < 100; i++)
		{
			final long t1 = System.currentTimeMillis();
			System.out.println("Pre status," + i);
			testQueueStatus();
			final long t2 = System.currentTimeMillis();
			System.out.println("Post status," + i + " single: " + (t2 - t1) + " total: " + (t2 - t));

		}
	}

	/**
	 * @throws Exception
	 */
    @Test
	public void testAbortAll() throws Exception
	{
		final long t = System.currentTimeMillis();
		final JMFFactory factory = JMFFactory.getInstance();
		JDFJMF jmf = new JMFBuilder().buildQueueStatus();
		JDFResponse resp = sendToURL(jmf, getWorkerURL());
		JDFQueue q = resp.getQueue(0);
		assertNotNull(q);
		int queueSize = q.getQueueSize();
		final VElement v = q.getQueueEntryVector();
		int numRunning = 0;
		for (int i = 0; i < v.size(); i++)
		{
			final long t1 = System.currentTimeMillis();
			System.out.println("Pre abort," + i);
			final String qeID = ((JDFQueueEntry) v.elementAt(i)).getQueueEntryID();
			jmf = new JMFBuilder().buildAbortQueueEntry(qeID);
			resp = factory.send2URLSynchResp(jmf, getWorkerURL(), null, null, 2000);
			assertNotNull(resp);
			assertEquals(0, resp.getReturnCode());

			jmf = new JMFBuilder().buildRemoveQueueEntry(qeID);
			resp = factory.send2URLSynchResp(jmf, getWorkerURL(), null, null, 2000);
			final long t2 = System.currentTimeMillis();
			assertNotNull(resp);
			// a return code of 106="Failed, QueueEntry is already executing" is possible
			// and does not indicate an error
			final int retCode = resp.getReturnCode();
			if (retCode == 106)
			{
				numRunning++;
			}
			if (retCode != 0 && retCode != 106)
			{
				fail("RemoveQueueEntry failed, return code is " + retCode);
			}
			System.out.println("Post abort," + i + " single: " + (t2 - t1) + " total: " + (t2 - t));
		}
		// give the device some time to process the Abort/RemoveQE's
		Thread.sleep(1000);

		jmf = new JMFBuilder().buildQueueStatus();
		resp = factory.send2URLSynchResp(jmf, getWorkerURL(), null, null, 2000);
		q = resp.getQueue(0);
		assertNotNull(q);
		queueSize = q.getQueueSize();
		// only then-running now-aborted QueueEntries are allowed to remain, all others should
		// be removed
		assertEquals(numRunning, queueSize);
		for (int i = 0; i < queueSize; i++)
		{
			final JDFQueueEntry qe = q.getQueueEntry(i);
			final EnumQueueEntryStatus status = qe.getQueueEntryStatus();
			if (EnumQueueEntryStatus.Aborted.equals(status))
			{
				fail("QueueEntryStatus is " + status.getName() + ", but should be Aborted");
			}
			// clean up
			jmf = new JMFBuilder().buildRemoveQueueEntry(qe.getQueueEntryID());
			resp = factory.send2URLSynchResp(jmf, getWorkerURL(), null, null, 2000);
		}
	}

	/**
	 * @throws Exception
	 */
    @Test
	public void testSubmitQueueEntry_MIME() throws Exception
	{
		// get number of QueueEntries before submitting
		final JDFJMF jmfStat = new JMFBuilder().buildQueueStatus();
		final JDFResponse resp = sendToURL(jmfStat, getWorkerURL());
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		final JDFQueue q = resp.getQueue(0);
		assertNotNull(q);

		// build SubmitQueueEntry
		final JDFDoc docJMF = new JDFDoc("JMF");
		final JDFJMF jmf = docJMF.getJMFRoot();
		final JDFCommand com = (JDFCommand) jmf.appendMessageElement(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.SubmitQueueEntry);
		final JDFQueueSubmissionParams qsp = com.appendQueueSubmissionParams();
		qsp.setURL("cid:" + sm_dirTestData + "Elk_ConventionalPrinting.jdf");

		final JDFParser p = new JDFParser();
		final JDFDoc docJDF = p.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf");
		final Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, true);

		try
		{
			final HttpURLConnection response = MimeUtil.writeToURL(mp, getWorkerURL());
			assertEquals(200, response.getResponseCode());
		}
		catch (final Exception e)
		{
			fail(e.getMessage()); // fail on exception
		}

		abortRemoveAll(getWorkerURL());
	}
}

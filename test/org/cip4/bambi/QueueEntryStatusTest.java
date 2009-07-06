/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2008 The International Cooperation for the Integration of 
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

package org.cip4.bambi;

import java.net.HttpURLConnection;

import javax.mail.Multipart;

import org.cip4.bambi.core.messaging.JMFBuilder;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.MimeUtil;

public class QueueEntryStatusTest extends BambiTestCase
{

	private JDFQueue getQueue()
	{
		final JMFFactory factory = JMFFactory.getJMFFactory();
		final JDFJMF jmf = new JMFBuilder().buildQueueStatus();
		final JDFResponse resp = factory.send2URLSynchResp(jmf, simWorkerUrl, null, null, 2000);
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());

		final JDFQueue qu = resp.getQueue(0);
		assertTrue(qu != null);
		return qu;
	}

	private JDFQueueEntry getRunningQueueEntry()
	{
		final JDFQueue qu = getQueue();
		final VElement qev = qu.getQueueEntryVector();
		assertNotNull(qev);

		// find a runnig QueueEntry
		JDFQueueEntry runningQE = null;
		for (int i = 0; i < qev.size(); i++)
		{
			final JDFQueueEntry qe = (JDFQueueEntry) qev.get(i);
			if (EnumQueueEntryStatus.Running.equals(qe.getQueueEntryStatus()))
			{
				runningQE = qe;
				break;
			}
		}
		return runningQE;
	}

	private EnumQueueEntryStatus getQueueEntryStatus(final String queueEntryID)
	{
		final JDFQueue qu = getQueue();
		final JDFQueueEntry que = qu.getQueueEntry(queueEntryID);
		return que == null ? null : que.getQueueEntryStatus();
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		abortRemoveAll(simWorkerUrl);

		JDFQueueEntry runningQE = null;

		// submit a new, fresh qe
		System.out.println("submitting new QueueEntry");
		final JDFDoc docJMF = new JDFDoc("JMF");
		final JDFJMF jmfSubmit = docJMF.getJMFRoot();
		final JDFCommand com = (JDFCommand) jmfSubmit.appendMessageElement(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.SubmitQueueEntry);
		final JDFQueueSubmissionParams qsp = com.appendQueueSubmissionParams();
		qsp.setURL("cid:" + sm_dirTestData + "Elk_ConventionalPrinting.jdf");

		final JDFParser p = new JDFParser();
		final JDFDoc docJDF = p.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf");
		final Multipart mp = MimeUtil.buildMimePackage(docJMF, docJDF, true);

		try
		{
			final HttpURLConnection response = MimeUtil.writeToURL(mp, simWorkerUrl);
			assertEquals(200, response.getResponseCode());
		}
		catch (final Exception e)
		{
			fail(e.getMessage());
		}

		// give the device some time to start processing
		boolean hasRunningQE = false;
		int counter = 0;
		while (counter < 10 && !hasRunningQE)
		{
			try
			{
				Thread.sleep(750);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}

			runningQE = getRunningQueueEntry();
			if (runningQE != null)
			{
				hasRunningQE = true;
			}
			counter++;
		}

		assertTrue(hasRunningQE);
	}

	public void testSuspendResumeQE()
	{
		final JDFQueueEntry runningQE = getRunningQueueEntry();
		assertNotNull(runningQE);
		final String qeID = runningQE.getQueueEntryID();

		final JMFFactory factory = JMFFactory.getJMFFactory();
		JDFJMF jmf = new JMFBuilder().buildSuspendQueueEntry(qeID);
		JDFResponse resp = factory.send2URLSynchResp(jmf, simWorkerUrl, null, null, 2000);
		assertEquals(0, resp.getReturnCode());
		// give the device some time to suspend the QE
		boolean hasSuspended = false;
		byte suspCounter = 0;
		while (!hasSuspended && suspCounter < 10)
		{
			try
			{
				Thread.sleep(750);
			}
			catch (final InterruptedException e)
			{
				fail("interrupted during Thread.sleep()");
			}
			if (EnumQueueEntryStatus.Suspended.equals(getQueueEntryStatus(qeID)))
			{
				hasSuspended = true;
			}
			suspCounter++;
		}
		assertTrue(hasSuspended);

		jmf = new JMFBuilder().buildResumeQueueEntry(qeID);
		resp = factory.send2URLSynchResp(jmf, simWorkerUrl, null, null, 2000);
		assertEquals(0, resp.getReturnCode());
		boolean hasSucceeded = false;
		EnumQueueEntryStatus status = null;
		int counter = 0;
		while (counter < 10 && !hasSucceeded)
		{
			// give the device some time to resume the QE
			try
			{
				Thread.sleep(750);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
			// now the qe should be either Waiting or Running, Completed is allowed as well
			status = getQueueEntryStatus(qeID);
			if (status.equals(EnumQueueEntryStatus.Running) || status.equals(EnumQueueEntryStatus.Waiting) || status.equals(EnumQueueEntryStatus.Completed))
			{
				assertTrue(true);
				hasSucceeded = true;
			}
			counter++;
		}

		if (!hasSucceeded)
		{
			if (status == null)
			{
				fail("status is null");
			}
			else
			{
				fail("status is " + status.getName() + ", should be Running or Waiting");
			}
		}

	}

	public void testAbortRemoveQE()
	{
		final JDFQueueEntry runningQE = getRunningQueueEntry();
		assertNotNull(runningQE);
		final String qeID = runningQE.getQueueEntryID();

		JDFJMF jmf = new JMFBuilder().buildAbortQueueEntry(qeID);
		final JMFFactory factory = JMFFactory.getJMFFactory();
		JDFResponse resp = factory.send2URLSynchResp(jmf, simWorkerUrl, null, null, 2000);
		assertEquals(0, resp.getReturnCode());
		// give the device some time to abort the QE
		try
		{
			Thread.sleep(2500);
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
		assertEquals(EnumQueueEntryStatus.Aborted.getName(), getQueueEntryStatus(qeID).getName());

		jmf = new JMFBuilder().buildRemoveQueueEntry(qeID);
		resp = factory.send2URLSynchResp(jmf, simWorkerUrl, null, null, 2000);
		assertEquals(0, resp.getReturnCode());
		// give the device some time to remove the QE
		try
		{
			Thread.sleep(1000);
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
		// now the qe should be gone
		assertNull("QueueEntry is still present", getQueueEntryStatus(qeID));
	}

	public void testSuspendAbortQE()
	{
		final JDFQueueEntry runningQE = getRunningQueueEntry();
		assertNotNull(runningQE);
		final String qeID = runningQE.getQueueEntryID();

		final JMFFactory factory = JMFFactory.getJMFFactory();
		JDFJMF jmf = new JMFBuilder().buildSuspendQueueEntry(qeID);
		JDFResponse resp = factory.send2URLSynchResp(jmf, simWorkerUrl, null, null, 2000);
		assertEquals(0, resp.getReturnCode());
		// give the device some time to suspend the QE
		try
		{
			Thread.sleep(2500);
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}
		assertEquals(EnumQueueEntryStatus.Suspended.getName(), getQueueEntryStatus(qeID).getName());

		jmf = new JMFBuilder().buildAbortQueueEntry(qeID);
		resp = factory.send2URLSynchResp(jmf, simWorkerUrl, null, null, 2000);
		assertEquals(0, resp.getReturnCode());

		// give the device some time to remove the QE
		boolean hasAborted = false;
		EnumQueueEntryStatus status = null;
		int counter = 0;
		while (counter < 10 && !hasAborted)
		{
			try
			{
				Thread.sleep(750);
			}
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}

			// check whether the QEStatus is Aborted
			status = getQueueEntryStatus(qeID);
			if (EnumQueueEntryStatus.Aborted.equals(status))
			{
				assertTrue(true);
				hasAborted = true;
			}
			counter++;
		}

		// now the qe should be gone
		assertTrue(hasAborted);
	}

	public void testRogueWaves() throws Exception
	{
		for (int i = 0; i < 20; i++)
		{
			System.out.println("run #" + i);
			setUp();
			testSuspendResumeQE();
			tearDown();
			Thread.sleep(2000);
		}
	}
}

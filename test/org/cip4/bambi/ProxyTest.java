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

import java.io.InputStream;
import java.net.HttpURLConnection;

import org.cip4.bambi.core.messaging.JMFBuilder;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode.NodeIdentifier;
import org.cip4.jdflib.util.ThreadUtil;

public class ProxyTest extends BambiTestCase
{

	/**
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception
	{
		bUpdateJobID = true;
		super.setUp();
	}

	/**
	 * @throws Exception
	 */
	public void testSubmitQueueEntry_MIME() throws Exception
	{
		submitMimetoURL(proxyUrl);
	}

	/**
	 * @throws Exception
	 */
	public void testResubmitQueueEntry_MIME() throws Exception
	{
		// get number of QueueEntries before submitting
		final JDFQueue q = getQueueStatus(proxyUrl);
		assertNotNull(q);
		// build SubmitQueueEntry
		final HttpURLConnection uc = resubmitMimetoURL("qe_090713_140246586_056621", proxyUrl);
		final InputStream is = uc.getInputStream();
		final JDFDoc doc = new JDFParser().parseStream(is);
		assertNotNull(doc);

	}

	/**
	 * @throws Exception
	 */
	public void testAbortQueueEntry() throws Exception
	{
		submitMimetoURL(proxyUrl);

		int loops = 0;
		boolean hasRunningQE = false;
		final JMFFactory factory = JMFFactory.getJMFFactory();
		while (loops < 10 && !hasRunningQE)
		{
			loops++;
			Thread.sleep(1000);
			final JDFJMF jmf = new JMFBuilder().buildQueueStatus();

			final JDFDoc dresp = submitJMFtoURL(jmf, proxyUrl);
			final JDFResponse resp = dresp.getJMFRoot().getResponse(0);
			assertNotNull(resp);
			assertEquals(0, resp.getReturnCode());
			final JDFQueue q = resp.getQueue(0);
			assertNotNull(q);

			final VElement elem = q.getQueueEntryVector();
			assertTrue(elem.size() > 0);

			for (int i = 0; i < elem.size(); i++)
			{
				final JDFQueueEntry qe = (JDFQueueEntry) elem.get(i);
				assertNotNull(qe);
				if (EnumQueueEntryStatus.Running.equals(qe.getQueueEntryStatus()))
				{
					hasRunningQE = true;
					break;
				}
			}
		}
		assertTrue(hasRunningQE);
	}

	/**
	 * @throws Exception
	 */
	public void testSubmitQueueEntry_MIME_Many() throws Exception
	{
		for (int i = 0; i < 10; i++)
		{
			testSubmitQueueEntry_MIME();
			System.out.println("Submitting: " + i);
			ThreadUtil.sleep(1000);
		}
	}

	/**
	 * @throws Exception
	 */
	public void testRequestQE() throws Exception
	{
		for (int i = 0; i < 0; i++)
		{
			testSubmitQueueEntry_MIME();
			System.out.println("Submitting: " + i);
			ThreadUtil.sleep(1000);
		}
		final JMFFactory factory = JMFFactory.getJMFFactory();

		JDFQueue q = getQueueStatus(proxyUrl);
		final int count = q.getEntryCount();
		for (int i = 0; i < count; i++)
		{
			final JDFQueueEntry qe = q.getQueueEntry(i);
			if (EnumQueueEntryStatus.Waiting.equals(qe.getQueueEntryStatus()))
			{
				final String jobID = qe.getJobID();
				final String jobPartID = qe.getJobPartID();
				final NodeIdentifier ni = new NodeIdentifier(jobID, jobPartID, null);
				final JDFJMF pull = new JMFBuilder().buildRequestQueueEntry(simWorkerUrl, ni);
				// pull.getCommand(0).setSenderID("sim001"); // needed for the senderID
				final JDFDoc dresp2 = submitJMFtoURL(pull, proxySlaveUrl);
				assertNotNull(dresp2);
				ThreadUtil.sleep(2000);
				q = getQueueStatus(simWorkerUrl);
				assertNotNull(q.getQueueEntry(ni, 0));
				break;
			}
		}

	}
}

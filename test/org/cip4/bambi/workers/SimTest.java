/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2010 The International Cooperation for the Integration of 
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

package org.cip4.bambi.workers;

import java.io.InputStream;
import java.net.HttpURLConnection;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.ElementName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.JDFResourceLink;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.goldenticket.MISGoldenTicket;
import org.cip4.jdflib.goldenticket.MISPreGoldenTicket;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.resource.JDFResource;
import org.cip4.jdflib.util.ThreadUtil;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen
 * 
 */
public class SimTest extends BambiTestCase
{

	/**
	 * 
	 */
	public SimTest()
	{
		super();
		gt = enumGTType.IDP;
	}

	/**
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 * @throws Exception
	 */
	@Override
	public void setUp() throws Exception
	{

		super.setUp();
		bUpdateJobID = true;
		// acknowledgeURL = "http://localhost:8080/httpdump/acknowledgeURL";

		simWorkerUrl = "http://kie-prosirai-lg:8080/SimWorker/jmf/sim001";
		//simWorkerUrl = "http://kie-prosirai-lg:8080/SimWorker/jmf/SimWorkerRoot";
		//simWorkerUrl = "http://10.51.201.204:8080/SimWorker/jmf/SimWorkerRoot";
		//simWorkerUrl = "http://localhost:8080/richworker/jmf/sim001";
		// simWorkerUrl = "http://146.140.222.217:8080/BambiProxy/jmf/kbaProxy";
		// simWorkerUrl = "http://127.0.0.1:8080/speedmaster/jmf/XL105";
	}

	/**
	 * @return
	 */
	@Override
	protected String getDeviceID()
	{
		return "sim001";
	}

	/**
	 * @throws Exception
	 */
	public void testSubmitQueueEntry_MIME() throws Exception
	{
		// get number of QueueEntries before submitting
		//		final JDFQueue q = getQueueStatus(simWorkerUrl);
		//		assertNotNull(q);
		// build SubmitQueueEntry
		_theGT.devID = getDeviceID();
		_theGT.assign(null);
		submitMimetoURL(simWorkerUrl);
	}

	/**
	 * @throws Exception
	 */
	public void testSubmitQueueEntry_Expand() throws Exception
	{
		simWorkerUrl = "http://kie-prosirai-lg:8080/SimWorker/jmf/sim003";
		_theGT.devID = "sim003";
		_theGT.m_pdfFile = sm_dirTestData + "url1.pdf";
		_theGT.assign(null);
		submitMimetoURL(_theGT.getNode().getOwnerDocument_JDFElement(), simWorkerUrl, true);
	}

	/**
	 * @throws Exception
	 */
	public void testSubmitQueueEntry_HF() throws Exception
	{
		// get number of QueueEntries before submitting
		//		final JDFQueue q = getQueueStatus(simWorkerUrl);
		//		assertNotNull(q);
		// build SubmitQueueEntry
		_theGT.devID = null;
		_theGT.devID = "sim001";
		_theGT.assign(null);
		final JDFDoc doc = _theGT.getNode().getOwnerDocument_JDFElement();
		doc.write2File(sm_dirTestDataTemp + "hf.jdf", 2, false);

	}

	/**
	 * @throws Exception
	 */
	public void testResubmitQueueEntry_MIME() throws Exception
	{
		// get number of QueueEntries before submitting
		final JDFQueue q = getQueueStatus(simWorkerUrl);
		assertNotNull(q);
		// build SubmitQueueEntry
		final HttpURLConnection uc = resubmitMimetoURL("qe_090713_090719387_976641", simWorkerUrl);
		final InputStream is = uc.getInputStream();
		final JDFDoc doc = new JDFParser().parseStream(is);
		assertNotNull(doc);

	}

	/**
	 * @throws Exception
	 */
	public void testSubmitQueueEntry_Subscription() throws Exception
	{
		// get number of QueueEntries before submitting
		final JDFJMF jmfStat = new JMFBuilder().buildStatusSubscription("http://localhost:8080/httpdump/BambiTest", 0, 0, null);
		jmfStat.getQuery(0).getStatusQuParams().setJobID("j1");
		final JDFResponse resp = send2URL(jmfStat, simWorkerUrl);
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		_theGT.getNode().setJobID("j1");
		((MISGoldenTicket) _theGT).setNodeInfoSubscription(false);
		submitMimetoURL(simWorkerUrl);

	}

	/**
	 * @param jmfStat
	 * @param url
	 * @return the response
	 */
	private JDFResponse send2URL(final JDFJMF jmfStat, final String url)
	{
		if (jmfStat == null || url == null)
		{
			return null;
		}
		final JDFDoc dResp = jmfStat.getOwnerDocument_JDFElement().write2URL(url);
		return dResp.getJMFRoot().getResponse(0);
	}

	/**
	 * @throws Exception
	 */
	public void testSubmitQueueEntry_X() throws Exception
	{
		for (int i = 0; i < 1; i++)
		{
			_theGT.devID = null;
			//			_theGT.devID = "sim001";
			_theGT.assign(null);
			if (i != 0)
			{
				ThreadUtil.sleep(1000);
			}
			System.out.println("Submit " + i);
			submitXtoURL(simWorkerUrl);
		}
	}

	/**
	 * @throws Exception
	 */
	public void testSubmitQueueEntry_MIME_Many() throws Exception
	{
		for (int i = 1; i < 200; i++)
		{
			System.out.println("submitting " + i);
			_theGT.devID = getDeviceID();
			_theGT.assign(null);
			submitMimetoURL(simWorkerUrl);
			ThreadUtil.sleep(50);
		}
	}

	/**
	 * @throws Exception
	 */
	public void testAbortQueueEntry() throws Exception
	{
		// build SubmitQueueEntry
		submitMimetoURL(simWorkerUrl);

		int loops = 0;
		boolean hasRunningQE = false;
		final JMFFactory factory = JMFFactory.getJMFFactory();
		while (loops < 10 && !hasRunningQE)
		{
			loops++;
			Thread.sleep(1000);
			final JDFJMF jmf = new JMFBuilder().buildQueueStatus();

			final JDFResponse resp = factory.send2URLSynchResp(jmf, simWorkerUrl, null, null, 2000);
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
		abortRemoveAll(simWorkerUrl);

	}

	/**
	 * @throws Exception
	 */
	public void testPlateSetter() throws Exception
	{
		_theGT = new MISPreGoldenTicket(1, EnumVersion.Version_1_3, 2, 2, null);
		final MISPreGoldenTicket pgt = (MISPreGoldenTicket) _theGT;
		_theGT.bExpandGrayBox = false;
		pgt.setCategory(MISPreGoldenTicket.MISPRE_PLATESETTING);
		pgt.assign(null);
		final JDFNode node = pgt.getNode();
		final JDFResource r = node.getResource(ElementName.EXPOSEDMEDIA, null, 0);
		final JDFResourceLink rl = node.getLink(r, null);
		rl.setAmount(4, null);
		// build SubmitQueueEntry
		submitMimetoURL(simWorkerUrl);
	}

}

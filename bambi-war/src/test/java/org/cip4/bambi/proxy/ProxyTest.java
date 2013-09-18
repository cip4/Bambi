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

package org.cip4.bambi.proxy;

import java.io.File;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.BambiTestHelper;
import org.cip4.bambi.core.MultiDeviceProperties;
import org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties;
import org.cip4.bambi.server.BambiServer;
import org.cip4.jdflib.auto.JDFAutoDeviceFilter.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.node.NodeIdentifier;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.StringUtil;
import org.cip4.jdflib.util.ThreadUtil;

/**
  * @author Rainer Prosi, Heidelberger Druckmaschinen *
 */
public class ProxyTest extends BambiTestCase
{
	/**
	 * 
	 * TODO Please insert comment!
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		new BambiServer().runServer();
	}

	/**
	 * 
	 */
	public ProxyTest()
	{
		super();
	}

	/**
	 * @return 
	 * 
	 */
	protected String getProxyURLForSlave()
	{
		return StringUtil.replaceString(getWorkerURL(), "/jmf/", "/slavejmf/");
	}

	/**
	 * @return
	 */
	@Override
	protected enumGTType getGTType()
	{
		return enumGTType.IDP;
		//return enumGTType.MISFIN_STITCH;
	}

	/**
	 * @see org.cip4.bambi.BambiTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		bUpdateJobID = true;
		workerURLBase = "http://localhost:8080/BambiProxy/jmf/";
		deviceID = null;

		//		workerURL = "http://146.140.222.217:8080/BambiProxy/jmf/pushproxy";
		super.setUp();
		_theGT.good = 100;
		_theGT.waste = 50;
	}

	/**
	 * @throws Exception
	 */
	public void testKnownDevices() throws Exception
	{
		BambiTestHelper h = new BambiTestHelper();
		JMFBuilder b = h.getBuilder();
		JDFJMF jmf = b.buildKnownDevicesQuery(EnumDeviceDetails.Brief);
		JDFDoc d = h.submitJMFtoURL(jmf, getWorkerURL());
		assertNotNull(d);
	}

	/**
	 * @throws Exception
	 */
	public void testKnownMessages() throws Exception
	{
		BambiTestHelper h = new BambiTestHelper();
		JMFBuilder b = h.getBuilder();
		JDFJMF jmf = b.buildKnownMessagesQuery();
		JDFDoc d = h.submitJMFtoURL(jmf, getWorkerURL());
		assertNotNull(d);
	}

	/**
	 * @throws Exception
	 */
	public void testSubmitQueueEntry_MIME() throws Exception
	{
		_theGT.devID = getDeviceID();
		_theGT.assign(null);

		submitMimetoURL("http://kie-prosirai-mc:44484/BambiProxy/jmf/pushproxy");
		//		submitMimetoURL(getWorkerURL());
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
			submitMimetoURL(getWorkerURL());
			ThreadUtil.sleep(50);
		}
	}

	/**
	 * @throws Exception
	 */
	public void testAbortQueueEntry() throws Exception
	{
		submitMimetoURL(getWorkerURL());

		int loops = 0;
		boolean hasRunningQE = false;
		while (loops < 10 && !hasRunningQE)
		{
			loops++;
			Thread.sleep(1000);
			final JDFJMF jmf = new JMFBuilder().buildQueueStatus();

			final JDFDoc dresp = submitJMFtoURL(jmf, getWorkerURL());
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
	public void testRequestQE() throws Exception
	{
		String proxyUrl = StringUtil.replaceString(getWorkerURL(), "push", "pull");
		String proxySlaveUrl = StringUtil.replaceString(getProxyURLForSlave(), "push", "pull");
		for (int i = 0; i < 1; i++)
		{
			submitMimetoURL(proxyUrl);
			System.out.println("Submitting: " + i);
			ThreadUtil.sleep(500);
		}
		JDFQueue q = getQueueStatus(proxyUrl);
		final int count = q.getEntryCount();
		NodeIdentifier ni = null;
		for (int i = 0; i < count; i++)
		{
			final JDFQueueEntry qe = q.getQueueEntry(i);
			if (EnumQueueEntryStatus.Waiting.equals(qe.getQueueEntryStatus()))
			{
				final String jobID = qe.getJobID();
				final String jobPartID = qe.getJobPartID();
				ni = new NodeIdentifier(jobID, jobPartID, null);
				final JDFJMF pull = new JMFBuilder().buildRequestQueueEntry(getWorkerURL(), ni);
				// pull.getCommand(0).setSenderID("sim001"); // needed for the senderID
				CPUTimer ct = new CPUTimer(true);
				final JDFDoc dresp2 = submitJMFtoURL(pull, proxySlaveUrl);
				System.out.println(ni + "" + i + ct.toString());
				assertNotNull(dresp2);
				if (dresp2.getJMFRoot().getResponse(0).getReturnCode() == 0)
				{
					System.out.print("break");
					break;
				}
			}
		}
		ThreadUtil.sleep(2000);
		q = getQueueStatus(getWorkerURL());
		assertNotNull(q.getQueueEntry(ni, 0));
	}

	/**
	 * @see org.cip4.bambi.BambiTestCase#moreSetup(org.cip4.bambi.core.MultiDeviceProperties.DeviceProperties)
	 * @param devProp
	*/
	@Override
	protected void moreSetup(DeviceProperties devProp)
	{
		super.moreSetup(devProp);
		devProp.setDeviceClassName("org.cip4.bambi.proxy.ProxyDevice");
	}

	/**
	 * @see org.cip4.bambi.BambiTestCase#createPropertiesForContainer()
	 * @return
	*/
	@Override
	protected MultiDeviceProperties createPropertiesForContainer()
	{
		MultiDeviceProperties props = new ProxyProperties(new File(sm_dirContainer), "test");
		return props;
	}
}

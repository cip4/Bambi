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

import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.auto.JDFAutoQueueEntry.EnumQueueEntryStatus;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.util.StatusCounter;

public class ProxyTest extends BambiTestCase
{

	@Override
	public void setUp() throws Exception
	{

		super.setUp();
		//		abortRemoveAll(proxyUrl);
	}

	private void submitMimeToProxy()
	{
		submitMimetoURL(proxyUrl);
	}

	public void testSubmitQueueEntry_MIME()
	{
		// get number of QueueEntries before submitting
		JDFJMF jmfStat = JMFFactory.buildQueueStatus();
		JDFResponse resp = JMFFactory.send2URLSynchResp(jmfStat, proxyUrl, null, null, 2000);
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		JDFQueue q = resp.getQueue(0);
		assertNotNull(q);
		int oldSize = q.getEntryCount();
		submitMimeToProxy();

		// check that the QE is on the proxy
		JDFJMF jmf = JMFFactory.buildQueueStatus();
		resp = JMFFactory.send2URLSynchResp(jmf, proxyUrl, null, null, 2000);
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		q = resp.getQueue(0);
		assertNotNull(q);
		int newCount = q.getEntryCount();
		assertEquals(oldSize + 1, newCount);

	}

	public void testAbortQueueEntry() throws InterruptedException
	{
		submitMimeToProxy();

		int loops = 0;
		boolean hasRunningQE = false;
		while (loops < 10 && !hasRunningQE)
		{
			loops++;
			Thread.sleep(1000);
			JDFJMF jmf = JMFFactory.buildQueueStatus();

			JDFResponse resp = JMFFactory.send2URLSynchResp(jmf, proxyUrl, null, null, 2000);
			assertNotNull(resp);
			assertEquals(0, resp.getReturnCode());
			JDFQueue q = resp.getQueue(0);
			assertNotNull(q);

			VElement elem = q.getQueueEntryVector();
			assertTrue(elem.size() > 0);

			for (int i = 0; i < elem.size(); i++)
			{
				JDFQueueEntry qe = (JDFQueueEntry) elem.get(i);
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

	public void testSubmitQueueEntry_MIME_Many()
	{
		// get number of QueueEntries before submitting
		JDFJMF jmfStat = JMFFactory.buildQueueStatus();
		JDFResponse resp = JMFFactory.send2URLSynchResp(jmfStat, proxyUrl, null, "foo", 20000);
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		JDFQueue q = resp.getQueue(0);
		assertNotNull(q);
		int oldSize = q.getEntryCount();

		// check that the QE is on the proxy
		JDFJMF jmf = JMFFactory.buildQueueStatus();
		for (int i = 0; i < 2213; i++)
		{
			System.out.println("submitting " + i);
			submitMimeToProxy();
			resp = JMFFactory.send2URLSynchResp(jmf, proxyUrl, null, null, 5000);
			assertNotNull("loop " + i, resp);
			assertEquals(0, resp.getReturnCode());
			q = resp.getQueue(0);
			assertNotNull(q);
			int newCount = q.getEntryCount();
			StatusCounter.sleep(1000);
			// assertEquals( oldSize+i,newCount );
		}

		//        abortRemoveAll(simWorkerUrl);
	}
}

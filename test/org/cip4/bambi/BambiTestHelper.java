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

package org.cip4.bambi;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.mail.Multipart;

import org.cip4.bambi.core.messaging.JMFFactory;
import org.cip4.jdflib.JDFTestCaseBase;
import org.cip4.jdflib.core.AttributeName;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFParser;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.VElement;
import org.cip4.jdflib.datatypes.JDFAttributeMap;
import org.cip4.jdflib.extensions.XJDF20;
import org.cip4.jdflib.jmf.JDFCommand;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFMessage;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JDFQueueEntry;
import org.cip4.jdflib.jmf.JDFQueueSubmissionParams;
import org.cip4.jdflib.jmf.JDFResponse;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.MimeUtil.MIMEDetails;
import org.cip4.jdflib.util.UrlUtil.HTTPDetails;

/**
 * @author Rainer Prosi, Heidelberger Druckmaschinen abstract test case for all bambi tests note that this has some site specific details that must be modified
 */
public class BambiTestHelper extends JDFTestCaseBase
{

	public boolean bUpdateJobID = false;
	public int chunkSize = -1;
	public String transferEncoding = UrlUtil.BASE64;
	public String returnJMF = "http://localhost:8080/httpDump/returnJMF";
	public String returnURL = null;// "http://localhost:8080/httpDump/returnURL";

	/**
	 * banbi test case
	 */
	public BambiTestHelper()
	{

	}

	/**
	 * cleaning up, brute-force-sytle: send a AbortQueueEntry and a RemoveQueueEntry message to every QueueEntry in the Queue
	 * @param url the URL of the device to send the command to
	 */
	public void abortRemoveAll(final String url)
	{
		final JMFFactory factory = JMFFactory.getJMFFactory();
		JDFJMF jmf = factory.buildQueueStatus();
		final JDFResponse resp = factory.send2URLSynchResp(jmf, url, null, "testcase", 2000);
		if (resp == null)
		{
			System.err.println("failed to send QueueStatus");
			return;
		}
		final JDFQueue theQueue = resp.getQueue(0);
		if (theQueue == null)
		{
			return;
		}
		final VElement qVec = theQueue.getQueueEntryVector();
		final int siz = qVec.size();
		if (siz == 0)
		{
			return;
		}

		for (int i = siz - 1; i >= 0; i--)
		{
			final String qeid = ((JDFQueueEntry) qVec.get(i)).getQueueEntryID();
			jmf = factory.buildAbortQueueEntry(qeid);
			factory.send2URL(jmf, url, null, null, "testcase");
		}

		// wait to allow the worker to process the AbortQueueEntries,
		// then send RemoveQueueEntry messages

		for (int i = 0; i < siz; i++)
		{
			final String qeid = ((JDFQueueEntry) qVec.get(i)).getQueueEntryID();
			jmf = factory.buildRemoveQueueEntry(qeid);
			factory.send2URL(jmf, url, null, null, "testcase");
		}
	}

	/**
	 * requires assigned node...
	 * @param url the url to send to
	 * @throws MalformedURLException
	 */
	public void submitXtoURL(final JDFNode n, final String url) throws MalformedURLException
	{
		final XJDF20 xc = new XJDF20();
		final KElement e = xc.makeNewJDF(n, null);
		submitMimetoURL(new JDFDoc(e.getOwnerDocument()), url);
	}

	/**
	 * @param n the node to send as root node
	 * @param url the url to send to
	 * @throws MalformedURLException
	 */
	public void submitMimetoURL(final JDFDoc d, final String url) throws MalformedURLException
	{
		final JDFDoc docJMF = new JDFDoc("JMF");
		final JDFJMF jmf = docJMF.getJMFRoot();
		final JDFCommand com = (JDFCommand) jmf.appendMessageElement(JDFMessage.EnumFamily.Command, JDFMessage.EnumType.SubmitQueueEntry);
		final JDFQueueSubmissionParams queueSubmissionParams = com.appendQueueSubmissionParams();
		queueSubmissionParams.setURL("dummy");
		queueSubmissionParams.setPriority(42);
		updateJobIDs(d);
		if (returnJMF != null)
		{
			queueSubmissionParams.setReturnJMF(new URL(returnJMF));
		}
		if (returnURL != null)
		{
			queueSubmissionParams.setReturnURL(new URL(returnURL));
		}

		final Multipart mp = MimeUtil.buildMimePackage(docJMF, d, false);

		try
		{
			final MIMEDetails md = new MIMEDetails();
			md.transferEncoding = transferEncoding;
			md.httpDetails.chunkSize = chunkSize;
			final HttpURLConnection response = MimeUtil.writeToURL(mp, url, md);
			if (!url.toLowerCase().startsWith("file:"))
			{
				assertEquals(url, 200, response.getResponseCode());
			}
		}
		catch (final Exception e)
		{
			fail(e.getMessage()); // fail on exception
		}
	}

	/**
	 * @param d
	 */
	private void updateJobIDs(final JDFDoc d)
	{
		if (bUpdateJobID)
		{
			final String newJobID = "Job" + JDFElement.uniqueID(0);
			final KElement root = d.getRoot();
			final VElement v = root.getChildrenByTagName_KElement(null, null, new JDFAttributeMap(AttributeName.JOBID, "*"), false, true, 0);
			v.add(root);
			for (int i = 0; i < v.size(); i++)
			{
				v.get(i).setAttribute(AttributeName.JOBID, newJobID);
			}
		}
	}

	public JDFQueue getQueueStatus(final String qURL)
	{
		final JMFFactory factory = JMFFactory.getJMFFactory();
		final JDFJMF jmf = factory.buildQueueStatus();
		final JDFDoc dresp = submitJMFtoURL(jmf, qURL);
		final JDFResponse resp = dresp.getJMFRoot().getResponse(0);
		assertNotNull(resp);
		assertEquals(0, resp.getReturnCode());
		final JDFQueue q = resp.getQueue(0);
		assertNotNull(q);
		return q;
	}

	/**
	 * @param jmf the jmf to send
	 * @param url the url to send to
	 * @throws MalformedURLException
	 */
	public JDFDoc submitJMFtoURL(final JDFJMF jmf, final String url)
	{
		try
		{
			final HTTPDetails md = new HTTPDetails();
			md.chunkSize = chunkSize;
			final HttpURLConnection urlCon = jmf.getOwnerDocument_JDFElement().write2HTTPURL(new URL(url), md);
			assertEquals(url, 200, urlCon.getResponseCode());
			final JDFParser parser = new JDFParser();
			final InputStream inStream = urlCon.getInputStream();

			parser.parseStream(inStream);
			final JDFDoc docResponse = parser.getDocument() == null ? null : new JDFDoc(parser.getDocument());
			return docResponse;
		}
		catch (final Exception e)
		{
			fail(e.getMessage()); // fail on exception
		}
		return null;
	}

}

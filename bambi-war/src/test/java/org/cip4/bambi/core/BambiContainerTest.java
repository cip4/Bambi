/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2016 The International Cooperation for the Integration of 
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

package org.cip4.bambi.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.mail.MessagingException;

import org.cip4.bambi.BambiTestCase;
import org.cip4.bambi.BambiTestHelper;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement;
import org.cip4.jdflib.core.JDFElement.EnumValidationLevel;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.core.XMLDoc;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JDFQueue;
import org.cip4.jdflib.jmf.JMFBuilder;
import org.cip4.jdflib.util.ByteArrayIOStream;
import org.cip4.jdflib.util.CPUTimer;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.ThreadUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.mime.MimeReader;
import org.cip4.jdflib.util.mime.MimeWriter;
import org.junit.Test;

/**
 * @author Dr. Rainer Prosi, Heidelberger Druckmaschinen AG
 * 
 * 16.11.2009
 */
public class BambiContainerTest extends BambiTestCase
{

	/**
	 * 
	 */
	public BambiContainerTest()
	{
		super();
		wantContainer = true;
	}

	/**
	 * 
	 */
	@Test
	public void testConstruct()
	{
		assertNotNull(bambiContainer.getRootDev());
		final AbstractDevice deviceFromID = bambiContainer.getDeviceFromID("device");
		assertNotNull(deviceFromID);
		assertEquals(deviceFromID.getDeviceID(), "device");
		assertNotNull("proxy incorrectly set? ", UrlUtil.writeToURL("http://www.example.com", null, UrlUtil.GET, UrlUtil.TEXT_PLAIN, null));
	}

	/**
	 * 
	 */
	@Test
	public void testGetTimer()
	{
		assertNotNull(bambiContainer.getTimer(null));
	}

	/**
	 * 
	 */
	@Test
	public void testStartStopTimer()
	{
		bambiContainer.startTimer(null);
		ThreadUtil.sleep(50);
		CPUTimer t = bambiContainer.getTimer(null);
		assertTrue(t.getCurrentRealTime() > 10);
		bambiContainer.stopTimer(null);
		assertTrue(t.getTotalRealTime() > 10);
	}

	/**
	 * 
	 */
	@Test
	public void testHandleJMF()
	{
		final JDFJMF jmf = new JMFBuilder().buildKnownMessagesQuery();
		final XMLResponse resp = bambiContainer.processJMFDoc(new XMLRequest(jmf));
		assertNotNull(resp);
		assertTrue(((JDFElement) resp.getXML()).isValid(EnumValidationLevel.Complete));
	}

	/**
	 * 
	 */
	@Test
	public void testHandleJunkXML()
	{
		final KElement junk = new XMLDoc("junk", null).getRoot();

		final XMLRequest request = new XMLRequest(junk);
		request.setContentType("application/junk+xml");
		final XMLResponse resp = bambiContainer.processXMLDoc(request);
		assertNotNull(resp);
	}

	/**
	 * 
	 */
	@Test
	public void testSubmitRawJDF()
	{
		final JDFDoc junk = new JDFDoc("JDF");

		final XMLRequest request = new XMLRequest(junk);
		final XMLResponse resp = bambiContainer.processXMLDoc(request);
		assertNotNull(resp);
	}

	/**
	 * 
	 */
	@Test
	public void testSubmitRawXJDF()
	{
		final XMLDoc junk = new XMLDoc("XJDF", null);

		final XMLRequest request = new XMLRequest(junk);
		final XMLResponse resp = bambiContainer.processXMLDoc(request);
		assertNotNull(resp);
	}

	/**
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testHandleStreamXML() throws IOException
	{
		final JDFJMF jmf = new JMFBuilder().buildKnownMessagesQuery();
		final ByteArrayIOStream ios = new ByteArrayIOStream();
		jmf.getOwnerDocument_JDFElement().write2Stream(ios, 0, true);
		final StreamRequest r = new StreamRequest(ios.getInputStream());
		r.setContentType(UrlUtil.TEXT_XML);
		final XMLResponse resp = bambiContainer.processStream(r);
		assertNotNull(resp);
		resp.getXMLDoc().write2File(sm_dirTestDataTemp + "handleStream.resp.jmf", 2, false);
		assertTrue(((JDFElement) resp.getXML()).isValid(EnumValidationLevel.Complete));
	}

	/**
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testHandleStreamError() throws IOException
	{
		final JDFJMF jmf = new JMFBuilder().buildKnownMessagesQuery();
		final ByteArrayIOStream ios = new ByteArrayIOStream();
		jmf.getOwnerDocument_JDFElement().write2Stream(ios, 0, true);
		final StreamRequest r = new StreamRequest(ios.getInputStream());
		r.setContentType(MimeUtil.MULTIPART_RELATED);
		final XMLResponse resp = bambiContainer.processStream(r);
		assertNotNull(resp);
		resp.getXMLDoc().write2File(sm_dirTestDataTemp + "handleStream.resp.jmf", 2, false);
		assertTrue(((JDFElement) resp.getXML()).isValid(EnumValidationLevel.Complete));
	}

	/**
	 * @throws Exception 
	 * 
	 */
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		JDFElement.setDefaultJDFVersion(EnumVersion.Version_1_4);
		startContainer();
	}

	/**
	 * 
	 */
	@Test
	public void testHandleJMFSubscription()
	{
		final JDFJMF jmf = new JMFBuilder().buildStatusSubscription("http://www.example.com", 15, 0, null);
		final XMLResponse resp = bambiContainer.processJMFDoc(new XMLRequest(jmf));
		assertNotNull(resp);
		final JDFJMF jmfResp = (JDFJMF) resp.getXML();
		resp.getXMLDoc().write2File(sm_dirTestDataTemp + "respSubs.jmf", 2, false);
		assertTrue(jmfResp.isValid(EnumValidationLevel.Complete));
		assertTrue(jmfResp.getResponse(0).getSubscribed());
	}

	/**
	 * @throws IOException if bad things happen
	 * 
	 */
	@Test
	public void testHandleGet() throws IOException
	{
		final StreamRequest sr = new StreamRequest((InputStream) null);
		sr.setPost(false);
		sr.setRequestURI("http://dummy:8080/war/showQueue/" + deviceID);
		final XMLResponse resp = bambiContainer.processStream(sr);
		assertNotNull(resp);
		final KElement htmlResp = resp.getXML();
		assertNotNull(htmlResp);
		assertTrue(htmlResp instanceof JDFQueue);
	}

	/**
	 * @throws IOException if bad things happen
	 * 
	 */
	@Test
	public void testSubmit() throws IOException
	{

		final JDFDoc docJDF = _theGT.getNode().getOwnerDocument_JDFElement();

		final BambiTestHelper helper = getHelper();
		final XMLResponse resp = helper.submitMimetoContainer(docJDF, getWorkerURL() + deviceID);
		assertNotNull(resp);
		final KElement htmlResp = resp.getXML();
		assertNotNull(htmlResp);
		assertTrue(htmlResp instanceof JDFJMF);
		final JDFQueue queue = helper.getQueueStatus(getWorkerURL());
		assertTrue(queue.numEntries(null) > 0);

	}

	/**
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testSubmitCrap() throws IOException
	{

		final JDFDoc docJDF = _theGT.getNode().getOwnerDocument_JDFElement();

		final BambiTestHelper helper = new BambiTestHelper();
		final JDFDoc jmfDoc = helper.createSubmitJMF(docJDF);
		final MimeWriter mimeWriter = new MimeWriter();
		mimeWriter.buildMimePackage(jmfDoc, null, false);
		final MimeRequest mr = new MimeRequest(new MimeReader(mimeWriter));
		mr.setRequestURI("http://dummy:8080/war/jmf/" + deviceID);
		final XMLResponse resp = bambiContainer.processMultipleDocuments(mr);
		assertNotNull(resp);
		final KElement htmlResp = resp.getXML();
		assertNotNull(htmlResp);
		assertTrue(htmlResp instanceof JDFJMF);
	}

	/**
	 * @throws IOException 
	 * 
	 */
	@Test
	public void testSubmitMany() throws IOException
	{
		final CPUTimer ct = new CPUTimer(false);
		for (int i = 0; i < 1000; i++)
		{
			ct.start();
			testSubmit();
			System.out.println(ct);
			ct.stop();
		}

	}

	/**
	 * @param jdf
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws MessagingException
	 */
	protected StreamRequest createSubmissionStreamRequest(final KElement jdf) throws MalformedURLException, IOException, MessagingException
	{
		final BambiTestHelper helper = new BambiTestHelper();
		helper.returnJMF = returnJMF;
		final JDFDoc xjdfDoc = new JDFDoc(jdf.getOwnerDocument());
		final JDFDoc jmfDoc = helper.createSubmitJMF(xjdfDoc);
		final MimeWriter mw = new MimeWriter();
		mw.buildMimePackage(jmfDoc, xjdfDoc, false);
		final ByteArrayIOStream ios = new ByteArrayIOStream();
		mw.writeToStream(ios);
		final StreamRequest req = new StreamRequest(ios.getInputStream());
		req.setContentType(MimeUtil.MULTIPART_RELATED);
		return req;
	}

}

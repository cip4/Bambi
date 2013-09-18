/*
 *
 * The CIP4 Software License, Version 1.0
 *
 *
 * Copyright (c) 2001-2013 The International Cooperation for the Integration of 
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
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.mime.MimeReader;
import org.cip4.jdflib.util.mime.MimeWriter;

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
	}

	/**
	 * 
	 */
	public void testConstruct()
	{
		assertNotNull(bambiContainer.getRootDev());
		AbstractDevice deviceFromID = bambiContainer.getDeviceFromID("device");
		assertNotNull(deviceFromID);
		assertEquals(deviceFromID.getDeviceID(), "device");
		assertNotNull("proxy incorrectly set? ", UrlUtil.writeToURL("http://www.example.com", null, UrlUtil.GET, UrlUtil.TEXT_PLAIN, null));
	}

	/**
	 * 
	 */
	public void testHandleJMF()
	{
		JDFJMF jmf = new JMFBuilder().buildKnownMessagesQuery();
		XMLResponse resp = bambiContainer.processJMFDoc(new XMLRequest(jmf));
		assertNotNull(resp);
		assertTrue(((JDFElement) resp.getXML()).isValid(EnumValidationLevel.Complete));
	}

	/**
	 * 
	 */
	public void testHandleJunkXML()
	{
		KElement junk = new XMLDoc("junk", null).getRoot();

		XMLRequest request = new XMLRequest(junk);
		request.setContentType("application/junk+xml");
		XMLResponse resp = bambiContainer.processXMLDoc(request);
		assertNotNull(resp);
	}

	/**
	 * 
	 */
	public void testSubmitRawJDF()
	{
		JDFDoc junk = new JDFDoc("JDF");

		XMLRequest request = new XMLRequest(junk);
		XMLResponse resp = bambiContainer.processXMLDoc(request);
		assertNotNull(resp);
	}

	/**
	 * 
	 */
	public void testSubmitRawXJDF()
	{
		XMLDoc junk = new XMLDoc("XJDF", null);

		XMLRequest request = new XMLRequest(junk);
		XMLResponse resp = bambiContainer.processXMLDoc(request);
		assertNotNull(resp);
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void testHandleStreamXML() throws IOException
	{
		JDFJMF jmf = new JMFBuilder().buildKnownMessagesQuery();
		ByteArrayIOStream ios = new ByteArrayIOStream();
		jmf.getOwnerDocument_JDFElement().write2Stream(ios, 0, true);
		StreamRequest r = new StreamRequest(ios.getInputStream());
		r.setContentType(UrlUtil.TEXT_XML);
		XMLResponse resp = bambiContainer.processStream(r);
		assertNotNull(resp);
		resp.getXMLDoc().write2File(sm_dirTestDataTemp + "handleStream.resp.jmf", 2, false);
		assertTrue(((JDFElement) resp.getXML()).isValid(EnumValidationLevel.Complete));
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void testHandleStreamError() throws IOException
	{
		JDFJMF jmf = new JMFBuilder().buildKnownMessagesQuery();
		ByteArrayIOStream ios = new ByteArrayIOStream();
		jmf.getOwnerDocument_JDFElement().write2Stream(ios, 0, true);
		StreamRequest r = new StreamRequest(ios.getInputStream());
		r.setContentType(MimeUtil.MULTIPART_RELATED);
		XMLResponse resp = bambiContainer.processStream(r);
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
	public void testHandleJMFSubscription()
	{
		JDFJMF jmf = new JMFBuilder().buildStatusSubscription("http://www.example.com", 15, 0, null);
		XMLResponse resp = bambiContainer.processJMFDoc(new XMLRequest(jmf));
		assertNotNull(resp);
		JDFJMF jmfResp = (JDFJMF) resp.getXML();
		resp.getXMLDoc().write2File(sm_dirTestDataTemp + "respSubs.jmf", 2, false);
		assertTrue(jmfResp.isValid(EnumValidationLevel.Complete));
		assertTrue(jmfResp.getResponse(0).getSubscribed());
	}

	/**
	 * @throws IOException if bad things happen
	 * 
	 */
	public void testHandleGet() throws IOException
	{
		StreamRequest sr = new StreamRequest((InputStream) null);
		sr.setPost(false);
		sr.setRequestURI("http://dummy:8080/war/showQueue/" + deviceID);
		XMLResponse resp = bambiContainer.processStream(sr);
		assertNotNull(resp);
		KElement htmlResp = resp.getXML();
		assertNotNull(htmlResp);
		assertTrue(htmlResp instanceof JDFQueue);
	}

	/**
	 * @throws IOException if bad things happen
	 * 
	 */
	public void testSubmit() throws IOException
	{

		final JDFDoc docJDF = _theGT.getNode().getOwnerDocument_JDFElement();

		BambiTestHelper helper = getHelper();
		XMLResponse resp = helper.submitMimetoContainer(docJDF, getWorkerURL() + deviceID);
		assertNotNull(resp);
		KElement htmlResp = resp.getXML();
		assertNotNull(htmlResp);
		assertTrue(htmlResp instanceof JDFJMF);
		JDFQueue queue = helper.getQueueStatus(getWorkerURL());
		assertTrue(queue.numEntries(null) > 0);

	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void testSubmitCrap() throws IOException
	{

		final JDFDoc docJDF = _theGT.getNode().getOwnerDocument_JDFElement();

		BambiTestHelper helper = new BambiTestHelper();
		JDFDoc jmfDoc = helper.createSubmitJMF(docJDF);
		MimeWriter mimeWriter = new MimeWriter();
		mimeWriter.buildMimePackage(jmfDoc, null, false);
		MimeRequest mr = new MimeRequest(new MimeReader(mimeWriter));
		mr.setRequestURI("http://dummy:8080/war/jmf/" + deviceID);
		XMLResponse resp = bambiContainer.processMultipleDocuments(mr);
		assertNotNull(resp);
		KElement htmlResp = resp.getXML();
		assertNotNull(htmlResp);
		assertTrue(htmlResp instanceof JDFJMF);
	}

	/**
	 * @throws IOException 
	 * 
	 */
	public void testSubmitMany() throws IOException
	{
		CPUTimer ct = new CPUTimer(false);
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
	protected StreamRequest createSubmissionStreamRequest(KElement jdf) throws MalformedURLException, IOException, MessagingException
	{
		final BambiTestHelper helper = new BambiTestHelper();
		helper.returnJMF = returnJMF;
		JDFDoc xjdfDoc = new JDFDoc(jdf.getOwnerDocument());
		JDFDoc jmfDoc = helper.createSubmitJMF(xjdfDoc);
		MimeWriter mw = new MimeWriter();
		mw.buildMimePackage(jmfDoc, xjdfDoc, false);
		ByteArrayIOStream ios = new ByteArrayIOStream();
		mw.writeToStream(ios);
		StreamRequest req = new StreamRequest(ios.getInputStream());
		req.setContentType(MimeUtil.MULTIPART_RELATED);
		return req;
	}

}
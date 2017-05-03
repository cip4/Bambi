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
package org.cip4.bambi.core.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.io.IOException;
import java.io.InputStream;

import javax.mail.BodyPart;

import org.cip4.bambi.BambiTestCaseBase;
import org.cip4.bambi.core.ConverterCallback;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumDeviceDetails;
import org.cip4.jdflib.auto.JDFAutoStatusQuParams.EnumJobDetails;
import org.cip4.jdflib.core.JDFDoc;
import org.cip4.jdflib.core.JDFElement.EnumVersion;
import org.cip4.jdflib.core.KElement;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.jmf.JMFBuilderFactory;
import org.cip4.jdflib.node.JDFNode;
import org.cip4.jdflib.util.MimeUtil;
import org.cip4.jdflib.util.UrlUtil;
import org.cip4.jdflib.util.zip.ZipReader;
import org.junit.Test;

public class MessageDetailsTest extends BambiTestCaseBase
{

	class MyCallback extends ConverterCallback
	{

		/**
		 * @see org.cip4.bambi.core.ConverterCallback#getJMFContentType()
		 */
		@Override
		public String getJMFContentType()
		{
			return UrlUtil.VND_JMF;
		}

	}

	/**
	 *
	 */
	@Test
	public void testGetContentType()
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		MessageDetails md = new MessageDetails(jmf, null, null, null, "http://foo");
		assertEquals(UrlUtil.VND_JMF, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testNameJMF()
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		MessageDetails md = new MessageDetails(jmf, null, null, null, "http://foo");
		assertEquals("Status", md.getName());
	}

	/**
	 *
	 */
	@Test
	public void testNameElem()
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		KElement message = new JDFDoc("Message").getRoot();
		message.copyElement(jmf, null);
		MessageDetails md = new MessageDetails(message);
		assertEquals("Status", md.getName());
	}

	/**
	 *
	 */
	@Test
	public void testGetStream()
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildStatusSignal(EnumDeviceDetails.Full, EnumJobDetails.Full);
		MessageDetails md = new MessageDetails(jmf, null, null, null, "http://foo");
		InputStream is = md.getInputStream();
		assertNotNull(JDFDoc.parseStream(is).getJMFRoot());
	}

	/**
	 *
	 */
	@Test
	public void testContentTypeZip()
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		assertEquals(UrlUtil.APPLICATION_ZIP, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testContentTypeMime()
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		ConverterCallback cb = new ConverterCallback();
		MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		assertEquals(MimeUtil.MULTIPART_RELATED, md.getContentType());
	}

	/**
	 *
	 */
	@Test
	public void testContentTypeXJMF()
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		MessageDetails md = new MessageDetails(jmf, null, cb, null, "http://foo");
		assertEquals(UrlUtil.VND_XJMF, md.getContentType());
	}

	/**
	 * @throws IOException
	 *
	 */
	@Test
	public void testStreamZip() throws IOException
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		ConverterCallback cb = new ConverterCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		InputStream is = md.getInputStream();
		ZipReader zr = ZipReader.getZipReader(is);
		zr.buffer();
		assertNotNull(zr);
		assertEquals(zr.getEntries().size(), 2);
		assertEquals('P', is.read());

	}

	/**
	 * @throws Throwable
	 *
	 */
	@Test
	public void testStreamNoZip() throws Throwable
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		ConverterCallback cb = new MyCallback();
		cb.setFixToExtern(EnumVersion.Version_2_0);
		MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		assertEquals(MimeUtil.MULTIPART_RELATED, md.getContentType());
		InputStream is = md.getInputStream();
		assertNotSame('P', is.read());
	}

	/**
	 *
	 */
	@Test
	public void testStreamMime()
	{
		JDFJMF jmf = JMFBuilderFactory.getJMFBuilder(null).buildSubmitQueueEntry("http://foo");
		JDFNode jdf = JDFDoc.parseFile(sm_dirTestData + "Elk_ConventionalPrinting.jdf").getJDFRoot();
		ConverterCallback cb = new ConverterCallback();
		MessageDetails md = new MessageDetails(jmf, jdf, null, cb, null, "http://foo");
		InputStream is = md.getInputStream();
		BodyPart[] bp = MimeUtil.extractMultipartMime(is);

		assertNotNull(bp);
		assertEquals(bp.length, 2);
	}
}
